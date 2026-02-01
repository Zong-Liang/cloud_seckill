package com.seckill.order.controller;

import com.seckill.common.exception.BusinessException;
import com.seckill.common.result.Result;
import com.seckill.common.result.ResultCode;
import com.seckill.order.feign.StockFeignClient;
import com.seckill.order.security.UserContext;
import com.seckill.order.service.OrderService;
import com.seckill.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单接口
 * <p>
 * 提供订单查询、支付、取消等功能
 * </p>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Tag(name = "订单接口", description = "秒杀订单相关接口")
@RestController
@RequestMapping("/order/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final StockFeignClient stockFeignClient;

    @Operation(summary = "查询当前用户订单列表")
    @GetMapping("/list")
    public Result<List<OrderVO>> listMyOrders() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return Result.success(orderService.listByUserId(userId));
    }

    @Operation(summary = "根据用户ID查询订单列表（内部调用）")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @GetMapping("/list/{userId}")
    public Result<List<OrderVO>> listByUserId(@PathVariable("userId") Long userId) {
        return Result.success(orderService.listByUserId(userId));
    }

    @Operation(summary = "查询订单详情")
    @Parameter(name = "id", description = "订单ID", required = true)
    @GetMapping("/{id}")
    public Result<OrderVO> getById(@PathVariable("id") Long id) {
        return Result.success(orderService.getOrderById(id));
    }

    @Operation(summary = "根据订单号查询订单")
    @Parameter(name = "orderNo", description = "订单号", required = true)
    @GetMapping("/no/{orderNo}")
    public Result<OrderVO> getByOrderNo(@PathVariable("orderNo") Long orderNo) {
        return Result.success(orderService.getOrderByOrderNo(orderNo));
    }

    @Operation(summary = "检查用户是否已下单（内部调用）")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @Parameter(name = "goodsId", description = "商品ID", required = true)
    @GetMapping("/check")
    public Result<Boolean> checkOrder(@RequestParam("userId") Long userId, @RequestParam("goodsId") Long goodsId) {
        return Result.success(orderService.hasOrder(userId, goodsId));
    }

    @Operation(summary = "支付订单（模拟支付）", description = "模拟支付成功，实际项目需对接支付网关")
    @Parameter(name = "orderNo", description = "订单号", required = true)
    @PostMapping("/pay/{orderNo}")
    public Result<Boolean> payOrder(@PathVariable("orderNo") Long orderNo) {
        log.info("收到支付请求 - orderNo: {}", orderNo);
        boolean success = orderService.payOrder(orderNo);
        if (success) {
            return Result.success("支付成功", true);
        }
        return Result.error(ResultCode.SYSTEM_ERROR.getCode(), "支付失败");
    }

    @Operation(summary = "取消订单", description = "取消未支付的订单并回滚库存")
    @Parameter(name = "orderNo", description = "订单号", required = true)
    @PostMapping("/cancel/{orderNo}")
    public Result<Boolean> cancelOrder(@PathVariable("orderNo") Long orderNo) {
        log.info("收到取消订单请求 - orderNo: {}", orderNo);

        // 1. 获取订单信息
        var order = orderService.getByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        // 2. 取消订单
        boolean success = orderService.cancelOrder(orderNo);
        if (success) {
            // 3. 回滚库存
            try {
                stockFeignClient.rollbackStock(order.getGoodsId(), order.getGoodsCount());
                log.info("订单取消成功并回滚库存 - orderNo: {}", orderNo);
            } catch (Exception e) {
                log.error("库存回滚失败，将由补偿任务处理 - orderNo: {}", orderNo, e);
            }
            return Result.success("订单已取消", true);
        }
        return Result.error(ResultCode.SYSTEM_ERROR.getCode(), "取消订单失败");
    }
}
