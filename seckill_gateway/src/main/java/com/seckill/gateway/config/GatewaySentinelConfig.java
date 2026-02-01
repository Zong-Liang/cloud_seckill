package com.seckill.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;

import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sentinel 网关限流配置
 * <p>
 * 在网关层实现流量控制，保护后端服务
 * </p>
 * 
 * <p>
 * 限流策略：
 * </p>
 * <ul>
 * <li>秒杀 API 组：QPS 1000</li>
 * <li>订单 API 组：QPS 500</li>
 * <li>商品 API 组：QPS 2000</li>
 * </ul>
 *
 * @author seckill
 * @since 3.0.0
 */
@Slf4j
@Configuration
public class GatewaySentinelConfig {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public GatewaySentinelConfig(ObjectProvider<List<ViewResolver>> viewResolversProvider,
            ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    /**
     * 配置 Sentinel Gateway 异常处理器
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    // SentinelGatewayFilter 由 SentinelSCGAutoConfiguration 自动配置提供
    // 无需手动定义，避免 Bean 冲突

    /**
     * 初始化网关限流规则
     */
    @PostConstruct
    public void initGatewayRules() {
        log.info("初始化网关 Sentinel 限流规则...");

        // 初始化 API 分组
        initApiDefinitions();

        // 初始化限流规则
        initFlowRules();

        log.info("网关 Sentinel 限流规则初始化完成");
    }

    /**
     * 定义 API 分组
     */
    private void initApiDefinitions() {
        Set<ApiDefinition> definitions = new HashSet<>();

        // 秒杀 API 组
        ApiDefinition seckillApi = new ApiDefinition("seckill_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {
                    {
                        add(new ApiPathPredicateItem()
                                .setPattern("/stock/seckill/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                    }
                });
        definitions.add(seckillApi);

        // 订单 API 组
        ApiDefinition orderApi = new ApiDefinition("order_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {
                    {
                        add(new ApiPathPredicateItem()
                                .setPattern("/order/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                    }
                });
        definitions.add(orderApi);

        // 商品 API 组
        ApiDefinition goodsApi = new ApiDefinition("goods_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {
                    {
                        add(new ApiPathPredicateItem()
                                .setPattern("/stock/goods/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                    }
                });
        definitions.add(goodsApi);

        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
        log.info("加载 API 分组定义: {}", definitions.size());
    }

    /**
     * 初始化限流规则
     */
    private void initFlowRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // 秒杀 API 限流：QPS 1000
        rules.add(new GatewayFlowRule("seckill_api")
                .setCount(1000)
                .setIntervalSec(1));

        // 订单 API 限流：QPS 500
        rules.add(new GatewayFlowRule("order_api")
                .setCount(500)
                .setIntervalSec(1));

        // 商品 API 限流：QPS 2000
        rules.add(new GatewayFlowRule("goods_api")
                .setCount(2000)
                .setIntervalSec(1));

        // 针对具体路由的限流
        rules.add(new GatewayFlowRule("seckill-stock")
                .setCount(1500)
                .setIntervalSec(1));

        rules.add(new GatewayFlowRule("seckill-order")
                .setCount(800)
                .setIntervalSec(1));

        GatewayRuleManager.loadRules(rules);
        log.info("加载网关限流规则: {}", rules.size());
    }
}
