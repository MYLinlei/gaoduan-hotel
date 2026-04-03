package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class HotelTableDTO implements Serializable {

    private Long id;
    private String tableNo;
    private String areaName;
    private Integer seatCount;
    private Integer status;
    private Integer sort;
    private String remark;
}
