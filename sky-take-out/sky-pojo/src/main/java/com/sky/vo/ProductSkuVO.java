package com.sky.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String skuCode;
    private String skuName;
    @Builder.Default
    private Map<String, String> specs = new LinkedHashMap<>();
    private BigDecimal salePrice;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer lockedStock;
    private Integer availableStock;
    private Integer status;

    @JsonIgnore
    private String specJson;
}
