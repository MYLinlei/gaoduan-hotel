package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    @Transactional
    public ShoppingCart addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = buildShoppingCartQuery(shoppingCartDTO);
        List<ShoppingCart> carts = shoppingCartMapper.list(shoppingCart);
        if (!carts.isEmpty()) {
            ShoppingCart current = carts.get(0);
            current.setNumber(current.getNumber() + 1);
            shoppingCartMapper.updateNumberById(current);
            return current;
        }

        fillItemMeta(shoppingCart, shoppingCartDTO);
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
        return shoppingCart;
    }

    @Override
    @Transactional
    public ShoppingCart subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = buildShoppingCartQuery(shoppingCartDTO);
        List<ShoppingCart> carts = shoppingCartMapper.list(shoppingCart);
        if (carts.isEmpty()) {
            throw new ShoppingCartBusinessException("购物车中不存在当前商品");
        }

        ShoppingCart current = carts.get(0);
        if (current.getNumber() != null && current.getNumber() > 1) {
            current.setNumber(current.getNumber() - 1);
            shoppingCartMapper.updateNumberById(current);
            return current;
        }

        shoppingCartMapper.deleteById(current.getId());
        current.setNumber(0);
        return current;
    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        return shoppingCartMapper.list(shoppingCart);
    }

    @Override
    public void cleanShoppingCart() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    private ShoppingCart buildShoppingCartQuery(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        shoppingCart.setDishId(shoppingCartDTO.getDishId());
        shoppingCart.setSetmealId(shoppingCartDTO.getSetmealId());
        shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
        return shoppingCart;
    }

    private void fillItemMeta(ShoppingCart shoppingCart, ShoppingCartDTO shoppingCartDTO) {
        if (shoppingCartDTO.getDishId() != null) {
            Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
            if (dish == null) {
                throw new ShoppingCartBusinessException("菜品不存在");
            }
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
            return;
        }

        if (shoppingCartDTO.getSetmealId() != null) {
            Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
            if (setmeal == null) {
                throw new ShoppingCartBusinessException("套餐不存在");
            }
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
            return;
        }

        throw new ShoppingCartBusinessException("购物车数据不合法");
    }
}
