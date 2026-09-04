package com.sky.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.Product;
import com.sky.entity.ProductSku;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.ProductMapper;
import com.sky.mapper.ProductSkuMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.HotelHighVoucherService;
import com.sky.service.SkuInventoryService;
import com.sky.service.UserOrderService;
import com.sky.vo.HotelHighVoucherOrderVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class UserOrderServiceImpl implements UserOrderService {

    private static final String SHOP_STATUS_KEY = "SHOP_STATUS";

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private HotelHighVoucherService hotelHighVoucherService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductSkuMapper productSkuMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SkuInventoryService skuInventoryService;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        ensureShopOpen();
        Long userId = BaseContext.getCurrentId();
        Integer orderType = resolveOrderType(ordersSubmitDTO.getOrderType());
        AddressBook addressBook = null;
        if (Orders.DELIVERY_ORDER.equals(orderType)) {
            addressBook = addressBookMapper.getByIdAndUserId(ordersSubmitDTO.getAddressBookId(), userId);
            if (addressBook == null) {
                throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
            }
        } else if (ordersSubmitDTO.getTableNo() == null || ordersSubmitDTO.getTableNo().trim().isEmpty()) {
            throw new OrderBusinessException("门店自提订单必须填写自提联系人");
        }

        ShoppingCart cartQuery = new ShoppingCart();
        cartQuery.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(cartQuery);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        validateCartQuantities(shoppingCartList);
        refreshProductCartItems(shoppingCartList);

        int packAmount = ordersSubmitDTO.getPackAmount() == null ? 0 : ordersSubmitDTO.getPackAmount();
        int tablewareNumber = ordersSubmitDTO.getTablewareNumber() == null ? 0 : ordersSubmitDTO.getTablewareNumber();
        Integer deliveryStatus = resolveDeliveryStatus(orderType, ordersSubmitDTO.getDeliveryStatus());
        BigDecimal originalAmount = calculateOrderAmount(shoppingCartList).add(BigDecimal.valueOf(packAmount));
        HotelHighVoucherOrderVO coupon = hotelHighVoucherService.getOwnedCoupon(ordersSubmitDTO.getCouponId(), userId, originalAmount);
        BigDecimal couponAmount = coupon == null ? BigDecimal.ZERO : coupon.getDiscountAmount();
        BigDecimal actualPayAmount = originalAmount.subtract(couponAmount);
        if (actualPayAmount.compareTo(BigDecimal.ZERO) < 0) {
            actualPayAmount = BigDecimal.ZERO;
        }

        Orders order = Orders.builder()
                .number(generateOrderNumber(userId))
                .status(Orders.PENDING_PAYMENT)
                .userId(userId)
                .addressBookId(addressBook == null ? null : addressBook.getId())
                .orderTime(LocalDateTime.now())
                .payMethod(ordersSubmitDTO.getPayMethod())
                .payStatus(Orders.UN_PAID)
                .orderType(orderType)
                .tableNo(normalizeTableNo(ordersSubmitDTO.getTableNo()))
                .couponId(coupon == null ? null : coupon.getId())
                .couponAmount(couponAmount)
                .actualPayAmount(actualPayAmount)
                .amount(originalAmount)
                .remark(ordersSubmitDTO.getRemark())
                .userName(addressBook == null ? "门店自提顾客" : addressBook.getConsignee())
                .phone(addressBook == null ? null : addressBook.getPhone())
                .address(addressBook == null ? null : buildFullAddress(addressBook))
                .consignee(addressBook == null ? null : addressBook.getConsignee())
                .estimatedDeliveryTime(Orders.DELIVERY_ORDER.equals(orderType) ? ordersSubmitDTO.getEstimatedDeliveryTime() : null)
                .deliveryStatus(deliveryStatus)
                .packAmount(packAmount)
                .tablewareNumber(tablewareNumber)
                .tablewareStatus(ordersSubmitDTO.getTablewareStatus())
                .build();
        ordersMapper.insert(order);
        skuInventoryService.reserve(order, shoppingCartList);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .name(shoppingCart.getName())
                    .orderId(order.getId())
                    .dishId(shoppingCart.getDishId())
                    .productId(shoppingCart.getProductId())
                    .skuId(shoppingCart.getSkuId())
                    .setmealId(shoppingCart.getSetmealId())
                    .dishFlavor(shoppingCart.getDishFlavor())
                    .skuSpec(shoppingCart.getSkuSpec())
                    .number(shoppingCart.getNumber())
                    .amount(shoppingCart.getAmount())
                    .image(shoppingCart.getImage())
                    .unit(shoppingCart.getUnit())
                    .build();
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);
        shoppingCartMapper.deleteByUserId(userId);
        if (coupon != null) {
            hotelHighVoucherService.useCoupon(coupon.getId(), order.getId(), userId);
        }

        return OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(actualPayAmount)
                .orderTime(order.getOrderTime())
                .build();
    }

    @Override
    @Transactional
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        Orders order = ordersMapper.getByNumberForUpdate(ordersPaymentDTO.getOrderNumber());
        if (order == null || !BaseContext.getCurrentId().equals(order.getUserId())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.PENDING_PAYMENT.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        skuInventoryService.confirm(order);

        Orders updateOrder = Orders.builder()
                .id(order.getId())
                .checkoutTime(LocalDateTime.now())
                .payMethod(ordersPaymentDTO.getPayMethod())
                .status(nextStatusAfterPayment(order))
                .payStatus(Orders.PAID)
                .actualPayAmount(order.getActualPayAmount() == null ? order.getAmount() : order.getActualPayAmount())
                .build();
        ordersMapper.update(updateOrder);

        return OrderPaymentVO.builder()
                .timeStamp(String.valueOf(System.currentTimeMillis()))
                .nonceStr(order.getNumber())
                .packageStr("mock_prepay_id=" + order.getNumber())
                .signType("MOCK")
                .paySign("mock-sign")
                .build();
    }

    @Override
    public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        List<Orders> ordersList = ordersMapper.pageQuery(ordersPageQueryDTO);
        long total = ordersMapper.countPageQuery(ordersPageQueryDTO);

        List<OrderVO> records = new ArrayList<>();
        for (Orders orders : ordersList) {
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDetailList(orderDetailList);
            orderVO.setOrderDishes(buildOrderDishes(orderDetailList));
            records.add(orderVO);
        }
        return new PageResult(total, records);
    }

    @Override
    public OrderVO details(Long id) {
        Orders order = requireOwnedOrder(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        orderVO.setOrderDishes(buildOrderDishes(orderDetailList));
        return orderVO;
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Orders order = requireOwnedOrderForUpdate(id);
        if (Orders.CANCELLED.equals(order.getStatus()) || Orders.COMPLETED.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        skuInventoryService.release(order);

        Orders updateOrder = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .cancelReason("用户取消")
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.PAID.equals(order.getPayStatus()) ? Orders.REFUND : order.getPayStatus())
                .build();
        ordersMapper.update(updateOrder);
    }

    @Override
    @Transactional
    public void repetition(Long id) {
        Orders order = requireOwnedOrder(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(order.getId());
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart query = new ShoppingCart();
            query.setUserId(order.getUserId());
            query.setProductId(orderDetail.getProductId());
            query.setSkuId(orderDetail.getSkuId());
            query.setDishId(orderDetail.getDishId());
            query.setSetmealId(orderDetail.getSetmealId());
            query.setDishFlavor(orderDetail.getDishFlavor());

            List<ShoppingCart> carts = shoppingCartMapper.list(query);
            if (!carts.isEmpty()) {
                ShoppingCart current = carts.get(0);
                current.setNumber(current.getNumber() + orderDetail.getNumber());
                shoppingCartMapper.updateNumberById(current);
                continue;
            }

            query.setName(orderDetail.getName());
            query.setSkuSpec(orderDetail.getSkuSpec());
            query.setNumber(orderDetail.getNumber());
            query.setAmount(orderDetail.getAmount());
            query.setImage(orderDetail.getImage());
            query.setUnit(orderDetail.getUnit());
            query.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(query);
        }
    }

    private Orders requireOwnedOrder(Long id) {
        Orders order = ordersMapper.getById(id);
        if (order == null || !BaseContext.getCurrentId().equals(order.getUserId())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return order;
    }

    private Orders requireOwnedOrderForUpdate(Long id) {
        Orders order = ordersMapper.getByIdForUpdate(id);
        if (order == null || !BaseContext.getCurrentId().equals(order.getUserId())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return order;
    }

    private String generateOrderNumber(Long userId) {
        return System.currentTimeMillis() + String.format("%04d", userId % 10000);
    }

    private Integer resolveOrderType(Integer orderType) {
        return orderType == null ? Orders.DELIVERY_ORDER : orderType;
    }

    private Integer nextStatusAfterPayment(Orders order) {
        return Orders.DINE_IN_ORDER.equals(order.getOrderType())
                ? Orders.DINE_IN_TO_BE_PREPARED
                : Orders.TO_BE_CONFIRMED;
    }

    private Integer resolveDeliveryStatus(Integer orderType, Integer deliveryStatus) {
        if (deliveryStatus != null) {
            return deliveryStatus;
        }
        return Orders.DINE_IN_ORDER.equals(orderType) ? 0 : 1;
    }

    private String normalizeTableNo(String tableNo) {
        return tableNo == null ? null : tableNo.trim();
    }

    private BigDecimal calculateOrderAmount(List<ShoppingCart> shoppingCartList) {
        BigDecimal total = BigDecimal.ZERO;
        for (ShoppingCart shoppingCart : shoppingCartList) {
            BigDecimal itemTotal = shoppingCart.getAmount().multiply(BigDecimal.valueOf(shoppingCart.getNumber()));
            total = total.add(itemTotal);
        }
        return total;
    }

    private void validateCartQuantities(List<ShoppingCart> cartItems) {
        for (ShoppingCart cart : cartItems) {
            if (cart.getNumber() == null || cart.getNumber() < 1 || cart.getNumber() > 99) {
                throw new ShoppingCartBusinessException("购物车商品数量必须在 1 到 99 件之间");
            }
        }
    }

    private void refreshProductCartItems(List<ShoppingCart> cartItems) {
        for (ShoppingCart cart : cartItems) {
            if (cart.getProductId() == null) {
                continue;
            }
            Product product = productMapper.getPurchasableById(cart.getProductId());
            if (product == null) {
                throw new ShoppingCartBusinessException(cart.getName() + " 已下架，请从购物车移除");
            }
            ProductSku sku = cart.getSkuId() == null ? null : productSkuMapper.getById(cart.getSkuId());
            if (sku == null || !product.getId().equals(sku.getProductId()) || sku.getStatus() == null || sku.getStatus() == 0) {
                throw new ShoppingCartBusinessException(cart.getName() + " 的当前规格已停售");
            }
            int availableStock = Math.max((sku.getStock() == null ? 0 : sku.getStock())
                    - (sku.getLockedStock() == null ? 0 : sku.getLockedStock()), 0);
            if (cart.getNumber() == null || cart.getNumber() < 1 || cart.getNumber() > availableStock) {
                throw new ShoppingCartBusinessException(cart.getName() + " 的当前规格库存不足");
            }
            cart.setDishId(product.getLegacyDishId());
            cart.setName(product.getName());
            cart.setSkuSpec(formatSkuSpec(sku));
            cart.setAmount(sku.getSalePrice());
            cart.setImage(product.getMainImage());
            cart.setUnit(product.getUnit());
        }
    }

    private String formatSkuSpec(ProductSku sku) {
        try {
            Map<String, Object> specs = objectMapper.readValue(sku.getSpecJson(),
                    new TypeReference<LinkedHashMap<String, Object>>() { });
            StringJoiner joiner = new StringJoiner("；");
            specs.forEach((key, value) -> joiner.add(key + "：" + value));
            return joiner.length() == 0 ? sku.getSkuName() : joiner.toString();
        } catch (Exception ignored) {
            return sku.getSkuName();
        }
    }

    private String buildFullAddress(AddressBook addressBook) {
        StringBuilder builder = new StringBuilder();
        appendIfPresent(builder, addressBook.getProvinceName());
        appendIfPresent(builder, addressBook.getCityName());
        appendIfPresent(builder, addressBook.getDistrictName());
        appendIfPresent(builder, addressBook.getDetail());
        return builder.toString();
    }

    private void appendIfPresent(StringBuilder builder, String part) {
        if (part != null && !part.isEmpty()) {
            builder.append(part);
        }
    }

    private String buildOrderDishes(List<OrderDetail> orderDetailList) {
        if (orderDetailList == null || orderDetailList.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(";");
        for (OrderDetail orderDetail : orderDetailList) {
            joiner.add(orderDetail.getName() + "*" + orderDetail.getNumber());
        }
        return joiner.toString();
    }

    private void ensureShopOpen() {
        Object rawStatus = redisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        if (rawStatus != null && "0".equals(String.valueOf(rawStatus))) {
            throw new OrderBusinessException("当前门店暂停营业，暂不支持下单");
        }
    }
}
