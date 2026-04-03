package com.sky.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HotelHighVoucherDTO implements Serializable {

    private Long id;

    private String name;

    private String scopeType;

    private Long scopeId;

    private String couponType;

    private String channelType;

    private Integer totalStock;

    private Integer availableStock;

    private BigDecimal payValue;

    private BigDecimal actualValue;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime seckillBeginTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime seckillEndTime;

    private Integer perLimit;

    private Integer dayLimit;

    private Integer status;

    private String rules;

    private String remark;
}
