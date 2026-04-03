package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelHighVoucher implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private LocalDateTime seckillBeginTime;

    private LocalDateTime seckillEndTime;

    private Integer perLimit;

    private Integer dayLimit;

    private Integer status;

    private String rules;

    private String remark;

    private Long createUser;

    private Long updateUser;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
