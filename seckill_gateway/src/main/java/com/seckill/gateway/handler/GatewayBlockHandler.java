package com.seckill.gateway.handler;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关限流响应处理器
 * <p>
 * 自定义限流时的响应内容，返回统一格式的 JSON 响应
 * </p>
 *
 * @author seckill
 * @since 3.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayBlockHandler {

    private final ObjectMapper objectMapper;

    /**
     * 初始化自定义限流响应
     */
    @PostConstruct
    public void init() {
        BlockRequestHandler blockRequestHandler = new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
                log.warn("网关限流触发 - path: {}, error: {}",
                        exchange.getRequest().getPath(), t.getMessage());

                Map<String, Object> response = new HashMap<>();
                response.put("code", 1201);
                response.put("message", "系统繁忙，请稍后重试");
                response.put("data", null);
                response.put("timestamp", System.currentTimeMillis());

                String body;
                try {
                    body = objectMapper.writeValueAsString(response);
                } catch (JsonProcessingException e) {
                    body = "{\"code\":1201,\"message\":\"系统繁忙\"}";
                }

                return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body);
            }
        };

        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
        log.info("网关限流响应处理器初始化完成");
    }
}
