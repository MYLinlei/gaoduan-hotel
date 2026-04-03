package com.sky.vo;

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
public class DishCommentVO implements Serializable {

    private Long id;

    private Long dishId;

    private Long orderId;

    private Long userId;

    private Long parentId;

    private String content;

    private BigDecimal score;

    private Integer liked;

    private String userName;

    private String userAvatar;

    private LocalDateTime createTime;
}
