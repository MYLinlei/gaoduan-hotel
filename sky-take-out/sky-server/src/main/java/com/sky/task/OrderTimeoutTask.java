package com.sky.task;

import com.sky.mapper.OrdersMapper;
import com.sky.service.OrderTimeoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@ConditionalOnProperty(name = "sky.order.timeout-scan-enabled", havingValue = "true", matchIfMissing = true)
public class OrderTimeoutTask {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderTimeoutService orderTimeoutService;

    @Value("${sky.order.payment-timeout-minutes:15}")
    private long paymentTimeoutMinutes;

    @Scheduled(initialDelayString = "${sky.order.timeout-scan-initial-delay-ms:60000}",
            fixedDelayString = "${sky.order.timeout-scan-interval-ms:60000}")
    public void closeExpiredOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(paymentTimeoutMinutes);
        List<Long> orderIds = ordersMapper.listExpiredPendingPaymentIds(deadline);
        for (Long orderId : orderIds) {
            try {
                orderTimeoutService.closeExpiredOrder(orderId);
            } catch (RuntimeException exception) {
                log.error("关闭超时订单失败，orderId={}", orderId, exception);
            }
        }
    }
}
