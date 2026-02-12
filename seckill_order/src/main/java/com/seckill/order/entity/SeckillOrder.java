package com.seckill.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单实体
 *
 * @author seckill
 * @since 1.0.0
 */
@Data
@TableName("seckill_order")
public class SeckillOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号(雪花算法)
     * 使用 ToStringSerializer 避免前端 JavaScript 精度丢失
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 商品名称(快照)
     */
    private String goodsName;

    /**
     * 商品图片(快照)
     */
    private String goodsImg;

    /**
     * 秒杀价格(快照)
     */
    private BigDecimal goodsPrice;

    /**
     * 购买数量
     */
    private Integer goodsCount;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 下单渠道: 1-PC, 2-Android, 3-iOS, 4-小程序
     */
    private Integer channel;

    /**
     * 订单状态: 0-待支付, 1-已支付, 2-已发货, 3-已收货, 4-已取消, 5-已超时
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
