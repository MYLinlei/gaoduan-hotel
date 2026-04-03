package com.sky.controller.admin;

import com.sky.dto.HotelTableDTO;
import com.sky.dto.HotelTablePageQueryDTO;
import com.sky.entity.HotelTable;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.HotelTableService;
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
@RequestMapping("/admin/hotelTable")
@Api(tags = "酒店堂食桌号管理接口")
@Slf4j
public class HotelTableController {

    @Autowired
    private HotelTableService hotelTableService;

    @PostMapping
    @ApiOperation("新增桌号")
    public Result save(@RequestBody HotelTableDTO hotelTableDTO) {
        hotelTableService.save(hotelTableDTO);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改桌号")
    public Result update(@RequestBody HotelTableDTO hotelTableDTO) {
        hotelTableService.update(hotelTableDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("删除桌号")
    public Result deleteById(Long id) {
        hotelTableService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("查询桌号详情")
    public Result<HotelTable> getById(@PathVariable Long id) {
        return Result.success(hotelTableService.getById(id));
    }

    @GetMapping("/page")
    @ApiOperation("分页查询桌号")
    public Result<PageResult> page(HotelTablePageQueryDTO pageQueryDTO) {
        return Result.success(hotelTableService.pageQuery(pageQueryDTO));
    }

    @GetMapping("/list")
    @ApiOperation("查询桌号列表")
    public Result<List<HotelTable>> list(@RequestParam(required = false) Integer status) {
        return Result.success(hotelTableService.list(status));
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用桌号")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        hotelTableService.startOrStop(status, id);
        return Result.success();
    }
}
