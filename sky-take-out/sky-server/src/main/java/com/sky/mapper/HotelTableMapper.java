package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.HotelTablePageQueryDTO;
import com.sky.entity.HotelTable;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface HotelTableMapper {

    @Insert("insert into hotel_table (table_no, area_name, seat_count, status, sort, remark, create_time, update_time) " +
            "values (#{tableNo}, #{areaName}, #{seatCount}, #{status}, #{sort}, #{remark}, now(), now())")
    void insert(HotelTable hotelTable);

    Page<HotelTable> pageQuery(HotelTablePageQueryDTO pageQueryDTO);

    @Select("select * from hotel_table where id = #{id}")
    HotelTable getById(Long id);

    @Update({
            "<script>",
            "update hotel_table",
            "<set>",
            "<if test='tableNo != null'>table_no = #{tableNo},</if>",
            "<if test='areaName != null'>area_name = #{areaName},</if>",
            "<if test='seatCount != null'>seat_count = #{seatCount},</if>",
            "<if test='status != null'>status = #{status},</if>",
            "<if test='sort != null'>sort = #{sort},</if>",
            "<if test='remark != null'>remark = #{remark},</if>",
            "update_time = now()",
            "</set>",
            "where id = #{id}",
            "</script>"
    })
    void update(HotelTable hotelTable);

    @Delete("delete from hotel_table where id = #{id}")
    void deleteById(Long id);

    @Select({
            "<script>",
            "select * from hotel_table",
            "<where>",
            "<if test='status != null'>status = #{status}</if>",
            "</where>",
            "order by sort asc, id desc",
            "</script>"
    })
    List<HotelTable> list(@Param("status") Integer status);

    @Select("select count(*) from orders where table_no = #{tableNo} and status in (7,8)")
    Integer countActiveDineInOrders(@Param("tableNo") String tableNo);
}
