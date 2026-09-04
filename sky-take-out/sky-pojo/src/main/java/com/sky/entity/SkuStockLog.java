package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SKU 库存变更审计流水。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuStockLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long productId;
    private Long skuId;
    private String changeType;
    private Integer stockDelta;
    private Integer lockedStockDelta;
    private Integer beforeStock;
    private Integer afterStock;
    private Integer beforeLockedStock;
    private Integer afterLockedStock;
    private LocalDateTime createTime;
}
