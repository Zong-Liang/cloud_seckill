package com.seckill.order.mq;

import com.alibaba.fastjson.JSON;
import com.seckill.common.constant.OrderStatus;
import com.seckill.common.dto.OrderTimeoutMessage;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.feign.StockFeignClient;
import com.seckill.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 订单超时消费者
 * <p>
 * 使用 Java 8 特性重构：
 * </p>
 * <ul>
 * <li>Optional 链式调用</li>
 * <li>Predicate 条件判断</li>
 * <li>Consumer 副作用处理</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "${seckill.mq.timeout-topic:order-timeout-topic}", consumerGroup = "order-timeout-consumer-group")
public class OrderTimeoutConsumer implements RocketMQListener<String> {

    private final OrderService orderService;
    private final StockFeignClient stockFeignClient;

    /**
     * 订单未支付判断条件
     */
    private static final Predicate<SeckillOrder> IS_UNPAID = order -> order.getStatus() == OrderStatus.UNPAID;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(String messageBody) {
        log.info("收到订单超时消息: {}", messageBody);

        try {
            OrderTimeoutMessage message = JSON.parseObject(messageBody, OrderTimeoutMessage.class);

            Optional.ofNullable(message.getOrderNo())
                    .flatMap(orderNo -> Optional.ofNullable(orderService.getByOrderNo(orderNo)))
                    .filter(IS_UNPAID)
                    .ifPresent(this::handleTimeout);

        } catch (Exception e) {
            log.error("处理订单超时消息失败: {}", messageBody, e);
            throw e;
        }
    }

    /**
     * 处理超时（使用 Consumer 封装副作用）
     */
    private void handleTimeout(SeckillOrder order) {
        Long orderNo = order.getOrderNo();

        // 超时处理消费者
        Consumer<SeckillOrder> timeoutHandler = o -> {
            // 1. 更新订单状态为超时
            boolean updated = orderService.updateOrderStatus(o.getId(), OrderStatus.TIMEOUT);

            // 2. 回滚库存
            Optional.of(updated)
                    .filter(Boolean::booleanValue)
                    .ifPresent(success -> {
                        try {
                            stockFeignClient.rollbackStock(o.getGoodsId(), o.getGoodsCount());
                            log.info("订单超时处理完成，库存已回滚 - orderNo: {}, goodsId: {}, count: {}",
                                    orderNo, o.getGoodsId(), o.getGoodsCount());
                        } catch (Exception e) {
                            log.error("回滚库存失败 - orderNo: {}, goodsId: {}", orderNo, o.getGoodsId(), e);
                        }
                    });
        };

        timeoutHandler.accept(order);
        log.info("订单超时取消成功 - orderNo: {}, userId: {}", orderNo, order.getUserId());
    }
}
