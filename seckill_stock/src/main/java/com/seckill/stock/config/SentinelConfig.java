package com.seckill.stock.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 限流熔断配置
 * <p>
 * 配置流量控制、熔断降级规则
 * </p>
 * 
 * <p>
 * 规则说明：
 * </p>
 * <ul>
 * <li>秒杀接口 QPS 限制为 500</li>
 * <li>热点商品参数限流</li>
 * <li>异常比例熔断</li>
 * </ul>
 *
 * @author seckill
 * @since 3.0.0
 */
@Slf4j
@Configuration
public class SentinelConfig {

    /**
     * 秒杀接口 QPS 限制
     */
    @Value("${sentinel.flow.seckill-qps:500}")
    private int seckillQps;

    /**
     * 商品查询接口 QPS 限制
     */
    @Value("${sentinel.flow.goods-qps:1000}")
    private int goodsQps;

    /**
     * 初始化限流规则
     */
    @PostConstruct
    public void initFlowRules() {
        log.info("初始化 Sentinel 限流规则...");

        List<FlowRule> rules = new ArrayList<>();

        // 1. 秒杀接口限流规则
        FlowRule seckillRule = new FlowRule();
        seckillRule.setResource("doSeckill");
        seckillRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        seckillRule.setCount(seckillQps);
        seckillRule.setLimitApp("default");
        // 使用令牌桶算法
        seckillRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);
        seckillRule.setWarmUpPeriodSec(10);
        rules.add(seckillRule);

        // 2. 商品列表接口限流规则
        FlowRule goodsListRule = new FlowRule();
        goodsListRule.setResource("listGoods");
        goodsListRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        goodsListRule.setCount(goodsQps);
        goodsListRule.setLimitApp("default");
        rules.add(goodsListRule);

        // 3. 库存查询接口限流规则
        FlowRule stockQueryRule = new FlowRule();
        stockQueryRule.setResource("getStock");
        stockQueryRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        stockQueryRule.setCount(goodsQps);
        stockQueryRule.setLimitApp("default");
        rules.add(stockQueryRule);

        FlowRuleManager.loadRules(rules);
        log.info("Sentinel 限流规则初始化完成, 秒杀QPS: {}, 商品查询QPS: {}", seckillQps, goodsQps);

        // 初始化热点参数限流
        initParamFlowRules();

        // 初始化熔断规则
        initDegradeRules();
    }

    /**
     * 初始化热点参数限流规则
     * <p>
     * 针对热门商品进行精细化限流
     * </p>
     */
    private void initParamFlowRules() {
        List<ParamFlowRule> rules = new ArrayList<>();

        // 秒杀接口按 goodsId 参数限流
        ParamFlowRule rule = new ParamFlowRule("doSeckillByGoodsId")
                .setParamIdx(0) // 第一个参数
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(100); // 单个商品 QPS 限制

        rules.add(rule);
        ParamFlowRuleManager.loadRules(rules);
        log.info("Sentinel 热点参数限流规则初始化完成");
    }

    /**
     * 初始化熔断降级规则
     * <p>
     * 当异常比例超过阈值时触发熔断
     * </p>
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 秒杀接口熔断规则：异常比例超过 50% 时熔断 10 秒
        DegradeRule seckillDegradeRule = new DegradeRule("doSeckill")
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setCount(0.5) // 异常比例阈值 50%
                .setTimeWindow(10) // 熔断时长 10 秒
                .setMinRequestAmount(10) // 最小请求数
                .setStatIntervalMs(10000); // 统计时长 10 秒

        rules.add(seckillDegradeRule);
        DegradeRuleManager.loadRules(rules);
        log.info("Sentinel 熔断规则初始化完成");
    }
}
