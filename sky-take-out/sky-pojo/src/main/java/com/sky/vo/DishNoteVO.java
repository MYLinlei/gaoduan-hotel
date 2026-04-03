package com.sky.vo;

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
public class DishNoteVO implements Serializable {

    private Long id;

    private Long dishId;

    private Long userId;

    private String title;

    private String content;

    private String images;

    private Integer liked;

    private Boolean featured;

    private String userName;

    private String userAvatar;

    private LocalDateTime createTime;
}
