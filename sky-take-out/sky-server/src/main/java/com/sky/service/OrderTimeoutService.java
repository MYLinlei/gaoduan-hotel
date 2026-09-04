package com.sky.service;

public interface OrderTimeoutService {

    void closeExpiredOrder(Long orderId);
}
