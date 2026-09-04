package com.sky.mapper;

import com.sky.dto.ProductPageQueryDTO;
import com.sky.entity.Product;
import com.sky.vo.ProductDetailVO;
import com.sky.vo.ProductListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    Long count(ProductPageQueryDTO query);

    List<ProductListVO> pageQuery(ProductPageQueryDTO query);

    ProductDetailVO getDetailById(Long id);

    Product getPurchasableById(Long id);

    List<ProductListVO> listRelated(@Param("categoryId") Long categoryId,
                                    @Param("excludeId") Long excludeId,
                                    @Param("limit") Integer limit);
}
