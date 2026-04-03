package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private ReportService reportService;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public BusinessDataVO getBusinessData() {
        LocalDate today = LocalDate.now();
        return reportService.getBusinessData(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    @Override
    public OrderOverViewVO getOrderOverView() {
        LocalDateTime begin = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        return OrderOverViewVO.builder()
                .waitingOrders(defaultZero(ordersMapper.countByCondition(Orders.TO_BE_CONFIRMED, begin, end)))
                .deliveredOrders(defaultZero(ordersMapper.countByCondition(Orders.CONFIRMED, begin, end)))
                .completedOrders(defaultZero(ordersMapper.countByCondition(Orders.COMPLETED, begin, end)))
                .cancelledOrders(defaultZero(ordersMapper.countByCondition(Orders.CANCELLED, begin, end)))
                .allOrders(defaultZero(ordersMapper.countByCondition(null, begin, end)))
                .build();
    }

    @Override
    public DishOverViewVO getDishOverView() {
        return DishOverViewVO.builder()
                .sold(defaultZero(dishMapper.countByStatus(StatusConstant.ENABLE)))
                .discontinued(defaultZero(dishMapper.countByStatus(StatusConstant.DISABLE)))
                .build();
    }

    @Override
    public SetmealOverViewVO getSetmealOverView() {
        return SetmealOverViewVO.builder()
                .sold(defaultZero(setmealMapper.countByStatus(StatusConstant.ENABLE)))
                .discontinued(defaultZero(setmealMapper.countByStatus(StatusConstant.DISABLE)))
                .build();
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
