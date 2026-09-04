package com.sky.service;

import com.sky.dto.ProductPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.ProductDetailVO;
import com.sky.vo.ProductListVO;

import java.util.List;

public interface ProductService {

    PageResult pageQuery(ProductPageQueryDTO query);

    ProductDetailVO getDetail(Long id);

    List<ProductListVO> listRelated(Long id, Integer limit);
}
