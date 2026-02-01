package com.seckill.stock.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品视图对象
 *
 * @author seckill
 * @since 1.0.0
 */
@Data
@Schema(description = "商品信息")
public class GoodsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品标题")
    private String goodsTitle;

    @Schema(description = "商品图片")
    private String goodsImg;

    @Schema(description = "商品详情")
    private String goodsDetail;

    @Schema(description = "商品原价")
    private BigDecimal goodsPrice;

    @Schema(description = "秒杀价格")
    private BigDecimal seckillPrice;

    @Schema(description = "库存数量")
    private Integer stockCount;

    @Schema(description = "秒杀开始时间")
    private LocalDateTime startTime;

    @Schema(description = "秒杀结束时间")
    private LocalDateTime endTime;

    @Schema(description = "状态: 0-未开始, 1-进行中, 2-已结束, 3-已下架")
    private Integer status;

    @Schema(description = "活动状态描述")
    private String statusDesc;

    @Schema(description = "折扣百分比")
    private Integer discountPercent;
}
