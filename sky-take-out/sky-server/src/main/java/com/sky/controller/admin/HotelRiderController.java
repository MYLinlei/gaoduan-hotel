package com.sky.controller.admin;

import com.sky.dto.HotelRiderDTO;
import com.sky.dto.HotelRiderPageQueryDTO;
import com.sky.entity.HotelRider;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.HotelRiderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/hotelRider")
@Api(tags = "酒店自有骑手管理接口")
@Slf4j
public class HotelRiderController {

    @Autowired
    private HotelRiderService hotelRiderService;

    @PostMapping
    @ApiOperation("新增骑手")
    public Result save(@RequestBody HotelRiderDTO hotelRiderDTO) {
        hotelRiderService.save(hotelRiderDTO);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改骑手")
    public Result update(@RequestBody HotelRiderDTO hotelRiderDTO) {
        hotelRiderService.update(hotelRiderDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("删除骑手")
    public Result deleteById(Long id) {
        hotelRiderService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("查询骑手详情")
    public Result<HotelRider> getById(@PathVariable Long id) {
        return Result.success(hotelRiderService.getById(id));
    }

    @GetMapping("/page")
    @ApiOperation("分页查询骑手")
    public Result<PageResult> page(HotelRiderPageQueryDTO pageQueryDTO) {
        return Result.success(hotelRiderService.pageQuery(pageQueryDTO));
    }

    @GetMapping("/list")
    @ApiOperation("查询骑手列表")
    public Result<List<HotelRider>> list(@RequestParam(required = false) Integer status) {
        return Result.success(hotelRiderService.list(status));
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用骑手")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        hotelRiderService.startOrStop(status, id);
        return Result.success();
    }
}
