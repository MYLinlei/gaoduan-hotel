package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class HotelRiderDTO implements Serializable {

    private Long id;
    private String name;
    private String phone;
    private String idCardNo;
    private String vehicleType;
    private String vehicleNo;
    private Integer status;
    private String deliveryZoneCode;
    private Integer sort;
    private String remark;
}
