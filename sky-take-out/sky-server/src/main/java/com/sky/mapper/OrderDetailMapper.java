package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderDetailMapper {

    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);

    @Select({
            "<script>",
            "select od.name as name, ifnull(sum(od.number), 0) as number",
            "from order_detail od",
            "inner join orders o on o.id = od.order_id",
            "<where>",
            "  o.status = 5",
            "  and od.dish_id is not null",
            "  <if test='begin != null'>",
            "    and o.order_time &gt;= #{begin}",
            "  </if>",
            "  <if test='end != null'>",
            "    and o.order_time &lt; #{end}",
            "  </if>",
            "</where>",
            "group by od.name",
            "order by number desc",
            "limit #{limit}",
            "</script>"
    })
    List<GoodsSalesDTO> getTopSellingDishes(@Param("begin") LocalDateTime begin,
                                            @Param("end") LocalDateTime end,
                                            @Param("limit") Integer limit);

    @Select({
            "<script>",
            "select od.name as name, ifnull(sum(od.number), 0) as number",
            "from order_detail od",
            "inner join orders o on o.id = od.order_id",
            "<where>",
            "  o.status = 5",
            "  and od.setmeal_id is not null",
            "  <if test='begin != null'>",
            "    and o.order_time &gt;= #{begin}",
            "  </if>",
            "  <if test='end != null'>",
            "    and o.order_time &lt; #{end}",
            "  </if>",
            "</where>",
            "group by od.name",
            "order by number desc",
            "limit #{limit}",
            "</script>"
    })
    List<GoodsSalesDTO> getTopSellingSetmeals(@Param("begin") LocalDateTime begin,
                                              @Param("end") LocalDateTime end,
                                              @Param("limit") Integer limit);

    void insertBatch(@Param("orderDetailList") List<OrderDetail> orderDetailList);
}
