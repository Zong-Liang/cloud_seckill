package com.seckill.stock.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.seckill.common.result.Result;
import com.seckill.stock.dto.SeckillRequest;
import com.seckill.stock.handler.SeckillBlockHandler;
import com.seckill.stock.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 秒杀接口
 * <p>
 * 核心秒杀业务接口，集成 Sentinel 限流熔断
 * </p>
 *
 * @author seckill
 * @since 3.0.0
 */
@Slf4j
@Tag(name = "秒杀接口", description = "秒杀核心业务接口")
@RestController
@RequestMapping("/stock/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    /**
     * 执行秒杀（带限流保护）
     * <p>
     * 限流规则：
     * <ul>
     * <li>QPS 限制：500</li>
     * <li>热点参数限流：单商品 QPS 100</li>
     * <li>熔断：异常比例超 50% 时熔断 10 秒</li>
     * </ul>
     *
     * @param request 秒杀请求
     * @return 订单号
     */
    @Operation(summary = "执行秒杀", description = "核心秒杀接口，返回订单号（带限流保护）")
    @PostMapping("/do")
    @SentinelResource(value = "doSeckill", blockHandler = "doSeckillBlockHandler", blockHandlerClass = SeckillBlockHandler.class, fallback = "doSeckillFallback", fallbackClass = SeckillBlockHandler.class)
    public Result<String> doSeckill(@Valid @RequestBody SeckillRequest request) {
        log.info("秒杀请求 - userId: {}, goodsId: {}", request.getUserId(), request.getGoodsId());
        Long orderNo = seckillService.doSeckill(request);
        // Long → String 避免前端 JavaScript 精度丢失（雪花 ID 超过 Number.MAX_SAFE_INTEGER）
        return Result.success(String.valueOf(orderNo));
    }

    @Operation(summary = "初始化商品库存", description = "将商品库存加载到Redis缓存")
    @Parameter(name = "goodsId", description = "商品ID", required = true)
    @PostMapping("/init/{goodsId}")
    public Result<Void> initStock(@PathVariable("goodsId") Long goodsId) {
        seckillService.initGoodsStock(goodsId);
        return Result.success();
    }

    @Operation(summary = "初始化所有商品库存", description = "将所有商品库存加载到Redis缓存")
    @PostMapping("/init/all")
    public Result<Void> initAllStock() {
        seckillService.initAllGoodsStock();
        return Result.success();
    }

    @Operation(summary = "检查是否已秒杀", description = "检查用户是否已秒杀该商品")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @Parameter(name = "goodsId", description = "商品ID", required = true)
    @GetMapping("/check")
    public Result<Boolean> checkKilled(@RequestParam("userId") Long userId, @RequestParam("goodsId") Long goodsId) {
        boolean killed = seckillService.hasKilled(userId, goodsId);
        return Result.success(killed);
    }
}
