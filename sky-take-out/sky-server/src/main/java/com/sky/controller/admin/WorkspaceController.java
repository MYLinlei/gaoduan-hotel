package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/workspace")
@Api(tags = "宸ヤ綔鍙扮浉鍏虫帴鍙?")
@Slf4j
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping("/businessData")
    @ApiOperation("浠婃棩钀ヤ笟鏁版嵁")
    public Result<BusinessDataVO> businessData() {
        log.info("鑾峰彇浠婃棩钀ヤ笟鏁版嵁");
        return Result.success(workspaceService.getBusinessData());
    }

    @GetMapping("/overviewOrders")
    @ApiOperation("浠婃棩璁㈠崟姒傝")
    public Result<OrderOverViewVO> overviewOrders() {
        log.info("鑾峰彇浠婃棩璁㈠崟姒傝");
        return Result.success(workspaceService.getOrderOverView());
    }

    @GetMapping("/overviewDishes")
    @ApiOperation("鑿滃搧鎬昏")
    public Result<DishOverViewVO> overviewDishes() {
        log.info("鑾峰彇鑿滃搧鎬昏");
        return Result.success(workspaceService.getDishOverView());
    }

    @GetMapping("/overviewSetmeals")
    @ApiOperation("濂楅鎬昏")
    public Result<SetmealOverViewVO> overviewSetmeals() {
        log.info("鑾峰彇濂楅鎬昏");
        return Result.success(workspaceService.getSetmealOverView());
    }
}
