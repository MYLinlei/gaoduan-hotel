package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/report")
@Api(tags = "鎶ヨ〃缁熻鐩稿叧鎺ュ彛")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/turnoverStatistics")
    @ApiOperation("钀ヤ笟棰濈粺璁?")
    public Result<TurnoverReportVO> turnoverStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(reportService.getTurnoverStatistics(begin, end));
    }

    @GetMapping("/userStatistics")
    @ApiOperation("鐢ㄦ埛缁熻")
    public Result<UserReportVO> userStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(reportService.getUserStatistics(begin, end));
    }

    @GetMapping("/ordersStatistics")
    @ApiOperation("璁㈠崟缁熻")
    public Result<OrderReportVO> ordersStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(reportService.getOrdersStatistics(begin, end));
    }

    @GetMapping("/top10")
    @ApiOperation("Top10閿€閲忕粺璁?")
    public Result<SalesTop10ReportVO> top10(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(reportService.getSalesTop10(begin, end));
    }

    @GetMapping("/dataOverView")
    @ApiOperation("鏁版嵁姒傝")
    public Result<BusinessDataVO> dataOverView(@RequestParam(required = false) String begin,
                                               @RequestParam(required = false) String end) {
        return Result.success(reportService.getBusinessData(parseBegin(begin), parseEnd(end)));
    }

    @GetMapping("/export")
    @ApiOperation("瀵煎嚭杩愯惀鎶ヨ〃")
    public void export(HttpServletResponse response) {
        reportService.export(response);
    }

    private LocalDateTime parseBegin(String begin) {
        if (!StringUtils.hasText(begin)) {
            return LocalDate.now().atStartOfDay();
        }
        return begin.length() == 10
                ? LocalDate.parse(begin).atStartOfDay()
                : LocalDateTime.parse(begin.replace(" ", "T"));
    }

    private LocalDateTime parseEnd(String end) {
        if (!StringUtils.hasText(end)) {
            return LocalDate.now().plusDays(1).atStartOfDay();
        }
        return end.length() == 10
                ? LocalDate.parse(end).plusDays(1).atStartOfDay()
                : LocalDateTime.parse(end.replace(" ", "T"));
    }
}
