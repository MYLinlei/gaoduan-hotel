package com.sky.service;

import com.sky.dto.HotelHighVoucherDTO;
import com.sky.dto.HotelHighVoucherPageQueryDTO;
import com.sky.entity.HotelHighVoucher;
import com.sky.result.PageResult;
import com.sky.vo.HotelHighVoucherOrderVO;

import java.math.BigDecimal;
import java.util.List;

public interface HotelHighVoucherService {

    void save(HotelHighVoucherDTO hotelHighVoucherDTO);

    void update(HotelHighVoucherDTO hotelHighVoucherDTO);

    void updateStatus(Long id, Integer status);

    HotelHighVoucher getById(Long id);

    PageResult pageQuery(HotelHighVoucherPageQueryDTO queryDTO);

    List<HotelHighVoucher> listEnabled(String scopeType, Long scopeId, String channelType);

    Long seckill(Long voucherId);

    List<HotelHighVoucherOrderVO> myCoupons(Integer status);

    HotelHighVoucherOrderVO getOwnedCoupon(Long voucherOrderId, Long userId, BigDecimal orderAmount);

    void useCoupon(Long voucherOrderId, Long orderId, Long userId);
}
