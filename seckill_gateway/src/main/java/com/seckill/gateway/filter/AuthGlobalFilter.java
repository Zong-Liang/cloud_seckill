package com.seckill.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.seckill.gateway.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 认证全局过滤器
 * <p>
 * 校验 Token 并将用户信息传递给下游服务
 * </p>
 * <p>
 * 增强功能：
 * </p>
 * <ul>
 * <li>完整的 JWT Token 验证（签名、过期时间）</li>
 * <li>将用户ID解析后传递给下游服务</li>
 * <li>详细的错误信息返回</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    @Value("${jwt.secret:seckill-jwt-secret-key-please-change-in-production-2024-very-long-key}")
    private String jwtSecret;

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_NAME_HEADER = "X-User-Name";
    private static final String USER_TOKEN_HEADER = "X-User-Token";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.debug("Gateway 接收到请求: {} {}", request.getMethod(), path);

        // 1. 白名单放行
        if (isWhitelisted(path)) {
            log.debug("白名单放行: {}", path);
            return chain.filter(exchange);
        }

        // 2. 获取 Token
        String token = request.getHeaders().getFirst(TOKEN_HEADER);
        if (StrUtil.isBlank(token)) {
            log.warn("请求未携带 Token: {}", path);
            return unauthorized(exchange, "请先登录", 401);
        }

        // 去掉 Bearer 前缀
        if (token.startsWith(TOKEN_PREFIX)) {
            token = token.substring(TOKEN_PREFIX.length());
        }

        // 3. 验证 Token
        TokenValidationResult validationResult = validateToken(token);
        if (!validationResult.isValid()) {
            log.warn("Token 验证失败: {} - {}", path, validationResult.getError());
            return unauthorized(exchange, validationResult.getError(), validationResult.getStatusCode());
        }

        // 4. 将用户信息传递给下游服务
        ServerHttpRequest newRequest = request.mutate()
                .header(USER_TOKEN_HEADER, token)
                .header(USER_ID_HEADER, String.valueOf(validationResult.getUserId()))
                .header(USER_NAME_HEADER, validationResult.getUsername())
                .build();

        log.debug("Token 校验通过，用户ID: {}, 转发请求: {}", validationResult.getUserId(), path);
        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    /**
     * 验证 Token
     *
     * @param token JWT Token
     * @return 验证结果
     */
    private TokenValidationResult validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 检查是否过期
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                return TokenValidationResult.invalid("Token已过期，请重新登录", 401);
            }

            // 提取用户信息
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);

            return TokenValidationResult.valid(userId, username);

        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", e.getMessage());
            return TokenValidationResult.invalid("Token已过期，请重新登录", 401);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("Token 签名无效: {}", e.getMessage());
            return TokenValidationResult.invalid("Token无效", 401);
        } catch (Exception e) {
            log.warn("Token 解析失败: {}", e.getMessage());
            return TokenValidationResult.invalid("Token无效或已损坏", 401);
        }
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhitelisted(String path) {
        List<String> whitelist = authProperties.getWhitelist();
        if (whitelist == null || whitelist.isEmpty()) {
            return false;
        }
        return whitelist.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message, int code) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String body = String.format(
                "{\"code\":%d,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
                code, message, System.currentTimeMillis());
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 优先级最高
        return -100;
    }

    /**
     * Token 验证结果内部类
     */
    private static class TokenValidationResult {
        private final boolean valid;
        private final Long userId;
        private final String username;
        private final String error;
        private final int statusCode;

        private TokenValidationResult(boolean valid, Long userId, String username, String error, int statusCode) {
            this.valid = valid;
            this.userId = userId;
            this.username = username;
            this.error = error;
            this.statusCode = statusCode;
        }

        public static TokenValidationResult valid(Long userId, String username) {
            return new TokenValidationResult(true, userId, username, null, 200);
        }

        public static TokenValidationResult invalid(String error, int statusCode) {
            return new TokenValidationResult(false, null, null, error, statusCode);
        }

        public boolean isValid() {
            return valid;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getError() {
            return error;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
