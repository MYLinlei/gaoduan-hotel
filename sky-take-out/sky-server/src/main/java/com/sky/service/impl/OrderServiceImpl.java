package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        List<Orders> ordersList = ordersMapper.pageQuery(ordersPageQueryDTO);
        long total = ordersMapper.countPageQuery(ordersPageQueryDTO);

        List<OrderVO> records = new ArrayList<>();
        for (Orders orders : ordersList) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDishes(buildOrderDishes(orderDetailMapper.getByOrderId(orders.getId())));
            records.add(orderVO);
        }
        return new PageResult(total, records);
    }

    @Override
    public OrderVO details(Long orderId) {
        Orders orders = getExistingOrder(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        orderVO.setOrderDishes(buildOrderDishes(orderDetailList));
        return orderVO;
    }

    @Override
    @Transactional
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = getExistingOrder(ordersConfirmDTO.getId());
        if (!Orders.TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders updateOrder = Orders.builder()
                .id(orders.getId())
                .status(Orders.CONFIRMED)
                .build();
        ordersMapper.update(updateOrder);
    }

    @Override
    @Transactional
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = getExistingOrder(ordersRejectionDTO.getId());
        if (!Orders.TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders updateOrder = Orders.builder()
                .id(orders.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.PAID.equals(orders.getPayStatus()) ? Orders.REFUND : orders.getPayStatus())
                .build();
        ordersMapper.update(updateOrder);
    }

    @Override
    @Transactional
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = getExistingOrder(ordersCancelDTO.getId());
        if (Orders.CANCELLED.equals(orders.getStatus()) || Orders.COMPLETED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders updateOrder = Orders.builder()
                .id(orders.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.PAID.equals(orders.getPayStatus()) ? Orders.REFUND : orders.getPayStatus())
                .build();
        ordersMapper.update(updateOrder);
    }

    @Override
    @Transactional
    public void delivery(Long id) {
        Orders orders = getExistingOrder(id);
        if (!Orders.CONFIRMED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders updateOrder = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        ordersMapper.update(updateOrder);
    }

    @Override
    @Transactional
    public void complete(Long id) {
        Orders orders = getExistingOrder(id);
        if (!Orders.DELIVERY_IN_PROGRESS.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders updateOrder = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        ordersMapper.update(updateOrder);
    }

    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(defaultZero(ordersMapper.countByCondition(Orders.TO_BE_CONFIRMED, null, null)));
        orderStatisticsVO.setConfirmed(defaultZero(ordersMapper.countByCondition(Orders.CONFIRMED, null, null)));
        orderStatisticsVO.setDeliveryInProgress(defaultZero(ordersMapper.countByCondition(Orders.DELIVERY_IN_PROGRESS, null, null)));
        return orderStatisticsVO;
    }

    private Orders getExistingOrder(Long orderId) {
        Orders orders = ordersMapper.getById(orderId);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return orders;
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

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
