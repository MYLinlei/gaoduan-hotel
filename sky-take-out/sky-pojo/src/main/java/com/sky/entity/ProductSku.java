package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品 SKU。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSku implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productId;
    private String skuCode;
    private String skuName;
    private String specJson;
    private BigDecimal salePrice;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer lockedStock;
    private Integer status;
    private Integer sort;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
    private Integer isDeleted;
}
