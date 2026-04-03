package com.sky.service;

import com.sky.dto.HotelTableDTO;
import com.sky.dto.HotelTablePageQueryDTO;
import com.sky.entity.HotelTable;
import com.sky.result.PageResult;

import java.util.List;

public interface HotelTableService {

    void save(HotelTableDTO hotelTableDTO);

    void update(HotelTableDTO hotelTableDTO);

    void deleteById(Long id);

    PageResult pageQuery(HotelTablePageQueryDTO pageQueryDTO);

    List<HotelTable> list(Integer status);

    HotelTable getById(Long id);

    void startOrStop(Integer status, Long id);
}
