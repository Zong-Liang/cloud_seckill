package com.seckill.stock.controller;

import com.seckill.common.result.Result;
import com.seckill.stock.service.GoodsService;
import com.seckill.stock.vo.GoodsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品接口
 *
 * @author seckill
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "商品接口", description = "秒杀商品查询相关接口")
@RestController
@RequestMapping("/stock/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    @Operation(summary = "获取进行中的秒杀商品列表")
    @GetMapping("/list")
    public Result<List<GoodsVO>> listOngoingGoods() {
        return Result.success(goodsService.listOngoingGoods());
    }

    @Operation(summary = "获取所有秒杀商品列表")
    @GetMapping("/all")
    public Result<List<GoodsVO>> listAllGoods() {
        return Result.success(goodsService.listAllGoods());
    }

    @Operation(summary = "获取商品详情")
    @Parameter(name = "id", description = "商品ID", required = true)
    @GetMapping("/{id}")
    public Result<GoodsVO> getById(@PathVariable("id") Long id) {
        return Result.success(goodsService.getGoodsById(id));
    }

    @Operation(summary = "扣减库存（内部调用）")
    @Parameter(name = "goodsId", description = "商品ID", required = true)
    @Parameter(name = "count", description = "扣减数量", required = true)
    @PostMapping("/deduct")
    public Result<Boolean> deductStock(@RequestParam("goodsId") Long goodsId,
            @RequestParam(value = "count", defaultValue = "1") Integer count) {
        return Result.success(goodsService.deductStock(goodsId, count));
    }

    @Operation(summary = "回滚库存（内部调用）")
    @Parameter(name = "goodsId", description = "商品ID", required = true)
    @Parameter(name = "count", description = "回滚数量", required = true)
    @PostMapping("/rollback")
    public Result<Boolean> rollbackStock(@RequestParam("goodsId") Long goodsId,
            @RequestParam(value = "count", defaultValue = "1") Integer count) {
        return Result.success(goodsService.rollbackStock(goodsId, count));
    }
}
