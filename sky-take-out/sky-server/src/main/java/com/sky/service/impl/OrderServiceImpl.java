package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.dto.OrdersAssignRiderDTO;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.HotelRider;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.exception.BaseException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.HotelRiderMapper;
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

    @Autowired
    private HotelRiderMapper hotelRiderMapper;

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        List<Orders> ordersList = ordersMapper.pageQuery(ordersPageQueryDTO);
        long total = ordersMapper.countPageQuery(ordersPageQueryDTO);

        List<OrderVO> records = new ArrayList<>();
        for (Orders orders : ordersList) {
            records.add(buildOrderVO(orders, false));
        }
        return new PageResult(total, records);
    }

    @Override
    public OrderVO details(Long orderId) {
        Orders orders = getExistingOrder(orderId);
        return buildOrderVO(orders, true);
    }

    @Override
    @Transactional
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = getExistingOrder(ordersConfirmDTO.getId());
        if (!canConfirm(orders)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        ordersMapper.update(Orders.builder()
                .id(orders.getId())
                .status(nextConfirmStatus(orders))
                .build());
    }

    @Override
    @Transactional
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = getExistingOrder(ordersRejectionDTO.getId());
        if (!canReject(orders)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        ordersMapper.update(Orders.builder()
                .id(orders.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.PAID.equals(orders.getPayStatus()) ? Orders.REFUND : orders.getPayStatus())
                .build());
    }

    @Override
    @Transactional
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = getExistingOrder(ordersCancelDTO.getId());
        if (Orders.CANCELLED.equals(orders.getStatus()) || Orders.COMPLETED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        ordersMapper.update(Orders.builder()
                .id(orders.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.PAID.equals(orders.getPayStatus()) ? Orders.REFUND : orders.getPayStatus())
                .build());
    }

    @Override
    @Transactional
    public void assignRider(OrdersAssignRiderDTO ordersAssignRiderDTO) {
        Orders orders = getExistingOrder(ordersAssignRiderDTO.getId());
        if (!Orders.DELIVERY_ORDER.equals(orders.getOrderType())) {
            throw new BaseException("dine-in order does not need rider assignment");
        }
        if (!Orders.CONFIRMED.equals(orders.getStatus()) && !Orders.DELIVERY_IN_PROGRESS.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        HotelRider hotelRider = hotelRiderMapper.getById(ordersAssignRiderDTO.getRiderId());
        if (hotelRider == null) {
            throw new BaseException("rider not found");
        }
        if (hotelRider.getStatus() == null || hotelRider.getStatus() != 1) {
            throw new BaseException("rider is disabled");
        }

        ordersMapper.update(Orders.builder()
                .id(orders.getId())
                .riderId(hotelRider.getId())
                .build());
    }

    @Override
    @Transactional
    public void delivery(Long id) {
        Orders orders = getExistingOrder(id);
        if (!canDispatchOrServe(orders)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        ordersMapper.update(Orders.builder()
                .id(id)
                .status(nextDispatchOrServeStatus(orders))
                .build());
    }

    @Override
    @Transactional
    public void complete(Long id) {
        Orders orders = getExistingOrder(id);
        if (!canComplete(orders)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        ordersMapper.update(Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build());
    }

    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(sumStatuses(Orders.TO_BE_CONFIRMED, Orders.DINE_IN_TO_BE_PREPARED));
        orderStatisticsVO.setConfirmed(sumStatuses(Orders.CONFIRMED, Orders.DINE_IN_IN_PROGRESS));
        orderStatisticsVO.setDeliveryInProgress(sumStatuses(Orders.DELIVERY_IN_PROGRESS, Orders.DINE_IN_SERVED));
        return orderStatisticsVO;
    }

    private Orders getExistingOrder(Long orderId) {
        Orders orders = ordersMapper.getById(orderId);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return orders;
    }

    private OrderVO buildOrderVO(Orders orders, boolean includeDetailList) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDishes(buildOrderDishes(orderDetailList));
        if (includeDetailList) {
            orderVO.setOrderDetailList(orderDetailList);
        }
        if (orders.getRiderId() != null) {
            HotelRider hotelRider = hotelRiderMapper.getById(orders.getRiderId());
            if (hotelRider != null) {
                orderVO.setRiderName(hotelRider.getName());
            }
        }
        return orderVO;
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

    private boolean canConfirm(Orders orders) {
        if (Orders.DINE_IN_ORDER.equals(orders.getOrderType())) {
            return Orders.DINE_IN_TO_BE_PREPARED.equals(orders.getStatus());
        }
        return Orders.TO_BE_CONFIRMED.equals(orders.getStatus());
    }

    private Integer nextConfirmStatus(Orders orders) {
        return Orders.DINE_IN_ORDER.equals(orders.getOrderType())
                ? Orders.DINE_IN_IN_PROGRESS
                : Orders.CONFIRMED;
    }

    private boolean canReject(Orders orders) {
        return canConfirm(orders);
    }

    private boolean canDispatchOrServe(Orders orders) {
        if (Orders.DINE_IN_ORDER.equals(orders.getOrderType())) {
            return Orders.DINE_IN_IN_PROGRESS.equals(orders.getStatus());
        }
        return Orders.CONFIRMED.equals(orders.getStatus()) && orders.getRiderId() != null;
    }

    private Integer nextDispatchOrServeStatus(Orders orders) {
        return Orders.DINE_IN_ORDER.equals(orders.getOrderType())
                ? Orders.DINE_IN_SERVED
                : Orders.DELIVERY_IN_PROGRESS;
    }

    private boolean canComplete(Orders orders) {
        if (Orders.DINE_IN_ORDER.equals(orders.getOrderType())) {
            return Orders.DINE_IN_SERVED.equals(orders.getStatus());
        }
        return Orders.DELIVERY_IN_PROGRESS.equals(orders.getStatus());
    }

    private Integer sumStatuses(Integer... statuses) {
        int total = 0;
        for (Integer status : statuses) {
            total += defaultZero(ordersMapper.countByCondition(status, null, null));
        }
        return total;
    }
}
