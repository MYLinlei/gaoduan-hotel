package com.sky.mapper;

import com.sky.entity.DishNote;
import com.sky.vo.DishNoteVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishNoteMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into dish_note (dish_id, user_id, title, content, images, liked, status, is_featured) " +
            "values (#{dishId}, #{userId}, #{title}, #{content}, #{images}, #{liked}, #{status}, #{isFeatured})")
    void insert(DishNote dishNote);

    @Select("select n.id, n.dish_id as dishId, n.user_id as userId, n.title, n.content, n.images, n.liked, " +
            "n.is_featured as featured, u.name as userName, u.avatar as userAvatar, n.create_time as createTime " +
            "from dish_note n left join user u on n.user_id = u.id " +
            "where n.dish_id = #{dishId} and n.status = 1 " +
            "order by n.is_featured desc, n.create_time desc")
    List<DishNoteVO> listVisibleByDishId(@Param("dishId") Long dishId);
}
