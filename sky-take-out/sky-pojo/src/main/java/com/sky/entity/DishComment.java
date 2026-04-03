package com.sky.entity;

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
public class DishComment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long dishId;

    private Long orderId;

    private Long userId;

    private Long parentId;

    private String content;

    private BigDecimal score;

    private Integer liked;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
