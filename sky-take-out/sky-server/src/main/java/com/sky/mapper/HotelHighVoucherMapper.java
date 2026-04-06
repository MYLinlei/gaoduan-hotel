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

    @Select({
            "<script>",
            "select count(*) from hotel_high_voucher",
            "<where>",
            "  <if test='name != null and name != \"\"'>",
            "    and name like concat('%', #{name}, '%')",
            "  </if>",
            "  <if test='channelType != null and channelType != \"\"'>",
            "    and channel_type = #{channelType}",
            "  </if>",
            "  <if test='status != null'>",
            "    and status = #{status}",
            "  </if>",
            "</where>",
            "</script>"
    })
    Long countByCondition(@Param("name") String name,
                          @Param("channelType") String channelType,
                          @Param("status") Integer status);

    @Select({
            "<script>",
            "select ifnull(sum(available_stock), 0) from hotel_high_voucher",
            "<where>",
            "  <if test='name != null and name != \"\"'>",
            "    and name like concat('%', #{name}, '%')",
            "  </if>",
            "  <if test='channelType != null and channelType != \"\"'>",
            "    and channel_type = #{channelType}",
            "  </if>",
            "  <if test='status != null'>",
            "    and status = #{status}",
            "  </if>",
            "</where>",
            "</script>"
    })
    Integer sumAvailableStockByCondition(@Param("name") String name,
                                         @Param("channelType") String channelType,
                                         @Param("status") Integer status);
}
