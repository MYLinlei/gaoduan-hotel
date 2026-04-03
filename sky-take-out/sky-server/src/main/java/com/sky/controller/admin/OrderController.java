package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
@Api(tags = "璁㈠崟绠＄悊鐩稿叧鎺ュ彛")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation("璁㈠崟鏉′欢鍒嗛〉鏌ヨ")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("璁㈠崟鏉′欢鍒嗛〉鏌ヨ: {}", ordersPageQueryDTO);
        return Result.success(orderService.conditionSearch(ordersPageQueryDTO));
    }

    @GetMapping("/details/{orderId}")
    @ApiOperation("鏌ヨ璁㈠崟璇︽儏")
    public Result<OrderVO> details(@PathVariable("orderId") Long orderId) {
        log.info("鏌ヨ璁㈠崟璇︽儏: {}", orderId);
        return Result.success(orderService.details(orderId));
    }

    @PutMapping("/confirm")
    @ApiOperation("鎺ュ崟")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("鎺ュ崟: {}", ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation("鎷掑崟")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("鎷掑崟: {}", ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("鍙栨秷璁㈠崟")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("鍙栨秷璁㈠崟: {}", ordersCancelDTO);
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("娲鹃€佽鍗?")
    public Result delivery(@PathVariable("id") Long id) {
        log.info("娲鹃€佽鍗?: {}", id);
        orderService.delivery(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("瀹屾垚璁㈠崟")
    public Result complete(@PathVariable("id") Long id) {
        log.info("瀹屾垚璁㈠崟: {}", id);
        orderService.complete(id);
        return Result.success();
    }

    @GetMapping("/statistics")
    @ApiOperation("寰呭鐞嗚鍗曠粺璁?")
    public Result<OrderStatisticsVO> statistics() {
        log.info("寰呭鐞嗚鍗曠粺璁?");
        return Result.success(orderService.statistics());
    }
}
