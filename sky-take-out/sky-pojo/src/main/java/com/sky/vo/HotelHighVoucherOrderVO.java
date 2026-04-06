package com.sky.vo;

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
public class HotelHighVoucherOrderVO implements Serializable {

    private Long id;

    private Long voucherId;

    private Long orderId;

    private String orderNo;

    private Integer status;

    private String voucherName;

    private String scopeType;

    private String scopeLabel;

    private BigDecimal thresholdAmount;

    private BigDecimal discountAmount;

    private LocalDateTime receiveTime;

    private LocalDateTime useTime;

    private LocalDateTime expireTime;
}
