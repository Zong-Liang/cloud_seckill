package com.seckill.order.feign;

import com.seckill.common.result.Result;
import com.seckill.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 库存服务 Feign 降级工厂
 * <p>
 * 当库存服务不可用时，提供降级处理
 * </p>
 * 
 * <p>
 * 降级策略：
 * </p>
 * <ul>
 * <li>记录错误日志</li>
 * <li>返回失败结果，让调用方感知并处理</li>
 * <li>配合重试和补偿机制保证最终一致性</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Component
public class StockFeignFallbackFactory implements FallbackFactory<StockFeignClient> {

    @Override
    public StockFeignClient create(Throwable cause) {
        log.error("库存服务调用失败，触发降级: {}", cause.getMessage());

        return new StockFeignClient() {
            @Override
            public Result<Boolean> rollbackStock(Long goodsId, Integer count) {
                log.error("库存回滚降级处理 - goodsId: {}, count: {}, error: {}",
                        goodsId, count, cause.getMessage());
                return Result.error(ResultCode.SERVICE_UNAVAILABLE.getCode(),
                        "库存服务暂时不可用，回滚操作已记录，稍后自动重试");
            }

            @Override
            public Result<Boolean> syncDeductStock(Long goodsId, Integer count) {
                log.error("库存同步降级处理 - goodsId: {}, count: {}, error: {}",
                        goodsId, count, cause.getMessage());
                return Result.error(ResultCode.SERVICE_UNAVAILABLE.getCode(),
                        "库存服务暂时不可用，同步操作已记录，稍后自动重试");
            }
        };
    }
}
