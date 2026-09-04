package com.sky.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * C 端商品列表查询条件。
 */
@Data
public class ProductPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer page = 1;
    private Integer pageSize = 12;
    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private String keyword;
    /** default / price-asc / price-desc */
    private String sort = "default";

    @JsonIgnore
    private Integer offset = 0;
}
