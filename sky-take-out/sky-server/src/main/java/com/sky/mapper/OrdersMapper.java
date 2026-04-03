package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrdersMapper {

    List<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    Long countPageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    void update(Orders orders);

    Integer countByCondition(@Param("status") Integer status,
                             @Param("begin") LocalDateTime begin,
                             @Param("end") LocalDateTime end);

    Double sumAmountByCondition(@Param("status") Integer status,
                                @Param("begin") LocalDateTime begin,
                                @Param("end") LocalDateTime end);

    List<GoodsSalesDTO> getSalesTop10(@Param("begin") LocalDateTime begin,
                                      @Param("end") LocalDateTime end);
}
