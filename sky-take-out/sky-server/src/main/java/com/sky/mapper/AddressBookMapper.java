package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into address_book (user_id, consignee, phone, sex, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default) " +
            "values (#{userId}, #{consignee}, #{phone}, #{sex}, #{provinceCode}, #{provinceName}, #{cityCode}, #{cityName}, #{districtCode}, #{districtName}, #{detail}, #{label}, #{isDefault})")
    void insert(AddressBook addressBook);

    @Select({
            "<script>",
            "select * from address_book",
            "where user_id = #{userId}",
            "<if test='consignee != null and consignee != \"\"'>",
            " and consignee like concat('%', #{consignee}, '%')",
            "</if>",
            "order by is_default desc, id desc",
            "</script>"
    })
    List<AddressBook> list(AddressBook addressBook);

    @Select("select * from address_book where id = #{id}")
    AddressBook getById(Long id);

    @Select("select * from address_book where id = #{id} and user_id = #{userId}")
    AddressBook getByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Update({
            "<script>",
            "update address_book",
            "<set>",
            "<if test='consignee != null'>consignee = #{consignee},</if>",
            "<if test='phone != null'>phone = #{phone},</if>",
            "<if test='sex != null'>sex = #{sex},</if>",
            "<if test='provinceCode != null'>province_code = #{provinceCode},</if>",
            "<if test='provinceName != null'>province_name = #{provinceName},</if>",
            "<if test='cityCode != null'>city_code = #{cityCode},</if>",
            "<if test='cityName != null'>city_name = #{cityName},</if>",
            "<if test='districtCode != null'>district_code = #{districtCode},</if>",
            "<if test='districtName != null'>district_name = #{districtName},</if>",
            "<if test='detail != null'>detail = #{detail},</if>",
            "<if test='label != null'>label = #{label},</if>",
            "<if test='isDefault != null'>is_default = #{isDefault},</if>",
            "</set>",
            "where id = #{id}",
            "</script>"
    })
    void update(AddressBook addressBook);

    @Delete("delete from address_book where id = #{id}")
    void deleteById(Long id);

    @Update("update address_book set is_default = 0 where user_id = #{userId}")
    void clearDefaultByUserId(Long userId);

    @Select("select * from address_book where user_id = #{userId} and is_default = 1 limit 1")
    AddressBook getDefaultByUserId(Long userId);

    @Select("select count(*) from address_book where user_id = #{userId}")
    Integer countByUserId(Long userId);
}
