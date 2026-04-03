package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelHighVoucherOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long voucherId;

    private Long userId;

    private Long orderId;

    private String orderNo;

    private Integer status;

    private LocalDateTime receiveTime;

    private LocalDateTime lockTime;

    private LocalDateTime useTime;

    private LocalDateTime expireTime;

    private LocalDateTime cancelTime;

    private String sourceType;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
