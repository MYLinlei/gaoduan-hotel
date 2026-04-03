package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.HotelHighVoucherPageQueryDTO;
import com.sky.entity.HotelHighVoucher;
import com.sky.enumeration.OperationType;
import com.sky.vo.HotelHighVoucherVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HotelHighVoucherMapper {

    @AutoFill(OperationType.INSERT)
    void insert(HotelHighVoucher hotelHighVoucher);

    @AutoFill(OperationType.UPDATE)
    void update(HotelHighVoucher hotelHighVoucher);

    Page<HotelHighVoucherVO> pageQuery(HotelHighVoucherPageQueryDTO queryDTO);

    @Select("select * from hotel_high_voucher where id = #{id}")
    HotelHighVoucher getById(Long id);

    List<HotelHighVoucher> listEnabled(@Param("scopeType") String scopeType,
                                       @Param("scopeId") Long scopeId,
                                       @Param("channelType") String channelType);

    int decreaseAvailableStock(@Param("id") Long id);
}
