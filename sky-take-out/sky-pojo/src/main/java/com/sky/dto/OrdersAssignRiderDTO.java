package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersAssignRiderDTO implements Serializable {

    private Long id;
    private Long riderId;
}
