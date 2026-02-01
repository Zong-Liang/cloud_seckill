package com.seckill.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 秒杀请求 DTO
 *
 * @author seckill
 * @since 1.0.0
 */
@Data
@Schema(description = "秒杀请求")
public class SeckillRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "购买数量", defaultValue = "1")
    @Min(value = 1, message = "购买数量至少为1")
    private Integer count = 1;

    @Schema(description = "下单渠道 PC/ANDROID/IOS/MINI_PROGRAM", defaultValue = "PC")
    private String channel = "PC";
}
