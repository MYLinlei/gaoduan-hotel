package com.sky.service;

import com.sky.dto.HotelHighVoucherDTO;
import com.sky.dto.HotelHighVoucherPageQueryDTO;
import com.sky.entity.HotelHighVoucher;
import com.sky.result.PageResult;

import java.util.List;

public interface HotelHighVoucherService {

    void save(HotelHighVoucherDTO hotelHighVoucherDTO);

    void update(HotelHighVoucherDTO hotelHighVoucherDTO);

    HotelHighVoucher getById(Long id);

    PageResult pageQuery(HotelHighVoucherPageQueryDTO queryDTO);

    List<HotelHighVoucher> listEnabled(String scopeType, Long scopeId, String channelType);

    Long seckill(Long voucherId);
}
