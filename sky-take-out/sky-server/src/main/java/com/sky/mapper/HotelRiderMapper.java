package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.HotelRiderPageQueryDTO;
import com.sky.entity.HotelRider;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface HotelRiderMapper {

    @Insert("insert into hotel_rider (name, phone, id_card_no, vehicle_type, vehicle_no, status, delivery_zone_code, sort, remark, create_time, update_time) " +
            "values (#{name}, #{phone}, #{idCardNo}, #{vehicleType}, #{vehicleNo}, #{status}, #{deliveryZoneCode}, #{sort}, #{remark}, now(), now())")
    void insert(HotelRider hotelRider);

    Page<HotelRider> pageQuery(HotelRiderPageQueryDTO pageQueryDTO);

    @Select("select * from hotel_rider where id = #{id}")
    HotelRider getById(Long id);

    @Update({
            "<script>",
            "update hotel_rider",
            "<set>",
            "<if test='name != null'>name = #{name},</if>",
            "<if test='phone != null'>phone = #{phone},</if>",
            "<if test='idCardNo != null'>id_card_no = #{idCardNo},</if>",
            "<if test='vehicleType != null'>vehicle_type = #{vehicleType},</if>",
            "<if test='vehicleNo != null'>vehicle_no = #{vehicleNo},</if>",
            "<if test='status != null'>status = #{status},</if>",
            "<if test='deliveryZoneCode != null'>delivery_zone_code = #{deliveryZoneCode},</if>",
            "<if test='sort != null'>sort = #{sort},</if>",
            "<if test='remark != null'>remark = #{remark},</if>",
            "update_time = now()",
            "</set>",
            "where id = #{id}",
            "</script>"
    })
    void update(HotelRider hotelRider);

    @Delete("delete from hotel_rider where id = #{id}")
    void deleteById(Long id);

    @Select({
            "<script>",
            "select * from hotel_rider",
            "<where>",
            "<if test='status != null'>status = #{status}</if>",
            "</where>",
            "order by sort asc, id desc",
            "</script>"
    })
    List<HotelRider> list(@Param("status") Integer status);

    @Select("select count(*) from orders where rider_id = #{riderId} and status in (2,3,4)")
    Integer countActiveDeliveryOrders(@Param("riderId") Long riderId);
}
