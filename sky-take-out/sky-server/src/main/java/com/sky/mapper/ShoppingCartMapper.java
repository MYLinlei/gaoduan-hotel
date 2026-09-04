package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    @Select({
            "<script>",
            "select * from shopping_cart",
            "where user_id = #{userId}",
            "<if test='productId != null'> and product_id = #{productId}</if>",
            "<if test='skuId != null'> and sku_id = #{skuId}</if>",
            "<if test='dishId != null'> and dish_id = #{dishId}</if>",
            "<if test='setmealId != null'> and setmeal_id = #{setmealId}</if>",
            "<if test='dishFlavor != null and dishFlavor != \"\"'> and dish_flavor = #{dishFlavor}</if>",
            "order by create_time asc, id asc",
            "</script>"
    })
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into shopping_cart (name, user_id, dish_id, product_id, sku_id, setmeal_id, dish_flavor, " +
            "sku_spec, number, amount, image, unit, create_time) " +
            "values (#{name}, #{userId}, #{dishId}, #{productId}, #{skuId}, #{setmealId}, #{dishFlavor}, " +
            "#{skuSpec}, #{number}, #{amount}, #{image}, #{unit}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    @Update("update shopping_cart set name = #{name}, dish_id = #{dishId}, sku_spec = #{skuSpec}, " +
            "number = #{number}, amount = #{amount}, image = #{image}, unit = #{unit} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);
}
