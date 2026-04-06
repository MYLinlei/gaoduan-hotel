package com.sky.controller.user;

import com.sky.entity.HotelHighVoucher;
import com.sky.result.Result;
import com.sky.service.HotelHighVoucherService;
import com.sky.vo.HotelHighVoucherOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userHotelHighVoucherController")
@RequestMapping("/user/hotelHighVoucher")
@Api(tags = "顾客端优惠券接口")
public class HotelHighVoucherController {

    @Autowired
    private HotelHighVoucherService hotelHighVoucherService;

    @GetMapping("/list")
    @ApiOperation("查询可领取优惠券")
    public Result<List<HotelHighVoucher>> list(@RequestParam(required = false) String scopeType,
                                               @RequestParam(required = false) Long scopeId,
                                               @RequestParam(required = false) String channelType) {
        return Result.success(hotelHighVoucherService.listEnabled(scopeType, scopeId, channelType));
    }

    @GetMapping("/my")
    @ApiOperation("查询我的优惠券")
    public Result<List<HotelHighVoucherOrderVO>> myCoupons(@RequestParam(required = false) Integer status) {
        return Result.success(hotelHighVoucherService.myCoupons(status));
    }

    @PostMapping("/seckill/{id}")
    @ApiOperation("领取优惠券")
    public Result<Long> seckill(@PathVariable("id") Long id) {
        return Result.success(hotelHighVoucherService.seckill(id));
    }
}
