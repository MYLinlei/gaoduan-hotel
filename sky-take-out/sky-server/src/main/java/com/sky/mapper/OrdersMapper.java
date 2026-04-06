package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrdersMapper {

    void insert(Orders orders);

    List<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    Long countPageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Select("select * from orders where number = #{number}")
    Orders getByNumber(String number);

    void update(Orders orders);

    Integer countByCondition(@Param("status") Integer status,
                             @Param("begin") LocalDateTime begin,
                             @Param("end") LocalDateTime end);

    Double sumAmountByCondition(@Param("status") Integer status,
                                @Param("begin") LocalDateTime begin,
                                @Param("end") LocalDateTime end);

    List<GoodsSalesDTO> getSalesTop10(@Param("begin") LocalDateTime begin,
                                      @Param("end") LocalDateTime end);

    @Select({
            "<script>",
            "select",
            "  u.id as userId,",
            "  u.phone as phone,",
            "  u.name as nickname,",
            "  count(o.id) as completedOrders,",
            "  ifnull(sum(ifnull(o.actual_pay_amount, o.amount)), 0) as totalAmount,",
            "  max(o.order_time) as lastOrderTime",
            "from orders o",
            "inner join user u on u.id = o.user_id",
            "where o.status = 5",
            "<if test='begin != null'>",
            "  and o.order_time &gt;= #{begin}",
            "</if>",
            "<if test='end != null'>",
            "  and o.order_time &lt;= #{end}",
            "</if>",
            "group by u.id, u.phone, u.name",
            "order by totalAmount desc, completedOrders desc, lastOrderTime desc",
            "limit #{limit}",
            "</script>"
    })
    List<Map<String, Object>> listUserConsumptionRanking(@Param("begin") LocalDateTime begin,
                                                         @Param("end") LocalDateTime end,
                                                         @Param("limit") Integer limit);
}
