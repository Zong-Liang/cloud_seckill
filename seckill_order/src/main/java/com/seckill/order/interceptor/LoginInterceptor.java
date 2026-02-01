package com.seckill.order.interceptor;

import com.seckill.order.security.JwtUtil;
import com.seckill.order.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 * <p>
 * 解析 Token 并设置用户上下文
 * </p>
 *
 * @author seckill
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String USER_TOKEN_HEADER = "X-User-Token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 优先从网关传递的 header 获取
        String token = request.getHeader(USER_TOKEN_HEADER);

        // 如果没有，则从 Authorization header 获取
        if (!StringUtils.hasText(token)) {
            String authHeader = request.getHeader(TOKEN_HEADER);
            if (StringUtils.hasText(authHeader) && authHeader.startsWith(TOKEN_PREFIX)) {
                token = authHeader.substring(TOKEN_PREFIX.length());
            }
        }

        // 解析 Token 并设置用户上下文
        if (StringUtils.hasText(token)) {
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);

            if (userId != null) {
                UserContext.setUserId(userId);
                UserContext.setUsername(username);
                log.debug("用户上下文已设置: userId={}, username={}", userId, username);
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        // 请求结束，清除用户上下文
        UserContext.clear();
    }
}
