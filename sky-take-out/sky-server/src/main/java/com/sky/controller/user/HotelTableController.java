package com.sky.controller.user;

import com.sky.entity.HotelTable;
import com.sky.result.Result;
import com.sky.service.HotelTableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userHotelTableController")
@RequestMapping("/user/hotelTable")
@Api(tags = "User hotel table APIs")
public class HotelTableController {

    @Autowired
    private HotelTableService hotelTableService;

    @GetMapping("/list")
    @ApiOperation("List available hotel tables")
    public Result<List<HotelTable>> list(@RequestParam(required = false) Integer status) {
        return Result.success(hotelTableService.list(status));
    }
}
