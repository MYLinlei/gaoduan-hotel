package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.entity.ProductSku;
import com.sky.entity.ShoppingCart;
import com.sky.entity.SkuStockLog;
import com.sky.entity.SkuStockReservation;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.ProductSkuMapper;
import com.sky.mapper.SkuInventoryMapper;
import com.sky.service.SkuInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkuInventoryServiceImpl implements SkuInventoryService {

    private static final String RESERVE = "RESERVE";
    private static final String CONFIRM = "CONFIRM";
    private static final String CANCEL_RELEASE = "CANCEL_RELEASE";
    private static final String CANCEL_RESTORE = "CANCEL_RESTORE";

    @Autowired
    private SkuInventoryMapper skuInventoryMapper;

    @Autowired
    private ProductSkuMapper productSkuMapper;

    @Override
    @Transactional
    public void reserve(Orders order, List<ShoppingCart> cartItems) {
        List<SkuStockReservation> existing = skuInventoryMapper.listByOrderIdForUpdate(order.getId());
        if (!existing.isEmpty()) {
            return;
        }

        Map<Long, Integer> quantities = new LinkedHashMap<>();
        Map<Long, Long> productIds = new LinkedHashMap<>();
        for (ShoppingCart item : cartItems) {
            if (item.getProductId() == null || item.getSkuId() == null) {
                continue;
            }
            quantities.merge(item.getSkuId(), item.getNumber(), Integer::sum);
            productIds.put(item.getSkuId(), item.getProductId());
        }

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Long skuId = entry.getKey();
            Integer quantity = entry.getValue();
            if (skuInventoryMapper.reserveSku(skuId, quantity) != 1) {
                throw new OrderBusinessException("商品库存不足，请刷新购物车后重试");
            }
            ProductSku after = requireSku(skuId);
            LocalDateTime now = LocalDateTime.now();
            skuInventoryMapper.insertReservation(SkuStockReservation.builder()
                    .orderId(order.getId())
                    .orderNumber(order.getNumber())
                    .productId(productIds.get(skuId))
                    .skuId(skuId)
                    .quantity(quantity)
                    .status(SkuStockReservation.LOCKED)
                    .createTime(now)
                    .updateTime(now)
                    .build());
            writeLog(order, productIds.get(skuId), skuId, RESERVE, 0, quantity,
                    after.getStock(), after.getStock(), after.getLockedStock() - quantity, after.getLockedStock());
        }
    }

    @Override
    @Transactional
    public void confirm(Orders order) {
        List<SkuStockReservation> reservations = skuInventoryMapper.listByOrderIdForUpdate(order.getId());
        for (SkuStockReservation reservation : reservations) {
            if (reservation.getStatus() != SkuStockReservation.LOCKED) {
                continue;
            }
            if (skuInventoryMapper.confirmSku(reservation.getSkuId(), reservation.getQuantity()) != 1) {
                throw new OrderBusinessException("库存确认失败，请稍后重试");
            }
            transition(reservation, SkuStockReservation.LOCKED, SkuStockReservation.CONFIRMED);
            ProductSku after = requireSku(reservation.getSkuId());
            writeLog(order, reservation.getProductId(), reservation.getSkuId(), CONFIRM,
                    -reservation.getQuantity(), -reservation.getQuantity(),
                    after.getStock() + reservation.getQuantity(), after.getStock(),
                    after.getLockedStock() + reservation.getQuantity(), after.getLockedStock());
        }
    }

    @Override
    @Transactional
    public void release(Orders order) {
        List<SkuStockReservation> reservations = skuInventoryMapper.listByOrderIdForUpdate(order.getId());
        for (SkuStockReservation reservation : reservations) {
            if (reservation.getStatus() == SkuStockReservation.RELEASED) {
                continue;
            }
            if (reservation.getStatus() == SkuStockReservation.LOCKED) {
                if (skuInventoryMapper.releaseLockedSku(reservation.getSkuId(), reservation.getQuantity()) != 1) {
                    throw new OrderBusinessException("库存释放失败，请稍后重试");
                }
                transition(reservation, SkuStockReservation.LOCKED, SkuStockReservation.RELEASED);
                ProductSku after = requireSku(reservation.getSkuId());
                writeLog(order, reservation.getProductId(), reservation.getSkuId(), CANCEL_RELEASE,
                        0, -reservation.getQuantity(), after.getStock(), after.getStock(),
                        after.getLockedStock() + reservation.getQuantity(), after.getLockedStock());
                continue;
            }
            if (reservation.getStatus() == SkuStockReservation.CONFIRMED) {
                if (skuInventoryMapper.restoreConfirmedSku(reservation.getSkuId(), reservation.getQuantity()) != 1) {
                    throw new OrderBusinessException("退款库存恢复失败，请稍后重试");
                }
                transition(reservation, SkuStockReservation.CONFIRMED, SkuStockReservation.RELEASED);
                ProductSku after = requireSku(reservation.getSkuId());
                writeLog(order, reservation.getProductId(), reservation.getSkuId(), CANCEL_RESTORE,
                        reservation.getQuantity(), 0, after.getStock() - reservation.getQuantity(),
                        after.getStock(), after.getLockedStock(), after.getLockedStock());
            }
        }
    }

    private void transition(SkuStockReservation reservation, int expectedStatus, int targetStatus) {
        if (skuInventoryMapper.transitionReservation(reservation.getId(), expectedStatus, targetStatus) != 1) {
            throw new OrderBusinessException("库存状态已变化，请稍后重试");
        }
    }

    private ProductSku requireSku(Long skuId) {
        ProductSku sku = productSkuMapper.getById(skuId);
        if (sku == null) {
            throw new OrderBusinessException("商品规格不存在");
        }
        return sku;
    }

    private void writeLog(Orders order,
                          Long productId,
                          Long skuId,
                          String changeType,
                          int stockDelta,
                          int lockedStockDelta,
                          int beforeStock,
                          int afterStock,
                          int beforeLockedStock,
                          int afterLockedStock) {
        skuInventoryMapper.insertLog(SkuStockLog.builder()
                .orderId(order.getId())
                .orderNumber(order.getNumber())
                .productId(productId)
                .skuId(skuId)
                .changeType(changeType)
                .stockDelta(stockDelta)
                .lockedStockDelta(lockedStockDelta)
                .beforeStock(beforeStock)
                .afterStock(afterStock)
                .beforeLockedStock(beforeLockedStock)
                .afterLockedStock(afterLockedStock)
                .createTime(LocalDateTime.now())
                .build());
    }
}
