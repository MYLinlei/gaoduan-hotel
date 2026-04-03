package com.sky.controller.user;

import com.sky.dto.DishCommentDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishInteractionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("userDishCommentController")
@RequestMapping("/user/dishComment")
@Api(tags = "C端菜品评价接口")
@Slf4j
public class DishCommentController {

    @Autowired
    private DishInteractionService dishInteractionService;

    @GetMapping("/page")
    @ApiOperation("分页查询菜品评价")
    public Result<PageResult> page(@RequestParam Long dishId,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(dishInteractionService.commentPage(dishId, page, pageSize));
    }

    @PostMapping
    @ApiOperation("发布菜品评价")
    public Result save(@RequestBody DishCommentDTO dishCommentDTO) {
        log.info("发布菜品评价：{}", dishCommentDTO);
        dishInteractionService.publishComment(dishCommentDTO);
        return Result.success();
    }
}
