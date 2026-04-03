package com.sky.controller.admin;

import com.sky.result.PageResult;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/admin/messages")
@Api(tags = "娑堟伅涓績鐩稿叧鎺ュ彛")
public class MessageController {

    @GetMapping("/page")
    @ApiOperation("娑堟伅鍒嗛〉鏌ヨ")
    public Result<PageResult> page(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(new PageResult(0, Collections.emptyList()));
    }

    @GetMapping("/countUnread")
    @ApiOperation("鏈娑堟伅鏁伴噺")
    public Result<Integer> countUnread() {
        return Result.success(0);
    }

    @PutMapping("/batch")
    @ApiOperation("鎵归噺鏍囪宸茶")
    public Result batch(@RequestBody(required = false) Object body) {
        return Result.success();
    }

    @PutMapping("/{id}")
    @ApiOperation("鍗曟潯鏍囪宸茶")
    public Result read(@PathVariable("id") Long id) {
        return Result.success();
    }
}
