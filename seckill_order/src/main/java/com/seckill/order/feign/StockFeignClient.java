package com.seckill.order.feign;

import com.seckill.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 库存服务 Feign 客户端
 * <p>
 * 订单服务调用库存服务的远程接口
 * </p>
 * 
 * <p>
 * 服务降级说明：
 * </p>
 * <ul>
 * <li>使用 fallbackFactory 实现降级</li>
 * <li>降级时返回错误信息，不会阻塞主流程</li>
 * <li>需配合重试机制和补偿任务</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@FeignClient(name = "seckill-stock", fallbackFactory = StockFeignFallbackFactory.class)
public interface StockFeignClient {

    /**
     * 回滚库存
     * <p>
     * 当订单取消或超时时调用，回滚 Redis 和 MySQL 库存
     * </p>
     *
     * @param goodsId 商品ID
     * @param count   回滚数量
     * @return 操作结果
     */
    @PostMapping("/stock/rollback")
    Result<Boolean> rollbackStock(@RequestParam("goodsId") Long goodsId,
            @RequestParam("count") Integer count);

    /**
     * 同步库存到 MySQL
     * <p>
     * Redis 扣减成功后，异步同步到 MySQL
     * </p>
     *
     * @param goodsId 商品ID
     * @param count   扣减数量
     * @return 操作结果
     */
    @PostMapping("/stock/sync/deduct")
    Result<Boolean> syncDeductStock(@RequestParam("goodsId") Long goodsId,
            @RequestParam("count") Integer count);
}
