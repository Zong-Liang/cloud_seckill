package com.seckill.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关全局异常处理器
 *
 * @author seckill
 * @since 1.0.0
 */
@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 设置响应头
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 确定状态码和错误消息
        int code;
        String message;

        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            code = rse.getStatusCode().value();
            message = rse.getReason();
            response.setStatusCode(rse.getStatusCode());
        } else {
            code = HttpStatus.INTERNAL_SERVER_ERROR.value();
            message = "系统繁忙，请稍后重试";
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        log.error("网关异常: {} - {}", exchange.getRequest().getURI().getPath(), ex.getMessage(), ex);

        // 构建响应体
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("序列化异常响应失败", e);
            return Mono.error(e);
        }
    }
}
