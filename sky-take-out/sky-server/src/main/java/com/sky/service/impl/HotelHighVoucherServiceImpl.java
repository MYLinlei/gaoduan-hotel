package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.VoucherRedisConstant;
import com.sky.context.BaseContext;
import com.sky.dto.HotelHighVoucherDTO;
import com.sky.dto.HotelHighVoucherPageQueryDTO;
import com.sky.entity.HotelHighVoucher;
import com.sky.entity.HotelHighVoucherOrder;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.HotelHighVoucherMapper;
import com.sky.mapper.HotelHighVoucherOrderMapper;
import com.sky.result.PageResult;
import com.sky.service.HotelHighVoucherService;
import com.sky.vo.HotelHighVoucherOrderVO;
import com.sky.vo.HotelHighVoucherVO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class HotelHighVoucherServiceImpl implements HotelHighVoucherService {

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("lua/hotel_voucher_seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Autowired
    private HotelHighVoucherMapper hotelHighVoucherMapper;

    @Autowired
    private HotelHighVoucherOrderMapper hotelHighVoucherOrderMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    @Transactional
    public void save(HotelHighVoucherDTO hotelHighVoucherDTO) {
        HotelHighVoucher voucher = new HotelHighVoucher();
        BeanUtils.copyProperties(hotelHighVoucherDTO, voucher);
        normalizeVoucher(voucher);
        hotelHighVoucherMapper.insert(voucher);
        syncVoucherStockToRedis(voucher.getId(), voucher.getAvailableStock());
    }

    @Override
    @Transactional
    public void update(HotelHighVoucherDTO hotelHighVoucherDTO) {
        HotelHighVoucher current = hotelHighVoucherMapper.getById(hotelHighVoucherDTO.getId());
        if (current == null) {
            throw new OrderBusinessException("优惠券不存在");
        }

        HotelHighVoucher voucher = new HotelHighVoucher();
        BeanUtils.copyProperties(hotelHighVoucherDTO, voucher);
        normalizeVoucher(voucher);
        hotelHighVoucherMapper.update(voucher);
        Integer stock = voucher.getAvailableStock() != null ? voucher.getAvailableStock() : current.getAvailableStock();
        syncVoucherStockToRedis(voucher.getId(), stock);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        HotelHighVoucher current = hotelHighVoucherMapper.getById(id);
        if (current == null) {
            throw new OrderBusinessException("优惠券不存在");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new OrderBusinessException("优惠券状态错误");
        }

        hotelHighVoucherMapper.update(HotelHighVoucher.builder()
                .id(id)
                .status(status)
                .build());
        syncVoucherStockToRedis(id, current.getAvailableStock());
    }

    @Override
    public HotelHighVoucher getById(Long id) {
        return hotelHighVoucherMapper.getById(id);
    }

    @Override
    public PageResult pageQuery(HotelHighVoucherPageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<HotelHighVoucherVO> page = hotelHighVoucherMapper.pageQuery(queryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<HotelHighVoucher> listEnabled(String scopeType, Long scopeId, String channelType) {
        return hotelHighVoucherMapper.listEnabled(scopeType, scopeId, channelType);
    }

    @Override
    @Transactional
    public Long seckill(Long voucherId) {
        Long userId = BaseContext.getCurrentId();
        HotelHighVoucher voucher = hotelHighVoucherMapper.getById(voucherId);
        if (voucher == null || voucher.getStatus() == null || voucher.getStatus() != 1) {
            throw new OrderBusinessException("优惠券不存在或未启用");
        }
        validateVoucherWindow(voucher);
        initVoucherStockIfAbsent(voucher);

        Long scriptResult = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString()
        );
        if (scriptResult == null) {
            throw new OrderBusinessException("抢券失败，请稍后重试");
        }
        if (scriptResult == 1L) {
            throw new OrderBusinessException("优惠券库存不足");
        }
        if (scriptResult == 2L) {
            throw new OrderBusinessException("不能重复领取该优惠券");
        }

        String lockKey = VoucherRedisConstant.HIGH_VOUCHER_USER_LOCK + voucherId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked;
        try {
            locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            compensateSeckill(voucherId, userId);
            throw new OrderBusinessException("抢券失败，请稍后重试");
        }
        if (!locked) {
            compensateSeckill(voucherId, userId);
            throw new OrderBusinessException("抢券过于频繁，请稍后重试");
        }

        try {
            return createVoucherOrder(voucher, userId);
        } catch (RuntimeException ex) {
            compensateSeckill(voucherId, userId);
            throw ex;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<HotelHighVoucherOrderVO> myCoupons(Integer status) {
        return hotelHighVoucherOrderMapper.listByUserId(BaseContext.getCurrentId(), status);
    }

    @Override
    public HotelHighVoucherOrderVO getOwnedCoupon(Long voucherOrderId, Long userId, BigDecimal orderAmount) {
        if (voucherOrderId == null) {
            return null;
        }

        HotelHighVoucherOrder voucherOrder = hotelHighVoucherOrderMapper.getById(voucherOrderId);
        if (voucherOrder == null || !userId.equals(voucherOrder.getUserId())) {
            throw new OrderBusinessException("优惠券不存在");
        }
        if (voucherOrder.getStatus() == null || voucherOrder.getStatus() != 1) {
            throw new OrderBusinessException("优惠券不可用");
        }

        HotelHighVoucher voucher = hotelHighVoucherMapper.getById(voucherOrder.getVoucherId());
        if (voucher == null || voucher.getStatus() == null || voucher.getStatus() != 1) {
            throw new OrderBusinessException("优惠券不可用");
        }
        validateVoucherWindow(voucher);
        if (orderAmount == null || orderAmount.compareTo(voucher.getPayValue()) < 0) {
            throw new OrderBusinessException("未达到优惠券使用门槛");
        }

        return buildVoucherOrderVO(voucherOrder, voucher);
    }

    @Override
    @Transactional
    public void useCoupon(Long voucherOrderId, Long orderId, Long userId) {
        if (voucherOrderId == null) {
            return;
        }

        HotelHighVoucherOrder voucherOrder = hotelHighVoucherOrderMapper.getById(voucherOrderId);
        if (voucherOrder == null || !userId.equals(voucherOrder.getUserId())) {
            throw new OrderBusinessException("优惠券不存在");
        }
        if (voucherOrder.getStatus() == null || voucherOrder.getStatus() != 1) {
            throw new OrderBusinessException("优惠券状态错误");
        }

        hotelHighVoucherOrderMapper.update(HotelHighVoucherOrder.builder()
                .id(voucherOrderId)
                .orderId(orderId)
                .status(3)
                .useTime(LocalDateTime.now())
                .build());
    }

    protected Long createVoucherOrder(HotelHighVoucher voucher, Long userId) {
        Integer count = hotelHighVoucherOrderMapper.countValidOrderByVoucherAndUser(voucher.getId(), userId);
        if (count != null && count >= safeLimit(voucher.getPerLimit())) {
            throw new OrderBusinessException("不能重复领取该优惠券");
        }

        LocalDateTime beginOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = beginOfDay.plusDays(1);
        Integer dayCount = hotelHighVoucherOrderMapper.countDailyOrderByVoucherAndUser(
                voucher.getId(), userId, beginOfDay, endOfDay
        );
        if (dayCount != null && dayCount >= safeLimit(voucher.getDayLimit())) {
            throw new OrderBusinessException("今日领取次数已达上限");
        }

        int updated = hotelHighVoucherMapper.decreaseAvailableStock(voucher.getId());
        if (updated < 1) {
            throw new OrderBusinessException("优惠券库存不足");
        }

        String orderNo = "HV" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        HotelHighVoucherOrder voucherOrder = HotelHighVoucherOrder.builder()
                .voucherId(voucher.getId())
                .userId(userId)
                .orderNo(orderNo)
                .status(1)
                .receiveTime(LocalDateTime.now())
                .expireTime(voucher.getEndTime())
                .sourceType("SECKILL")
                .build();
        hotelHighVoucherOrderMapper.insert(voucherOrder);
        return voucherOrder.getId();
    }

    private HotelHighVoucherOrderVO buildVoucherOrderVO(HotelHighVoucherOrder voucherOrder, HotelHighVoucher voucher) {
        return HotelHighVoucherOrderVO.builder()
                .id(voucherOrder.getId())
                .voucherId(voucher.getId())
                .orderId(voucherOrder.getOrderId())
                .orderNo(voucherOrder.getOrderNo())
                .status(voucherOrder.getStatus())
                .voucherName(voucher.getName())
                .scopeType(voucher.getScopeType())
                .scopeLabel(voucher.getChannelType())
                .thresholdAmount(voucher.getPayValue())
                .discountAmount(voucher.getActualValue())
                .receiveTime(voucherOrder.getReceiveTime())
                .useTime(voucherOrder.getUseTime())
                .expireTime(voucherOrder.getExpireTime())
                .build();
    }

    private void validateVoucherWindow(HotelHighVoucher voucher) {
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getSeckillBeginTime() != null && voucher.getSeckillBeginTime().isAfter(now)) {
            throw new OrderBusinessException("抢券尚未开始");
        }
        if (voucher.getSeckillEndTime() != null && voucher.getSeckillEndTime().isBefore(now)) {
            throw new OrderBusinessException("抢券已结束");
        }
        if (voucher.getBeginTime() != null && voucher.getBeginTime().isAfter(now)) {
            throw new OrderBusinessException("优惠券尚未生效");
        }
        if (voucher.getEndTime() != null && voucher.getEndTime().isBefore(now)) {
            throw new OrderBusinessException("优惠券已失效");
        }
    }

    private void normalizeVoucher(HotelHighVoucher voucher) {
        if (voucher.getScopeType() == null || voucher.getScopeType().isBlank()) {
            voucher.setScopeType("ALL_STORE");
        }
        if (voucher.getCouponType() == null || voucher.getCouponType().isBlank()) {
            voucher.setCouponType("FULL_REDUCTION");
        }
        if (voucher.getChannelType() == null || voucher.getChannelType().isBlank()) {
            voucher.setChannelType("UNIVERSAL");
        }
        if (voucher.getAvailableStock() == null && voucher.getTotalStock() != null) {
            voucher.setAvailableStock(voucher.getTotalStock());
        }
        if (voucher.getPerLimit() == null || voucher.getPerLimit() < 1) {
            voucher.setPerLimit(1);
        }
        if (voucher.getDayLimit() == null || voucher.getDayLimit() < 1) {
            voucher.setDayLimit(1);
        }
        if (voucher.getStatus() == null) {
            voucher.setStatus(1);
        }
    }

    private void syncVoucherStockToRedis(Long voucherId, Integer availableStock) {
        stringRedisTemplate.opsForValue().set(
                VoucherRedisConstant.HIGH_VOUCHER_STOCK_KEY + voucherId,
                String.valueOf(availableStock == null ? 0 : availableStock)
        );
    }

    private void initVoucherStockIfAbsent(HotelHighVoucher voucher) {
        stringRedisTemplate.opsForValue().setIfAbsent(
                VoucherRedisConstant.HIGH_VOUCHER_STOCK_KEY + voucher.getId(),
                String.valueOf(voucher.getAvailableStock() == null ? 0 : voucher.getAvailableStock())
        );
    }

    private void compensateSeckill(Long voucherId, Long userId) {
        stringRedisTemplate.opsForValue().increment(VoucherRedisConstant.HIGH_VOUCHER_STOCK_KEY + voucherId);
        stringRedisTemplate.opsForSet().remove(
                VoucherRedisConstant.HIGH_VOUCHER_ORDER_KEY + voucherId,
                userId.toString()
        );
    }

    private int safeLimit(Integer limit) {
        return limit == null || limit < 1 ? 1 : limit;
    }
}
