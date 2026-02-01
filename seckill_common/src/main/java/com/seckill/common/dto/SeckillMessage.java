package com.seckill.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * 秒杀消息 DTO（用于 MQ）
 * <p>
 * 使用 Java 8 特性：
 * </p>
 * <ul>
 * <li>Builder 模式构建对象</li>
 * <li>Optional 处理默认值</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMessage implements Serializable {

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
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品图片
     */
    private String goodsImg;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 购买数量
     */
    private Integer count;

    /**
     * 下单渠道
     */
    private String channel;

    /**
     * 请求时间戳
     */
    private Long timestamp;

    /**
     * 获取购买数量（带默认值）
     */
    public Integer getCountOrDefault() {
        return Optional.ofNullable(count).orElse(1);
    }

    /**
     * 获取渠道（带默认值）
     */
    public String getChannelOrDefault() {
        return Optional.ofNullable(channel).orElse("UNKNOWN");
    }

    /**
     * 计算总金额
     */
    public BigDecimal calculateTotalAmount() {
        return Optional.ofNullable(seckillPrice)
                .map(price -> price.multiply(BigDecimal.valueOf(getCountOrDefault())))
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 校验必填字段
     */
    public void validate() {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(goodsId, "goodsId cannot be null");
    }

    /**
     * 静态工厂方法
     */
    public static SeckillMessage of(Long userId, Long goodsId, Integer count) {
        return SeckillMessage.builder()
                .userId(userId)
                .goodsId(goodsId)
                .count(count)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
