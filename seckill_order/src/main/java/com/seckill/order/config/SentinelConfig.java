package com.seckill.order.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 限流熔断配置
 * <p>
 * 订单服务限流规则配置
 * </p>
 *
 * @author seckill
 * @since 3.0.0
 */
@Slf4j
@Configuration
public class SentinelConfig {

    /**
     * 订单查询接口 QPS 限制
     */
    @Value("${sentinel.flow.order-qps:500}")
    private int orderQps;

    /**
     * 支付接口 QPS 限制
     */
    @Value("${sentinel.flow.pay-qps:200}")
    private int payQps;

    /**
     * 初始化限流规则
     */
    @PostConstruct
    public void initFlowRules() {
        log.info("初始化订单服务 Sentinel 限流规则...");

        List<FlowRule> rules = new ArrayList<>();

        // 1. 订单查询接口限流
        FlowRule orderListRule = new FlowRule();
        orderListRule.setResource("listOrders");
        orderListRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        orderListRule.setCount(orderQps);
        orderListRule.setLimitApp("default");
        rules.add(orderListRule);

        // 2. 支付接口限流
        FlowRule payRule = new FlowRule();
        payRule.setResource("payOrder");
        payRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        payRule.setCount(payQps);
        payRule.setLimitApp("default");
        // 使用排队等待模式，避免突发流量
        payRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        payRule.setMaxQueueingTimeMs(500);
        rules.add(payRule);

        FlowRuleManager.loadRules(rules);
        log.info("订单服务限流规则初始化完成 - 订单查询QPS: {}, 支付QPS: {}", orderQps, payQps);

        // 初始化熔断规则
        initDegradeRules();
    }

    /**
     * 初始化熔断降级规则
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 调用库存服务熔断规则：慢调用比例熔断
        DegradeRule stockServiceRule = new DegradeRule("stockServiceCall")
                .setGrade(RuleConstant.DEGRADE_GRADE_RT) // 响应时间
                .setCount(500) // 慢调用阈值 500ms
                .setSlowRatioThreshold(0.5) // 慢调用比例阈值 50%
                .setTimeWindow(10) // 熔断时长 10 秒
                .setMinRequestAmount(10)
                .setStatIntervalMs(10000);

        rules.add(stockServiceRule);
        DegradeRuleManager.loadRules(rules);
        log.info("订单服务熔断规则初始化完成");
    }
}
