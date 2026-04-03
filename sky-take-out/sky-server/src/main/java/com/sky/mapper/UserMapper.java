package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
