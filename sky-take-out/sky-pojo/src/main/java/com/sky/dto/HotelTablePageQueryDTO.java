package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class HotelTablePageQueryDTO implements Serializable {

    private Integer page;
    private Integer pageSize;
    private String tableNo;
    private String areaName;
    private Integer status;
}
