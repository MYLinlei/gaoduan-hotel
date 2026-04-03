package com.sky.service;

import com.sky.dto.HotelRiderDTO;
import com.sky.dto.HotelRiderPageQueryDTO;
import com.sky.entity.HotelRider;
import com.sky.result.PageResult;

import java.util.List;

public interface HotelRiderService {

    void save(HotelRiderDTO hotelRiderDTO);

    void update(HotelRiderDTO hotelRiderDTO);

    void deleteById(Long id);

    PageResult pageQuery(HotelRiderPageQueryDTO pageQueryDTO);

    List<HotelRider> list(Integer status);

    HotelRider getById(Long id);

    void startOrStop(Integer status, Long id);
}
