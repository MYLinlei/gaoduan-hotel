package com.sky.mapper;

import com.sky.entity.DishFavorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishFavoriteMapper {

    @Select("select * from dish_favorite where dish_id = #{dishId} and user_id = #{userId} limit 1")
    DishFavorite getByDishIdAndUserId(@Param("dishId") Long dishId, @Param("userId") Long userId);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into dish_favorite (dish_id, user_id) values (#{dishId}, #{userId})")
    void insert(DishFavorite dishFavorite);

    @Delete("delete from dish_favorite where dish_id = #{dishId} and user_id = #{userId}")
    void deleteByDishIdAndUserId(@Param("dishId") Long dishId, @Param("userId") Long userId);
}
