package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应的套餐id
     * @param dishIds
     * @return
     */
    //select setmeal id from setmeal_dish where dish_id in ()
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 绑定套餐与菜品之间的关系
     * @param setmealDishList
     */
/*    @Insert("insert into setmeal_dish (setmeal_id, dish_id, name, price, copies) VALUES " +
            "(#{setmealId}, #{dishId}, #{name}, #{price}, #{copies})")*/
    void insetBatch(List<SetmealDish> setmealDishList);


    /**
     * 根据套餐ids批量删除数据
     * @param setmealIds
     */
    void deleteBySetmealIds(List<Long> setmealIds);

    /**
     * 根据套餐id查询套餐和菜品的关联关系
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);
}
