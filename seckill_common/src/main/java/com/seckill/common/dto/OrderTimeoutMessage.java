package com.seckill.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * 订单超时消息 DTO
 * <p>
 * 使用 Java 8 特性：
 * </p>
 * <ul>
 * <li>Builder 模式构建对象</li>
 * <li>Objects.requireNonNull 校验</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTimeoutMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单号
     */
    private Long orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 购买数量
     */
    private Integer count;

    /**
     * 消息时间戳
     */
    private Long timestamp;

    /**
     * 校验必填字段
     */
    public void validate() {
        Objects.requireNonNull(orderNo, "orderNo cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(goodsId, "goodsId cannot be null");
    }

    /**
     * 静态工厂方法
     */
    public static OrderTimeoutMessage of(Long orderNo, Long userId, Long goodsId, Integer count) {
        return OrderTimeoutMessage.builder()
                .orderNo(orderNo)
                .userId(userId)
                .goodsId(goodsId)
                .count(count)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
