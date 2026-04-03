package com.sky.dto;

import com.sky.entity.OrderDetail;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrdersDTO implements Serializable {

    private Long id;

    //订单号
    private String number;

    //订单状态 1待付款，2待派送，3已派送，4已完成，5已取消
    private Integer status;

    //下单用户id
    private Long userId;

    //地址id
    private Long addressBookId;

    //下单时间
    private LocalDateTime orderTime;

    //结账时间
    private LocalDateTime checkoutTime;

    //支付方式 1微信，2支付宝
    private Integer payMethod;

    //订单类型 1外卖 2堂食
    private Integer orderType;

    //堂食桌号
    private String tableNo;

    //自有骑手ID
    private Long riderId;

    //配送区域编码
    private String deliveryZoneCode;

    //优惠券ID
    private Long couponId;

    //优惠券抵扣金额
    private BigDecimal couponAmount;

    //实际支付金额
    private BigDecimal actualPayAmount;

    //实收金额
    private BigDecimal amount;

    //备注
    private String remark;

    //用户名
    private String userName;

    //手机号
    private String phone;

    //地址
    private String address;

    //收货人
    private String consignee;

    private List<OrderDetail> orderDetails;

}
