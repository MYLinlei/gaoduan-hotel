package com.sky.vo;

import com.sky.entity.HotelHighVoucher;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class HotelHighVoucherVO extends HotelHighVoucher implements Serializable {
}
