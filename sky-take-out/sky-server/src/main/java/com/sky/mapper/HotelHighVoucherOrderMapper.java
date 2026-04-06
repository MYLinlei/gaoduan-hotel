package com.sky.mapper;

import com.sky.entity.HotelHighVoucherOrder;
import com.sky.vo.HotelHighVoucherOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    List<HotelHighVoucherOrderVO> listByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    HotelHighVoucherOrder getById(@Param("id") Long id);

    void update(HotelHighVoucherOrder hotelHighVoucherOrder);

    @Select("select count(*) from hotel_high_voucher_order where voucher_id = #{voucherId}")
    Integer countByVoucherId(@Param("voucherId") Long voucherId);

    @Select("select count(*) from hotel_high_voucher_order where voucher_id = #{voucherId} and status = #{status}")
    Integer countByVoucherIdAndStatus(@Param("voucherId") Long voucherId, @Param("status") Integer status);

    @Select("select count(*) from hotel_high_voucher_order where user_id = #{userId}")
    Integer countByUserId(@Param("userId") Long userId);

    @Select("select count(*) from hotel_high_voucher_order where user_id = #{userId} and status = #{status}")
    Integer countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Select({
            "<script>",
            "select",
            "  hvo.id as id,",
            "  hvo.order_no as orderNo,",
            "  hvo.status as status,",
            "  hvo.receive_time as receiveTime,",
            "  hvo.use_time as useTime,",
            "  hvo.expire_time as expireTime,",
            "  hvo.source_type as sourceType,",
            "  u.id as userId,",
            "  u.phone as phone,",
            "  u.name as nickname",
            "from hotel_high_voucher_order hvo",
            "left join user u on u.id = hvo.user_id",
            "where hvo.voucher_id = #{voucherId}",
            "<if test='status != null'>",
            "  and hvo.status = #{status}",
            "</if>",
            "<if test='phone != null and phone != \"\"'>",
            "  and u.phone like concat('%', #{phone}, '%')",
            "</if>",
            "order by hvo.receive_time desc, hvo.id desc",
            "</script>"
    })
    List<Map<String, Object>> pageReceiveRecordsByVoucherId(@Param("voucherId") Long voucherId,
                                                            @Param("status") Integer status,
                                                            @Param("phone") String phone);
}
