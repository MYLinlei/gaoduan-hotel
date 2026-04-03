package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        Integer totalOrderCount = defaultZero(ordersMapper.countByCondition(null, begin, end));
        Integer validOrderCount = defaultZero(ordersMapper.countByCondition(Orders.COMPLETED, begin, end));
        Double turnover = defaultZero(ordersMapper.sumAmountByCondition(Orders.COMPLETED, begin, end));
        Integer newUsers = defaultZero(userMapper.countByCreateTime(begin, end));

        double orderCompletionRate = totalOrderCount == 0 ? 0D : validOrderCount * 1.0 / totalOrderCount;
        double unitPrice = validOrderCount == 0 ? 0D : turnover / validOrderCount;

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = buildDateList(begin, end);
        List<String> turnoverList = new ArrayList<>();

        for (LocalDate date : dates) {
            Double turnover = defaultZero(ordersMapper.sumAmountByCondition(
                    Orders.COMPLETED,
                    date.atStartOfDay(),
                    date.plusDays(1).atStartOfDay()));
            turnoverList.add(String.valueOf(turnover));
        }

        return TurnoverReportVO.builder()
                .dateList(joinDates(dates))
                .turnoverList(String.join(",", turnoverList))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = buildDateList(begin, end);
        List<String> totalUserList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();

        for (LocalDate date : dates) {
            LocalDateTime dayBegin = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            totalUserList.add(String.valueOf(defaultZero(userMapper.countByCreateTime(null, dayEnd))));
            newUserList.add(String.valueOf(defaultZero(userMapper.countByCreateTime(dayBegin, dayEnd))));
        }

        return UserReportVO.builder()
                .dateList(joinDates(dates))
                .totalUserList(String.join(",", totalUserList))
                .newUserList(String.join(",", newUserList))
                .build();
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = buildDateList(begin, end);
        List<String> orderCountList = new ArrayList<>();
        List<String> validOrderCountList = new ArrayList<>();

        int totalOrderCount = 0;
        int validOrderCount = 0;
        for (LocalDate date : dates) {
            LocalDateTime dayBegin = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            Integer dayOrderCount = defaultZero(ordersMapper.countByCondition(null, dayBegin, dayEnd));
            Integer dayValidOrderCount = defaultZero(ordersMapper.countByCondition(Orders.COMPLETED, dayBegin, dayEnd));
            orderCountList.add(String.valueOf(dayOrderCount));
            validOrderCountList.add(String.valueOf(dayValidOrderCount));
            totalOrderCount += dayOrderCount;
            validOrderCount += dayValidOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(joinDates(dates))
                .orderCountList(String.join(",", orderCountList))
                .validOrderCountList(String.join(",", validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(totalOrderCount == 0 ? 0D : validOrderCount * 1.0 / totalOrderCount)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        List<GoodsSalesDTO> salesList = ordersMapper.getSalesTop10(begin.atStartOfDay(), end.plusDays(1).atStartOfDay());
        return SalesTop10ReportVO.builder()
                .nameList(salesList.stream().map(GoodsSalesDTO::getName).collect(Collectors.joining(",")))
                .numberList(salesList.stream().map(item -> String.valueOf(item.getNumber())).collect(Collectors.joining(",")))
                .build();
    }

    @Override
    public void export(HttpServletResponse response) {
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDate begin = end.minusDays(29);

        BusinessDataVO businessData = getBusinessData(begin.atStartOfDay(), end.plusDays(1).atStartOfDay());
        TurnoverReportVO turnoverReportVO = getTurnoverStatistics(begin, end);
        UserReportVO userReportVO = getUserStatistics(begin, end);
        OrderReportVO orderReportVO = getOrdersStatistics(begin, end);
        SalesTop10ReportVO salesTop10ReportVO = getSalesTop10(begin, end);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ServletOutputStream outputStream = response.getOutputStream()) {
            var sheet = workbook.createSheet("report");
            int rowIndex = 0;

            Row titleRow = sheet.createRow(rowIndex++);
            titleRow.createCell(0).setCellValue("运营数据统计报表");

            Row rangeRow = sheet.createRow(rowIndex++);
            rangeRow.createCell(0).setCellValue("时间范围");
            rangeRow.createCell(1).setCellValue(begin + " 至 " + end);

            rowIndex++;

            Row headRow = sheet.createRow(rowIndex++);
            headRow.createCell(0).setCellValue("营业额");
            headRow.createCell(1).setCellValue("有效订单");
            headRow.createCell(2).setCellValue("订单完成率");
            headRow.createCell(3).setCellValue("平均客单价");
            headRow.createCell(4).setCellValue("新增用户");

            Row dataRow = sheet.createRow(rowIndex++);
            dataRow.createCell(0).setCellValue(businessData.getTurnover());
            dataRow.createCell(1).setCellValue(businessData.getValidOrderCount());
            dataRow.createCell(2).setCellValue(businessData.getOrderCompletionRate());
            dataRow.createCell(3).setCellValue(businessData.getUnitPrice());
            dataRow.createCell(4).setCellValue(businessData.getNewUsers());

            rowIndex++;

            Row turnoverRow = sheet.createRow(rowIndex++);
            turnoverRow.createCell(0).setCellValue("营业额趋势");
            turnoverRow.createCell(1).setCellValue(turnoverReportVO.getDateList());
            turnoverRow.createCell(2).setCellValue(turnoverReportVO.getTurnoverList());

            Row userRow = sheet.createRow(rowIndex++);
            userRow.createCell(0).setCellValue("用户趋势");
            userRow.createCell(1).setCellValue(userReportVO.getDateList());
            userRow.createCell(2).setCellValue(userReportVO.getTotalUserList());
            userRow.createCell(3).setCellValue(userReportVO.getNewUserList());

            Row orderRow = sheet.createRow(rowIndex++);
            orderRow.createCell(0).setCellValue("订单趋势");
            orderRow.createCell(1).setCellValue(orderReportVO.getDateList());
            orderRow.createCell(2).setCellValue(orderReportVO.getOrderCountList());
            orderRow.createCell(3).setCellValue(orderReportVO.getValidOrderCountList());
            orderRow.createCell(4).setCellValue(orderReportVO.getTotalOrderCount());
            orderRow.createCell(5).setCellValue(orderReportVO.getValidOrderCount());
            orderRow.createCell(6).setCellValue(orderReportVO.getOrderCompletionRate());

            Row topRow = sheet.createRow(rowIndex++);
            topRow.createCell(0).setCellValue("销量Top10");
            topRow.createCell(1).setCellValue(salesTop10ReportVO.getNameList());
            topRow.createCell(2).setCellValue(salesTop10ReportVO.getNumberList());

            for (int i = 0; i <= 6; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment; filename=" +
                    URLEncoder.encode("运营数据统计报表.xlsx", StandardCharsets.UTF_8));

            workbook.write(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException("export report failed", e);
        }
    }

    private List<LocalDate> buildDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dates.add(date);
        }
        return dates;
    }

    private String joinDates(List<LocalDate> dates) {
        return dates.stream().map(LocalDate::toString).collect(Collectors.joining(","));
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Double defaultZero(Double value) {
        return value == null ? 0D : value;
    }
}
