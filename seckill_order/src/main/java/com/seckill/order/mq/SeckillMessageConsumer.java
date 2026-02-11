package com.seckill.order.mq;

import com.alibaba.fastjson.JSON;
import com.seckill.common.constant.OrderStatus;
import com.seckill.common.dto.SeckillMessage;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 秒杀消息消费者
 * <p>
 * 使用 Java 8 特性重构：
 * </p>
 * <ul>
 * <li>Optional 处理空值和默认值</li>
 * <li>Supplier 延迟计算</li>
 * <li>Lambda 简化代码</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "${seckill.mq.topic:seckill-order-topic}", consumerGroup = "seckill-order-consumer-group")
public class SeckillMessageConsumer implements RocketMQListener<String> {

    private final OrderService orderService;
    private final OrderTimeoutProducer orderTimeoutProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(String messageBody) {
        log.info("收到秒杀消息: {}", messageBody);

        // 使用 Supplier 延迟解析
        Supplier<SeckillMessage> messageSupplier = () -> JSON.parseObject(messageBody, SeckillMessage.class);

        try {
            SeckillMessage message = Optional.ofNullable(messageSupplier.get())
                    .orElseThrow(() -> new IllegalArgumentException("消息解析失败"));

            // 幂等性校验
            if (isOrderExists(message.getUserId(), message.getGoodsId())) {
                log.warn("订单已存在，跳过 - userId: {}, goodsId: {}",
                        message.getUserId(), message.getGoodsId());
                return;
            }

            // 创建订单
            SeckillOrder order = buildOrder(message);
            orderService.save(order);

            log.info("创建订单成功 - orderNo: {}, userId: {}, goodsId: {}",
                    order.getOrderNo(), order.getUserId(), order.getGoodsId());

            // 发送订单超时延时消息（未支付则自动取消）
            orderTimeoutProducer.sendTimeoutMessage(
                    order.getOrderNo(), order.getUserId(),
                    order.getGoodsId(), order.getGoodsCount());

        } catch (Exception e) {
            log.error("处理秒杀消息失败: {}", messageBody, e);
            throw e;
        }
    }

    /**
     * 检查订单是否已存在
     */
    private boolean isOrderExists(Long userId, Long goodsId) {
        return orderService.hasOrder(userId, goodsId);
    }

    /**
     * 构建订单（使用 Optional 处理可选字段）
     */
    private SeckillOrder buildOrder(SeckillMessage message) {
        SeckillOrder order = new SeckillOrder();

        // 使用 Optional 处理可选字段和默认值
        order.setOrderNo(message.getOrderNo());
        order.setUserId(message.getUserId());
        order.setGoodsId(message.getGoodsId());
        order.setGoodsName(message.getGoodsName());
        order.setGoodsPrice(message.getSeckillPrice());
        order.setGoodsImg(message.getGoodsImg());

        // 使用 Optional 处理默认值
        Integer count = Optional.ofNullable(message.getCount()).orElse(1);
        order.setGoodsCount(count);

        // 计算总金额
        order.setTotalAmount(message.getSeckillPrice().multiply(java.math.BigDecimal.valueOf(count)));

        // 处理渠道字段（String 转 Integer）
        order.setChannel(parseChannel(message.getChannel()));

        order.setStatus(OrderStatus.UNPAID);
        order.setCreateTime(LocalDateTime.now());

        return order;
    }

    /**
     * 解析渠道字符串为整数
     */
    private Integer parseChannel(String channel) {
        return Optional.ofNullable(channel)
                .filter(c -> !c.isEmpty())
                .map(c -> {
                    switch (c.toUpperCase()) {
                        case "PC":
                            return 1;
                        case "ANDROID":
                            return 2;
                        case "IOS":
                            return 3;
                        case "WECHAT":
                            return 4;
                        default:
                            try {
                                return Integer.parseInt(c);
                            } catch (NumberFormatException e) {
                                return 1;
                            }
                    }
                })
                .orElse(1);
    }
}
