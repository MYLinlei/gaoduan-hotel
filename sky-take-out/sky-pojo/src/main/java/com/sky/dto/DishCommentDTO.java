package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DishCommentDTO implements Serializable {

    private Long dishId;

    private Long orderId;

    private Long parentId;

    private String content;

    private BigDecimal score;
}
