package com.seckill.order.security;

/**
 * 用户上下文（ThreadLocal）
 * <p>
 * 用于在同一请求线程中传递用户信息
 * </p>
 *
 * @author seckill
 * @since 1.0.0
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    /**
     * 设置用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 设置用户名
     */
    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    /**
     * 获取用户名
     */
    public static String getUsername() {
        return USERNAME.get();
    }

    /**
     * 清除上下文信息（请求结束时调用）
     */
    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
    }
}
