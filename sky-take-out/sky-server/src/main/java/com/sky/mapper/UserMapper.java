package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    /**
     * Query user by openid.
     *
     * @param openid user openid
     * @return user
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    @Select("select * from user where phone = #{phone} limit 1")
    User getByPhone(String phone);

    @Select("select * from user where id = #{id} limit 1")
    User getById(Long id);

    @Select({
            "<script>",
            "select * from user",
            "<where>",
            "  <if test='name != null and name != \"\"'>",
            "    and name like concat('%', #{name}, '%')",
            "  </if>",
            "  <if test='phone != null and phone != \"\"'>",
            "    and phone like concat('%', #{phone}, '%')",
            "  </if>",
            "</where>",
            "order by create_time desc, id desc",
            "</script>"
    })
    List<User> pageQuery(@Param("name") String name, @Param("phone") String phone);

    @Select({
            "<script>",
            "select",
            "  count(*) as totalOrders,",
            "  sum(case when status = 5 then 1 else 0 end) as completedOrders,",
            "  sum(case when status = 6 then 1 else 0 end) as cancelledOrders,",
            "  sum(case when status in (2,3,4,7,8,9) then 1 else 0 end) as activeOrders,",
            "  ifnull(sum(case when status = 5 then ifnull(actual_pay_amount, amount) else 0 end), 0) as totalAmount,",
            "  max(order_time) as lastOrderTime",
            "from orders",
            "where user_id = #{userId}",
            "<if test='begin != null'>",
            "  and order_time &gt;= #{begin}",
            "</if>",
            "<if test='end != null'>",
            "  and order_time &lt;= #{end}",
            "</if>",
            "</script>"
    })
    Map<String, Object> getConsumptionSummary(@Param("userId") Long userId,
                                              @Param("begin") LocalDateTime begin,
                                              @Param("end") LocalDateTime end);

    /**
     * Insert a new user.
     *
     * @param user user entity
     */
    void insert(User user);

    /**
     * Count users by create_time range.
     *
     * @param begin begin time, nullable
     * @param end end time, nullable, exclusive
     * @return user count
     */
    Integer countByCreateTime(@Param("begin") LocalDateTime begin,
                              @Param("end") LocalDateTime end);
}
