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
public class ProductListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long legacyDishId;
    private Long categoryId;
    private String categoryName;
    private String productCode;
    private String brandName;
    private String name;
    private String subtitle;
    private String unit;
    private String mainImage;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer availableStock;
    private String stockStatus;
    private Integer status;
    private Integer sort;
    @Builder.Default
    private Map<String, Object> attributes = new LinkedHashMap<>();

    @JsonIgnore
    private String attributesJson;

    @JsonIgnore
    private Integer enabledSkuCount;
}
