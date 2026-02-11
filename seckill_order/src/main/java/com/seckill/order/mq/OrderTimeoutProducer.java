package com.seckill.order.mq;

import com.alibaba.fastjson.JSON;
import com.seckill.common.dto.OrderTimeoutMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 订单超时消息生产者
 * <p>
 * 发送延时消息用于订单超时检测
 * </p>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutProducer {

    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 订单超时 Topic
     */
    @Value("${seckill.mq.timeout-topic:order-timeout-topic}")
    private String timeoutTopic;

    /**
     * 订单超时时间（分钟）
     */
    @Value("${seckill.order.timeout-minutes:15}")
    private int timeoutMinutes;

    @Value("${seckill.order.timeout-delay-level:14}")
    private int timeoutDelayLevel;

    /**
     * 发送订单超时检测消息
     * <p>
     * 延迟级别对应关系（RocketMQ 4.x）：
     * </p>
     * <ul>
     * <li>1 = 1s</li>
     * <li>2 = 5s</li>
     * <li>3 = 10s</li>
     * <li>4 = 30s</li>
     * <li>5 = 1m</li>
     * <li>6 = 2m</li>
     * <li>7 = 3m</li>
     * <li>8 = 4m</li>
     * <li>9 = 5m</li>
     * <li>10 = 6m</li>
     * <li>11 = 7m</li>
     * <li>12 = 8m</li>
     * <li>13 = 9m</li>
     * <li>14 = 10m</li>
     * <li>15 = 20m</li>
     * <li>16 = 30m</li>
     * </ul>
     *
     * @param orderNo 订单号
     * @param userId  用户ID
     * @param goodsId 商品ID
     * @param count   购买数量
     */
    public void sendTimeoutMessage(Long orderNo, Long userId, Long goodsId, Integer count) {
        OrderTimeoutMessage message = new OrderTimeoutMessage();
        message.setOrderNo(orderNo);
        message.setUserId(userId);
        message.setGoodsId(goodsId);
        message.setCount(count);
        message.setTimestamp(System.currentTimeMillis());

        try {
            String msgBody = JSON.toJSONString(message);

            // 使用配置的延迟级别（优先于计算值）
            int delayLevel = timeoutDelayLevel;

            rocketMQTemplate.syncSend(
                    timeoutTopic,
                    MessageBuilder.withPayload(msgBody)
                            .setHeader("KEYS", String.valueOf(orderNo))
                            .build(),
                    3000, // 发送超时时间
                    delayLevel // 延迟级别
            );

            log.info("发送订单超时检测消息成功 - orderNo: {}, delayLevel: {}",
                    orderNo, delayLevel);

        } catch (Exception e) {
            log.error("发送订单超时检测消息失败 - orderNo: {}, error: {}", orderNo, e.getMessage(), e);
            // 失败不抛异常，不影响订单创建；后续由补偿任务处理
        }
    }

    /**
     * 根据超时分钟数获取延迟级别
     *
     * @param minutes 分钟数
     * @return RocketMQ 延迟级别
     */
    private int getDelayLevel(int minutes) {
        // RocketMQ 4.x 延迟级别映射
        if (minutes <= 1)
            return 5; // 1m
        if (minutes <= 2)
            return 6; // 2m
        if (minutes <= 3)
            return 7; // 3m
        if (minutes <= 4)
            return 8; // 4m
        if (minutes <= 5)
            return 9; // 5m
        if (minutes <= 6)
            return 10; // 6m
        if (minutes <= 7)
            return 11; // 7m
        if (minutes <= 8)
            return 12; // 8m
        if (minutes <= 9)
            return 13; // 9m
        if (minutes <= 10)
            return 14; // 10m
        if (minutes <= 20)
            return 15; // 20m
        return 16; // 30m
    }
}
