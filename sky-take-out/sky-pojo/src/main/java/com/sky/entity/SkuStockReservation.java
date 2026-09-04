package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单维度的 SKU 库存预占记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuStockReservation implements Serializable {

    public static final int LOCKED = 1;
    public static final int CONFIRMED = 2;
    public static final int RELEASED = 3;

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long productId;
    private Long skuId;
    private Integer quantity;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
