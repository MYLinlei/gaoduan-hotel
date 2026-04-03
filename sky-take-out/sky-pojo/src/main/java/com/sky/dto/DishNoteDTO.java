package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DishNoteDTO implements Serializable {

    private Long dishId;

    private String title;

    private String content;

    private String images;
}
