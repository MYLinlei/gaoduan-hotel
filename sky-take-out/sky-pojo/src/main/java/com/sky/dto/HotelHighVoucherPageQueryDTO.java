package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class HotelHighVoucherPageQueryDTO implements Serializable {

    private Integer page = 1;

    private Integer pageSize = 10;

    private String name;

    private String scopeType;

    private String channelType;

    private Integer status;
}
