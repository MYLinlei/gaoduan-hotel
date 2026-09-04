package com.sky.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailVO implements Serializable {

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
    private String detailDescription;
    @Builder.Default
    private Map<String, Object> attributes = new LinkedHashMap<>();
    @Builder.Default
    private List<ProductSkuVO> skus = new ArrayList<>();

    @JsonIgnore
    private String attributesJson;

    @JsonIgnore
    private Integer enabledSkuCount;
}
