package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.UserOrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Service
public class UserOrderServiceImpl implements UserOrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        Integer orderType = resolveOrderType(ordersSubmitDTO.getOrderType());
        AddressBook addressBook = null;
        if (Orders.DELIVERY_ORDER.equals(orderType)) {
            addressBook = addressBookMapper.getByIdAndUserId(ordersSubmitDTO.getAddressBookId(), userId);
            if (addressBook == null) {
                throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
            }
        } else if (ordersSubmitDTO.getTableNo() == null || ordersSubmitDTO.getTableNo().trim().isEmpty()) {
            throw new OrderBusinessException("堂食订单必须选择桌号");
        }

        ShoppingCart cartQuery = new ShoppingCart();
        cartQuery.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(cartQuery);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        int packAmount = ordersSubmitDTO.getPackAmount() == null ? 0 : ordersSubmitDTO.getPackAmount();
        int tablewareNumber = ordersSubmitDTO.getTablewareNumber() == null ? 0 : ordersSubmitDTO.getTablewareNumber();
        BigDecimal orderAmount = calculateOrderAmount(shoppingCartList).add(BigDecimal.valueOf(packAmount));
        LocalDateTime now = LocalDateTime.now();

        Orders order = Orders.builder()
                .number(generateOrderNumber(userId))
                .status(Orders.PENDING_PAYMENT)
                .userId(userId)
                .addressBookId(addressBook == null ? null : addressBook.getId())
                .orderTime(now)
                .payMethod(ordersSubmitDTO.getPayMethod())
                .payStatus(Orders.UN_PAID)
                .orderType(orderType)
                .tableNo(normalizeTableNo(ordersSubmitDTO.getTableNo()))
                .couponAmount(BigDecimal.ZERO)
                .actualPayAmount(orderAmount)
                .amount(orderAmount)
                .remark(ordersSubmitDTO.getRemark())
                .userName(addressBook == null ? "堂食顾客" : addressBook.getConsignee())
                .phone(addressBook == null ? null : addressBook.getPhone())
                .address(addressBook == null ? null : buildFullAddress(addressBook))
                .consignee(addressBook == null ? null : addressBook.getConsignee())
                .estimatedDeliveryTime(Orders.DELIVERY_ORDER.equals(orderType) ? ordersSubmitDTO.getEstimatedDeliveryTime() : null)
                .deliveryStatus(Orders.DELIVERY_ORDER.equals(orderType) ? ordersSubmitDTO.getDeliveryStatus() : null)
                .packAmount(packAmount)
                .tablewareNumber(tablewareNumber)
                .tablewareStatus(ordersSubmitDTO.getTablewareStatus())
                .build();
        ordersMapper.insert(order);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .name(shoppingCart.getName())
                    .orderId(order.getId())
                    .dishId(shoppingCart.getDishId())
                    .setmealId(shoppingCart.getSetmealId())
                    .dishFlavor(shoppingCart.getDishFlavor())
                    .number(shoppingCart.getNumber())
                    .amount(shoppingCart.getAmount())
                    .image(shoppingCart.getImage())
                    .build();
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);
        shoppingCartMapper.deleteByUserId(userId);

        return OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(orderAmount)
                .orderTime(order.getOrderTime())
                .build();
    }

    @Override
    @Transactional
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        Orders order = ordersMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        if (order == null || !BaseContext.getCurrentId().equals(order.getUserId())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.PENDING_PAYMENT.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

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
        Orders order = requireOwnedOrder(id);
        if (Orders.CANCELLED.equals(order.getStatus()) || Orders.COMPLETED.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

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
            query.setNumber(orderDetail.getNumber());
            query.setAmount(orderDetail.getAmount());
            query.setImage(orderDetail.getImage());
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
}
