package com.sky.service;

import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface SkuInventoryService {

    void reserve(Orders order, List<ShoppingCart> cartItems);

    void confirm(Orders order);

    void release(Orders order);
}
