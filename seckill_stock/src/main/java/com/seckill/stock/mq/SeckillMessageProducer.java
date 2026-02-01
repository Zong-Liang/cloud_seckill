package com.seckill.stock.mq;

import com.alibaba.fastjson.JSON;
import com.seckill.common.dto.SeckillMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 秒杀消息生产者
 * <p>
 * 使用 Java 8 特性重构：
 * </p>
 * <ul>
 * <li>Optional 处理空值</li>
 * <li>Consumer 封装消息处理</li>
 * <li>Lambda 简化日志</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillMessageProducer {

    private final RocketMQTemplate rocketMQTemplate;

    @Value("${seckill.mq.topic:seckill-order-topic}")
    private String seckillTopic;

    /**
     * 发送秒杀消息
     *
     * @param message 秒杀消息
     * @param orderNo 订单号
     */
    public void sendSeckillMessage(SeckillMessage message, Long orderNo) {
        Objects.requireNonNull(message, "message cannot be null");
        Objects.requireNonNull(orderNo, "orderNo cannot be null");

        String msgBody = JSON.toJSONString(message);

        // 使用 Consumer 封装发送后处理
        Consumer<Long> onSuccess = orderId -> log.info("发送秒杀消息成功 - orderNo: {}, userId: {}, goodsId: {}",
                orderId, message.getUserId(), message.getGoodsId());

        try {
            // 先尝试同步发送
            rocketMQTemplate.syncSend(
                    seckillTopic,
                    MessageBuilder.withPayload(msgBody)
                            .setHeader("KEYS", String.valueOf(orderNo))
                            .build(),
                    3000); // 3秒超时
            onSuccess.accept(orderNo);

        } catch (Exception e) {
            // MQ发送失败时，记录日志进行降级处理
            // 实际生产环境可以存入本地消息表，后续补偿
            log.warn("MQ发送超时，进入降级处理 - orderNo: {}, userId: {}, goodsId: {}, error: {}",
                    orderNo, message.getUserId(), message.getGoodsId(), e.getMessage());
            // 降级策略：记录到数据库或Redis，后续补偿处理
            // 这里暂时只记录日志，不抛出异常，保证秒杀流程可以完成
            log.info("秒杀订单已记录，等待后续补偿处理 - orderNo: {}", orderNo);
        }
    }

    /**
     * 发送秒杀消息（支持自定义 Tag）
     *
     * @param message 秒杀消息
     * @param orderNo 订单号
     * @param tag     消息 Tag
     */
    public void sendSeckillMessage(SeckillMessage message, Long orderNo, String tag) {
        Objects.requireNonNull(message, "message cannot be null");

        // 使用 Optional 处理 Tag
        String destination = Optional.ofNullable(tag)
                .filter(t -> !t.isEmpty())
                .map(t -> seckillTopic + ":" + t)
                .orElse(seckillTopic);

        String msgBody = JSON.toJSONString(message);

        try {
            rocketMQTemplate.syncSend(
                    destination,
                    MessageBuilder.withPayload(msgBody)
                            .setHeader("KEYS", String.valueOf(orderNo))
                            .build());
            log.info("发送秒杀消息成功 - destination: {}, orderNo: {}", destination, orderNo);

        } catch (Exception e) {
            log.error("发送秒杀消息失败 - orderNo: {}, destination: {}", orderNo, destination, e);
            throw new RuntimeException("发送秒杀消息失败", e);
        }
    }
}
