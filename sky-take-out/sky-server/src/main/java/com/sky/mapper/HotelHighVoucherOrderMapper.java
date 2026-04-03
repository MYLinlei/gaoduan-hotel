package com.sky.mapper;

import com.sky.entity.HotelHighVoucherOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface HotelHighVoucherOrderMapper {

    void insert(HotelHighVoucherOrder hotelHighVoucherOrder);

    @Select("select count(*) from hotel_high_voucher_order where voucher_id = #{voucherId} and user_id = #{userId} and status in (1,2,3)")
    Integer countValidOrderByVoucherAndUser(@Param("voucherId") Long voucherId, @Param("userId") Long userId);

    @Select("select count(*) from hotel_high_voucher_order where voucher_id = #{voucherId} and user_id = #{userId} and receive_time >= #{beginOfDay} and receive_time < #{endOfDay} and status in (1,2,3)")
    Integer countDailyOrderByVoucherAndUser(@Param("voucherId") Long voucherId,
                                            @Param("userId") Long userId,
                                            @Param("beginOfDay") LocalDateTime beginOfDay,
                                            @Param("endOfDay") LocalDateTime endOfDay);
}
