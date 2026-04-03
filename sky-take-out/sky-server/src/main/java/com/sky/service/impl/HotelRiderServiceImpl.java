package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.HotelRiderDTO;
import com.sky.dto.HotelRiderPageQueryDTO;
import com.sky.entity.HotelRider;
import com.sky.exception.BaseException;
import com.sky.mapper.HotelRiderMapper;
import com.sky.result.PageResult;
import com.sky.service.HotelRiderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelRiderServiceImpl implements HotelRiderService {

    @Autowired
    private HotelRiderMapper hotelRiderMapper;

    @Override
    public void save(HotelRiderDTO hotelRiderDTO) {
        if (hotelRiderDTO.getStatus() == null) {
            hotelRiderDTO.setStatus(StatusConstant.ENABLE);
        }
        HotelRider hotelRider = new HotelRider();
        BeanUtils.copyProperties(hotelRiderDTO, hotelRider);
        hotelRiderMapper.insert(hotelRider);
    }

    @Override
    public void update(HotelRiderDTO hotelRiderDTO) {
        HotelRider current = getExisting(hotelRiderDTO.getId());
        HotelRider hotelRider = new HotelRider();
        BeanUtils.copyProperties(hotelRiderDTO, hotelRider);
        hotelRider.setId(current.getId());
        hotelRiderMapper.update(hotelRider);
    }

    @Override
    public void deleteById(Long id) {
        getExisting(id);
        Integer activeCount = hotelRiderMapper.countActiveDeliveryOrders(id);
        if (activeCount != null && activeCount > 0) {
            throw new BaseException("rider has active delivery orders");
        }
        hotelRiderMapper.deleteById(id);
    }

    @Override
    public PageResult pageQuery(HotelRiderPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<HotelRider> page = hotelRiderMapper.pageQuery(pageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<HotelRider> list(Integer status) {
        return hotelRiderMapper.list(status);
    }

    @Override
    public HotelRider getById(Long id) {
        return getExisting(id);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        getExisting(id);
        hotelRiderMapper.update(HotelRider.builder().id(id).status(status).build());
    }

    private HotelRider getExisting(Long id) {
        HotelRider hotelRider = hotelRiderMapper.getById(id);
        if (hotelRider == null) {
            throw new BaseException("rider not found");
        }
        return hotelRider;
    }
}
