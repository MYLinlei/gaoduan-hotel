package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishNote implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long dishId;

    private Long userId;

    private String title;

    private String content;

    private String images;

    private Integer liked;

    private Integer status;

    private Integer isFeatured;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
