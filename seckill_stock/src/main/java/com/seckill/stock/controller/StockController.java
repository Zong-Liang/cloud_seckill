package com.seckill.stock.controller;

import com.seckill.common.result.Result;
import com.seckill.stock.service.GoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 库存管理接口
 * <p>
 * 提供库存扣减、回滚、查询等功能
 * </p>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Tag(name = "库存管理接口", description = "库存相关操作接口")
@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final GoodsService goodsService;

    @Operation(summary = "回滚库存", description = "回滚 Redis 和 MySQL 库存（订单超时/取消时调用）")
    @Parameter(name = "goodsId", description = "商品ID", required = true)
    @Parameter(name = "count", description = "回滚数量", required = true)
    @PostMapping("/rollback")
    public Result<Boolean> rollbackStock(@RequestParam Long goodsId,
            @RequestParam Integer count) {
        log.info("收到库存回滚请求 - goodsId: {}, count: {}", goodsId, count);
        boolean success = goodsService.rollbackStock(goodsId, count);
        return Result.success(success);
    }

    @Operation(summary = "同步扣减库存", description = "同步扣减 MySQL 库存（Redis 扣减后调用）")
    @Parameter(name = "goodsId", description = "商品ID", required = true)
    @Parameter(name = "count", description = "扣减数量", required = true)
    @PostMapping("/sync/deduct")
    public Result<Boolean> syncDeductStock(@RequestParam Long goodsId,
            @RequestParam Integer count) {
        log.info("收到库存同步扣减请求 - goodsId: {}, count: {}", goodsId, count);
        boolean success = goodsService.deductMySQLStock(goodsId, count);
        return Result.success(success);
    }

    @Operation(summary = "查询商品库存", description = "查询 Redis 缓存中的库存")
    @Parameter(name = "goodsId", description = "商品ID", required = true)
    @GetMapping("/{goodsId}")
    public Result<Long> getStock(@PathVariable Long goodsId) {
        Long stock = goodsService.getRedisStock(goodsId);
        return Result.success(stock);
    }

    @Operation(summary = "查询商品详情库存", description = "查询商品详情和库存信息")
    @Parameter(name = "goodsId", description = "商品ID", required = true)
    @GetMapping("/detail/{goodsId}")
    public Result<?> getStockDetail(@PathVariable Long goodsId) {
        return Result.success(goodsService.getById(goodsId));
    }
}
