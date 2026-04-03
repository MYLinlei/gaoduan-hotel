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
public class DishLike implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long dishId;

    private Long userId;

    private LocalDateTime createTime;
}
