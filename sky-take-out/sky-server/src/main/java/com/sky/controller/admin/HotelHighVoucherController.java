package com.sky.controller.admin;

import com.sky.dto.HotelHighVoucherDTO;
import com.sky.dto.HotelHighVoucherPageQueryDTO;
import com.sky.entity.HotelHighVoucher;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.HotelHighVoucherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/hotelHighVoucher")
@Api(tags = "高端优惠券管理接口")
@Slf4j
public class HotelHighVoucherController {

    @Autowired
    private HotelHighVoucherService hotelHighVoucherService;

    @PostMapping
    @ApiOperation("新增高端优惠券")
    public Result save(@RequestBody HotelHighVoucherDTO hotelHighVoucherDTO) {
        log.info("新增高端优惠券：{}", hotelHighVoucherDTO);
        hotelHighVoucherService.save(hotelHighVoucherDTO);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改高端优惠券")
    public Result update(@RequestBody HotelHighVoucherDTO hotelHighVoucherDTO) {
        log.info("修改高端优惠券：{}", hotelHighVoucherDTO);
        hotelHighVoucherService.update(hotelHighVoucherDTO);
        return Result.success();
    }

    @PutMapping("/{id}/status/{status}")
    @ApiOperation("更新优惠券上下架状态")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        log.info("更新高端优惠券状态，id={}, status={}", id, status);
        hotelHighVoucherService.updateStatus(id, status);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("查询高端优惠券详情")
    public Result<HotelHighVoucher> getById(@PathVariable Long id) {
        return Result.success(hotelHighVoucherService.getById(id));
    }

    @GetMapping("/page")
    @ApiOperation("分页查询高端优惠券")
    public Result<PageResult> page(HotelHighVoucherPageQueryDTO queryDTO) {
        return Result.success(hotelHighVoucherService.pageQuery(queryDTO));
    }
}
