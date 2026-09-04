package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.service.OrderTimeoutService;
import com.sky.service.SkuInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderTimeoutServiceImpl implements OrderTimeoutService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private SkuInventoryService skuInventoryService;

    @Override
    @Transactional
    public void closeExpiredOrder(Long orderId) {
        Orders order = ordersMapper.getByIdForUpdate(orderId);
        if (order == null
                || !Orders.PENDING_PAYMENT.equals(order.getStatus())
                || !Orders.UN_PAID.equals(order.getPayStatus())) {
            return;
        }

        skuInventoryService.release(order);
        ordersMapper.update(Orders.builder()
                .id(order.getId())
                .status(Orders.CANCELLED)
                .cancelReason("支付超时自动关闭")
                .cancelTime(LocalDateTime.now())
                .build());
    }
}
