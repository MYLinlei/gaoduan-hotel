package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class HotelRiderPageQueryDTO implements Serializable {

    private Integer page;
    private Integer pageSize;
    private String name;
    private Integer status;
}
