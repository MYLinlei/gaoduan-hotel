package com.sky.mapper;

import com.sky.entity.ProductSku;
import com.sky.vo.ProductSkuVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductSkuMapper {

    List<ProductSkuVO> listByProductId(Long productId);

    ProductSku getById(Long id);
}
