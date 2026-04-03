package com.sky.controller.user;

import com.sky.dto.DishNoteDTO;
import com.sky.result.Result;
import com.sky.service.DishInteractionService;
import com.sky.vo.DishNoteVO;
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

import java.util.List;

@RestController("userDishNoteController")
@RequestMapping("/user/dishNote")
@Api(tags = "C端菜品种草接口")
@Slf4j
public class DishNoteController {

    @Autowired
    private DishInteractionService dishInteractionService;

    @GetMapping("/list")
    @ApiOperation("查询菜品种草笔记")
    public Result<List<DishNoteVO>> list(@RequestParam Long dishId) {
        return Result.success(dishInteractionService.listNotes(dishId));
    }

    @PostMapping
    @ApiOperation("发布菜品种草笔记")
    public Result save(@RequestBody DishNoteDTO dishNoteDTO) {
        log.info("发布菜品种草笔记：{}", dishNoteDTO);
        dishInteractionService.publishNote(dishNoteDTO);
        return Result.success();
    }
}
