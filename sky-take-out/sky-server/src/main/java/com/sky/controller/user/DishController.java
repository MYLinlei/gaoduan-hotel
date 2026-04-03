package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishInteractionService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端菜品浏览接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishInteractionService dishInteractionService;

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);
        return Result.success(dishService.listWithFlavor(dish));
    }

    @GetMapping("/{id}")
    @ApiOperation("查询菜品详情")
    public Result<DishVO> detail(@PathVariable Long id) {
        return Result.success(dishInteractionService.getDetail(id));
    }

    @PostMapping("/like/{id}")
    @ApiOperation("切换菜品点赞状态")
    public Result<Boolean> toggleLike(@PathVariable Long id) {
        return Result.success(dishInteractionService.toggleLike(id));
    }

    @PostMapping("/favorite/{id}")
    @ApiOperation("切换菜品收藏状态")
    public Result<Boolean> toggleFavorite(@PathVariable Long id) {
        return Result.success(dishInteractionService.toggleFavorite(id));
    }

    @GetMapping("/favorite/page")
    @ApiOperation("分页查询我的收藏菜品")
    public Result<PageResult> favoritePage(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(dishInteractionService.favoritePage(page, pageSize));
    }
}
