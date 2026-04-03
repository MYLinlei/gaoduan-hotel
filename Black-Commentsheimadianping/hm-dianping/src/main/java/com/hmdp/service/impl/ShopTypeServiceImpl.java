package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String CACHE_SHOP_TYPE_KEY = "cache:shopType:list";

    @Override
    public Result queryTypeList() {
        // 1. 先查 Redis
        String shopTypeJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_TYPE_KEY);

        // 2. Redis 有数据，直接返回
        if (shopTypeJson != null && !shopTypeJson.isEmpty()) {
            List<ShopType> typeList = JSONUtil.toList(shopTypeJson, ShopType.class);
            return Result.ok(typeList);
        }

        // 3. Redis 没有，再查数据库
        List<ShopType> typeList = this.query()
                .orderByAsc("sort")
                .list();

        // 4. 数据库也没有，返回异常信息
        if (typeList == null || typeList.isEmpty()) {
            return Result.fail("查询数据异常");
        }

        // 5. 数据库有数据，写入 Redis
        stringRedisTemplate.opsForValue().set(
                CACHE_SHOP_TYPE_KEY,
                JSONUtil.toJsonStr(typeList),
                30,
                TimeUnit.MINUTES
        );

        // 6. 返回数据
        return Result.ok(typeList);
    }
}
