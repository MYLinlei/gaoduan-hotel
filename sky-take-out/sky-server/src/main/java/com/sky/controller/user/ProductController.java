package com.sky.controller.user;

import com.sky.dto.ProductPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.ProductService;
import com.sky.vo.ProductDetailVO;
import com.sky.vo.ProductListVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/product")
@Api(tags = "C端商品浏览接口")
@Slf4j
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/page")
    @ApiOperation("分页查询商品")
    public Result<PageResult> page(ProductPageQueryDTO query) {
        return Result.success(productService.pageQuery(query));
    }

    @GetMapping("/{id}")
    @ApiOperation("查询商品详情与 SKU")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.success(productService.getDetail(id));
    }

    @GetMapping("/{id}/related")
    @ApiOperation("查询同品类推荐商品")
    public Result<List<ProductListVO>> related(@PathVariable Long id,
                                               @RequestParam(required = false) Integer limit) {
        return Result.success(productService.listRelated(id, limit));
    }
}
