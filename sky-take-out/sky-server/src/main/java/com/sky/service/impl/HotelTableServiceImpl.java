package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.HotelTableDTO;
import com.sky.dto.HotelTablePageQueryDTO;
import com.sky.entity.HotelTable;
import com.sky.exception.BaseException;
import com.sky.mapper.HotelTableMapper;
import com.sky.result.PageResult;
import com.sky.service.HotelTableService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelTableServiceImpl implements HotelTableService {

    @Autowired
    private HotelTableMapper hotelTableMapper;

    @Override
    public void save(HotelTableDTO hotelTableDTO) {
        if (hotelTableDTO.getStatus() == null) {
            hotelTableDTO.setStatus(StatusConstant.ENABLE);
        }
        HotelTable hotelTable = new HotelTable();
        BeanUtils.copyProperties(hotelTableDTO, hotelTable);
        hotelTableMapper.insert(hotelTable);
    }

    @Override
    public void update(HotelTableDTO hotelTableDTO) {
        HotelTable current = getExisting(hotelTableDTO.getId());
        HotelTable hotelTable = new HotelTable();
        BeanUtils.copyProperties(hotelTableDTO, hotelTable);
        hotelTable.setId(current.getId());
        hotelTableMapper.update(hotelTable);
    }

    @Override
    public void deleteById(Long id) {
        HotelTable current = getExisting(id);
        Integer activeCount = hotelTableMapper.countActiveDineInOrders(current.getTableNo());
        if (activeCount != null && activeCount > 0) {
            throw new BaseException("table has active dine-in orders");
        }
        hotelTableMapper.deleteById(id);
    }

    @Override
    public PageResult pageQuery(HotelTablePageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<HotelTable> page = hotelTableMapper.pageQuery(pageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<HotelTable> list(Integer status) {
        return hotelTableMapper.list(status);
    }

    @Override
    public HotelTable getById(Long id) {
        return getExisting(id);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        getExisting(id);
        hotelTableMapper.update(HotelTable.builder().id(id).status(status).build());
    }

    private HotelTable getExisting(Long id) {
        HotelTable hotelTable = hotelTableMapper.getById(id);
        if (hotelTable == null) {
            throw new BaseException("table not found");
        }
        return hotelTable;
    }
}
