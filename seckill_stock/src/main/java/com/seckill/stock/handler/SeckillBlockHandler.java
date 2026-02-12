package com.seckill.stock.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.seckill.common.result.Result;
import com.seckill.common.result.ResultCode;
import com.seckill.stock.dto.SeckillRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Sentinel 限流降级处理器
 * <p>
 * 当触发限流或熔断时的处理方法
 * </p>
 * 
 * <p>
 * 使用说明：
 * </p>
 * <ul>
 * <li>方法必须为 static</li>
 * <li>参数列表需要和原方法一致，最后加 BlockException</li>
 * <li>返回类型需要和原方法一致</li>
 * </ul>
 *
 * @author seckill
 * @since 3.0.0
 */
@Slf4j
public class SeckillBlockHandler {

    /**
     * 秒杀接口限流处理
     *
     * @param request   秒杀请求
     * @param exception 限流异常
     * @return 友好提示
     */
    public static Result<String> doSeckillBlockHandler(SeckillRequest request, BlockException exception) {
        log.warn("秒杀接口触发限流 - userId: {}, goodsId: {}, rule: {}",
                request.getUserId(), request.getGoodsId(), exception.getRule());
        return Result.error(ResultCode.SYSTEM_BUSY);
    }

    /**
     * 秒杀接口熔断降级处理
     *
     * @param request   秒杀请求
     * @param throwable 异常
     * @return 降级响应
     */
    public static Result<String> doSeckillFallback(SeckillRequest request, Throwable throwable) {
        log.error("秒杀接口触发熔断 - userId: {}, goodsId: {}, error: {}",
                request.getUserId(), request.getGoodsId(), throwable.getMessage());
        return Result.error(ResultCode.SERVICE_DEGRADED);
    }

    /**
     * 热点商品限流处理
     *
     * @param goodsId   商品ID
     * @param exception 限流异常
     * @return 友好提示
     */
    public static Result<Long> hotGoodsBlockHandler(Long goodsId, BlockException exception) {
        log.warn("热门商品触发限流 - goodsId: {}, rule: {}", goodsId, exception.getRule());
        return Result.error(ResultCode.RATE_LIMIT.getCode(), "该商品太火爆了，请稍后再试");
    }
}
