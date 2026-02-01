package com.seckill.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一状态码枚举
 * <p>
 * 状态码规范:
 * - 200: 成功
 * - 4xx: 客户端错误
 * - 5xx: 服务端错误
 * - 1001-1099: 库存相关业务错误
 * - 1101-1199: 订单相关业务错误
 * - 1201-1299: 限流相关错误
 * - 1301-1399: 用户相关错误
 * </p>
 *
 * @author seckill
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // ==================== 成功 ====================
    SUCCESS(200, "操作成功"),

    // ==================== 客户端错误 4xx ====================
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    // ==================== 服务端错误 5xx ====================
    SYSTEM_ERROR(500, "系统繁忙，请稍后重试"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),

    // ==================== 库存相关 1001-1099 ====================
    GOODS_NOT_EXIST(1001, "商品不存在"),
    STOCK_NOT_ENOUGH(1002, "库存不足"),
    ACTIVITY_NOT_STARTED(1003, "活动尚未开始"),
    ACTIVITY_ENDED(1004, "活动已结束"),
    GOODS_OFF_SHELF(1005, "商品已下架"),

    // ==================== 订单相关 1101-1199 ====================
    ORDER_NOT_EXIST(1101, "订单不存在"),
    ORDER_ALREADY_PAID(1102, "订单已支付"),
    ORDER_CANCELLED(1103, "订单已取消"),
    REPEAT_ORDER(1104, "请勿重复下单"),
    ORDER_TIMEOUT(1105, "订单已超时"),
    ORDER_STATUS_ERROR(1106, "订单状态异常"),

    // ==================== 限流相关 1201-1299 ====================
    RATE_LIMIT(1201, "访问过于频繁，请稍后重试"),
    SYSTEM_BUSY(1202, "系统繁忙，请稍后重试"),
    SERVICE_DEGRADED(1203, "服务降级中，请稍后重试"),

    // ==================== 用户相关 1301-1399 ====================
    USER_NOT_EXIST(1301, "用户不存在"),
    USER_EXIST(1302, "用户名已存在"),
    PASSWORD_ERROR(1303, "密码错误"),
    USER_DISABLED(1304, "用户已被禁用"),
    PHONE_EXIST(1305, "手机号已被注册"),
    TOKEN_INVALID(1306, "Token无效或已过期"),
    TOKEN_EXPIRED(1307, "Token已过期，请重新登录");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态信息
     */
    private final String message;
}
