package com.sky.mapper;

import com.sky.entity.DishLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishLikeMapper {

    @Select("select * from dish_like where dish_id = #{dishId} and user_id = #{userId} limit 1")
    DishLike getByDishIdAndUserId(@Param("dishId") Long dishId, @Param("userId") Long userId);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into dish_like (dish_id, user_id) values (#{dishId}, #{userId})")
    void insert(DishLike dishLike);

    @Delete("delete from dish_like where dish_id = #{dishId} and user_id = #{userId}")
    void deleteByDishIdAndUserId(@Param("dishId") Long dishId, @Param("userId") Long userId);
}
