package com.seckill.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单信息 VO
 *
 * @author seckill
 * @since 1.0.0
 */
@Data
@Schema(description = "订单信息")
public class OrderVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private Long orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "商品ID")
    private Long goodsId;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品图片")
    private String goodsImg;

    @Schema(description = "商品价格")
    private BigDecimal goodsPrice;

    @Schema(description = "购买数量")
    private Integer goodsCount;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "下单渠道")
    private Integer channel;

    @Schema(description = "订单状态")
    private Integer status;

    @Schema(description = "订单状态描述")
    private String statusDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;
}
