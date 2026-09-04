package com.sky.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Product;
import com.sky.entity.ProductSku;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.ProductMapper;
import com.sky.mapper.ProductSkuMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductSkuMapper productSkuMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public ShoppingCart addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        validateRequest(shoppingCartDTO);
        ShoppingCart shoppingCart = buildShoppingCartQuery(shoppingCartDTO);
        List<ShoppingCart> carts = shoppingCartMapper.list(shoppingCart);
        int quantity = normalizeQuantity(shoppingCartDTO.getQuantity());
        if (!carts.isEmpty()) {
            ShoppingCart current = carts.get(0);
            int nextNumber = current.getNumber() + quantity;
            ensureQuantityLimit(nextNumber);
            if (shoppingCartDTO.getProductId() != null) {
                fillProductMeta(current, shoppingCartDTO, nextNumber);
            }
            current.setNumber(nextNumber);
            shoppingCartMapper.updateNumberById(current);
            return current;
        }

        fillItemMeta(shoppingCart, shoppingCartDTO);
        shoppingCart.setNumber(quantity);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
        return shoppingCart;
    }

    @Override
    @Transactional
    public ShoppingCart subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        validateRequest(shoppingCartDTO);
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
        shoppingCart.setProductId(shoppingCartDTO.getProductId());
        shoppingCart.setSkuId(shoppingCartDTO.getSkuId());
        shoppingCart.setDishId(shoppingCartDTO.getDishId());
        shoppingCart.setSetmealId(shoppingCartDTO.getSetmealId());
        shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
        return shoppingCart;
    }

    private void fillItemMeta(ShoppingCart shoppingCart, ShoppingCartDTO shoppingCartDTO) {
        if (shoppingCartDTO.getProductId() != null) {
            fillProductMeta(shoppingCart, shoppingCartDTO, normalizeQuantity(shoppingCartDTO.getQuantity()));
            return;
        }

        if (shoppingCartDTO.getDishId() != null) {
            Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
            if (dish == null) {
                throw new ShoppingCartBusinessException("商品不存在");
            }
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
            return;
        }

        if (shoppingCartDTO.getSetmealId() != null) {
            Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
            if (setmeal == null) {
                throw new ShoppingCartBusinessException("组合商品不存在");
            }
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
            return;
        }

        throw new ShoppingCartBusinessException("购物车数据不合法");
    }

    private void fillProductMeta(ShoppingCart cart, ShoppingCartDTO dto, int requestedNumber) {
        Product product = productMapper.getPurchasableById(dto.getProductId());
        if (product == null) {
            throw new ShoppingCartBusinessException("商品不存在或已下架");
        }
        ProductSku sku = productSkuMapper.getById(dto.getSkuId());
        if (sku == null || !product.getId().equals(sku.getProductId())) {
            throw new ShoppingCartBusinessException("商品规格不存在");
        }
        ensureAvailableStock(sku, requestedNumber);

        cart.setProductId(product.getId());
        cart.setSkuId(sku.getId());
        cart.setDishId(product.getLegacyDishId());
        cart.setName(product.getName());
        cart.setSkuSpec(formatSkuSpec(sku));
        cart.setAmount(sku.getSalePrice());
        cart.setImage(product.getMainImage());
        cart.setUnit(product.getUnit());
    }

    private void ensureAvailableStock(ProductSku sku, int requestedNumber) {
        if (sku == null || sku.getStatus() == null || sku.getStatus() == 0) {
            throw new ShoppingCartBusinessException("当前商品规格已停售");
        }
        int stock = sku.getStock() == null ? 0 : sku.getStock();
        int lockedStock = sku.getLockedStock() == null ? 0 : sku.getLockedStock();
        if (requestedNumber > Math.max(stock - lockedStock, 0)) {
            throw new ShoppingCartBusinessException("当前商品规格库存不足");
        }
    }

    private String formatSkuSpec(ProductSku sku) {
        try {
            Map<String, Object> specs = objectMapper.readValue(sku.getSpecJson(),
                    new TypeReference<LinkedHashMap<String, Object>>() { });
            StringJoiner joiner = new StringJoiner("；");
            specs.forEach((key, value) -> joiner.add(key + "：" + value));
            return joiner.length() == 0 ? sku.getSkuName() : joiner.toString();
        } catch (Exception ignored) {
            return sku.getSkuName();
        }
    }

    private void validateRequest(ShoppingCartDTO dto) {
        if (dto == null) {
            throw new ShoppingCartBusinessException("购物车数据不合法");
        }
        boolean hasProduct = dto.getProductId() != null || dto.getSkuId() != null;
        if (hasProduct && (dto.getProductId() == null || dto.getSkuId() == null)) {
            throw new ShoppingCartBusinessException("商品和 SKU 必须同时选择");
        }
        if (hasProduct && (dto.getDishId() != null || dto.getSetmealId() != null)) {
            throw new ShoppingCartBusinessException("购物车商品类型冲突");
        }
        if (!hasProduct && (dto.getDishId() == null) == (dto.getSetmealId() == null)) {
            throw new ShoppingCartBusinessException("必须且只能选择一种商品");
        }
    }

    private int normalizeQuantity(Integer quantity) {
        if (quantity == null) {
            return 1;
        }
        if (quantity < 1 || quantity > 99) {
            throw new ShoppingCartBusinessException("商品数量必须在 1 到 99 之间");
        }
        return quantity;
    }

    private void ensureQuantityLimit(int quantity) {
        if (quantity < 1 || quantity > 99) {
            throw new ShoppingCartBusinessException("单件商品数量必须在 1 到 99 件之间");
        }
    }
}
