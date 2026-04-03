package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.hmdp.utils.RedisConstants.LOCK_SHOP_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        //缓存穿透
        //Shop shop = queryWithPassThrough(id);

        //在缓存穿透的基础上加入缓存击穿:用互斥锁解决缓存击穿
        Shop shop = queryWithMutex(id);
        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        //5.返回商铺信息给前端
        return Result.ok(shop);
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    /**
     * 利用逻辑过期，解决缓存击穿
     * @param id
     * @return
     */
    public Shop queryWithLogicalExpire(Long id){
        //1.从redis中查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get("cache:shop:" + id.toString());

        //2.判断商铺是否存在
        if (StrUtil.isBlank(shopJson)) {
            //不存在
            return null;
        }

        //命中，需要先把json反序列话成对象
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        Shop shop = JSONUtil.toBean(data, Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();

        //判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            //未过期，直接返回店铺信息
            return shop;
        }

        //已经过期，需要缓存重建
        //获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);

        //判断是否互斥锁成功
        if (isLock) {
            //获取成功，开启独立线程，实现缓存重建，同样返回过期商铺信息
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    //重建缓存
                    this.saveShop2Redis(id, 20L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally{
                    //释放锁
                    unlock(lockKey);
                }
            });
        }
        //获取失败，返回店铺信息、
        return shop;
    }

    /**
     * 在缓存穿透的基础上，利用互斥锁防止缓存击穿
     */
    public Shop queryWithMutex(Long id){
        //1.从redis中查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get("cache:shop:" + id.toString());

        //2.判断商铺是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            //存在.返回商铺信息
            Shop shop = JSONUtil.toBean(shopJson, Shop.class, true);
            return shop;
        }
        //判断是否是null  空字符串“”和null不是一回事，（null：没有值）   （空字符串“”：有值，但值是空内容）
        if (shopJson != null) {
            return null;
        }

        //3.不存在，实现缓存重建
        //3.1获取互斥锁
        String lockKey = "lock:shop:"+id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);
            //3.2判断是否成功
            if (isLock) {
                //失败，则休眠重试
                Thread.sleep(50);
                queryWithMutex(id);
            }

            //3.3获取锁成功，再次检查redis缓存是否存在，不存在则继续根据id查询数据库
            shopJson = stringRedisTemplate.opsForValue().get("cache:shop:" + id.toString());

            //2.判断商铺是否存在
            if (StrUtil.isNotBlank(shopJson)) {
                //存在.返回商铺信息
                shop = JSONUtil.toBean(shopJson, Shop.class, true);
                return shop;
            }
            //判断是否是null  空字符串“”和null不是一回事，（null：没有值）   （空字符串“”：有值，但值是空内容）
            if (shopJson != null) {
                return null;
            }

            shop = getById(id);
            if (shop == null) {
                //将空值写入redis
                stringRedisTemplate.opsForValue().set("cache:shop:" + id, "", 2, TimeUnit.MINUTES);
                return null;
            }

            //4.存在，把商铺信息存储到redis
            String shopJSON = JSONUtil.toJsonStr(shop);
            stringRedisTemplate.opsForValue().set("cache:shop:" + id, shopJSON, 30L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            //释放互斥锁
            unlock(lockKey);
        }

        //5.返回商铺信息给前端
        return shop;
    }

    /**
     * 缓存穿透的代码
     * @param id
     * @return
     */
    public Shop queryWithPassThrough(Long id){
        //1.从redis中查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get("cache:shop:" + id.toString());

        //2.判断商铺是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            //存在.返回商铺信息
            Shop shop = JSONUtil.toBean(shopJson, Shop.class, true);
            return shop;
        }
        //判断是否是null  空字符串“”和null不是一回事，（null：没有值）   （空字符串“”：有值，但值是空内容）
        if (shopJson != null) {
            return null;
        }

        //3.不存在，从数据库中查询商铺信息:根据id查询数据库
        Shop shop = getById(id);
        if (shop == null) {
            //将空值写入redis
            stringRedisTemplate.opsForValue().set("cache:shop:" + id, "", 2, TimeUnit.MINUTES);
            return null;
        }

        //4.存在，把商铺信息存储到redis
        String shopJSON = JSONUtil.toJsonStr(shop);
        stringRedisTemplate.opsForValue().set("cache:shop:" + id, shopJSON, 30L, TimeUnit.MINUTES);

        //5.返回商铺信息给前端
        return shop;
    }

    /**
     * 获取锁
     * @param key
     * @return
     */
    private boolean tryLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     */
    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }

    private void saveShop2Redis(Long id, Long expireSeconds){
       // 1.查询店铺数据
        Shop shop = getById(id);
        //2.封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));

        //3.写入redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY+id, JSONUtil.toJsonStr(redisData));
    }
    /**
     * 更新商铺信息
     * @param shop
     * @return
     */
    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺的id不能为空");
        }
        //1.更新数据库
        updateById(shop);
        //2.删除缓存
        stringRedisTemplate.delete("cache:shop:" + shop.getId());
        //
        return Result.ok();
    }
}
