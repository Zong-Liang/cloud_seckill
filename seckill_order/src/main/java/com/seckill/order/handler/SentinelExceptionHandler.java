package com.seckill.order.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.seckill.common.result.Result;
import com.seckill.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sentinel 异常处理器
 * <p>
 * 处理订单服务中的 Sentinel 限流、熔断等异常
 * </p>
 *
 * @author seckill
 * @since 3.0.0
 */
@Slf4j
@Order(-1)
@RestControllerAdvice
public class SentinelExceptionHandler {

    /**
     * 处理限流异常
     */
    @ExceptionHandler(FlowException.class)
    public Result<Void> handleFlowException(FlowException e) {
        log.warn("触发限流规则 - rule: {}", e.getRule());
        return Result.error(ResultCode.RATE_LIMIT);
    }

    /**
     * 处理熔断降级异常
     */
    @ExceptionHandler(DegradeException.class)
    public Result<Void> handleDegradeException(DegradeException e) {
        log.warn("触发熔断降级规则 - rule: {}", e.getRule());
        return Result.error(ResultCode.SERVICE_DEGRADED);
    }

    /**
     * 处理热点参数限流异常
     */
    @ExceptionHandler(ParamFlowException.class)
    public Result<Void> handleParamFlowException(ParamFlowException e) {
        log.warn("触发热点参数限流 - rule: {}", e.getRule());
        return Result.error(ResultCode.RATE_LIMIT.getCode(), "请求过于频繁");
    }

    /**
     * 处理授权异常
     */
    @ExceptionHandler(AuthorityException.class)
    public Result<Void> handleAuthorityException(AuthorityException e) {
        log.warn("触发授权规则 - rule: {}", e.getRule());
        return Result.error(ResultCode.FORBIDDEN);
    }

    /**
     * 处理所有 BlockException
     */
    @ExceptionHandler(BlockException.class)
    public Result<Void> handleBlockException(BlockException e) {
        log.warn("触发 Sentinel 规则 - rule: {}", e.getRule());
        return Result.error(ResultCode.SYSTEM_BUSY);
    }
}
