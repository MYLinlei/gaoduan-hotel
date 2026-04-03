package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.DishComment;
import com.sky.vo.DishCommentVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DishCommentMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into dish_comment (dish_id, order_id, user_id, parent_id, content, score, liked, status) " +
            "values (#{dishId}, #{orderId}, #{userId}, #{parentId}, #{content}, #{score}, #{liked}, #{status})")
    void insert(DishComment dishComment);

    @Select("select c.id, c.dish_id as dishId, c.order_id as orderId, c.user_id as userId, c.parent_id as parentId, " +
            "c.content, c.score, c.liked, u.name as userName, u.avatar as userAvatar, c.create_time as createTime " +
            "from dish_comment c left join user u on c.user_id = u.id " +
            "where c.dish_id = #{dishId} and c.status = 1 " +
            "order by c.create_time desc")
    Page<DishCommentVO> pageVisibleByDishId(@Param("dishId") Long dishId);

    @Select("select ifnull(avg(score), 5.00) from dish_comment where dish_id = #{dishId} and status = 1")
    BigDecimal avgScoreByDishId(@Param("dishId") Long dishId);

    @Select("select * from dish_comment where dish_id = #{dishId} and user_id = #{userId} order by create_time desc")
    List<DishComment> listByDishIdAndUserId(@Param("dishId") Long dishId, @Param("userId") Long userId);
}
