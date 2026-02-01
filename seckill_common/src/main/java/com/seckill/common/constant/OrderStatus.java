package com.seckill.common.constant;

/**
 * 订单状态常量
 *
 * @author seckill
 * @since 1.0.0
 */
public interface OrderStatus {

    /**
     * 待支付
     */
    int UNPAID = 0;

    /**
     * 已支付
     */
    int PAID = 1;

    /**
     * 已发货
     */
    int SHIPPED = 2;

    /**
     * 已收货
     */
    int RECEIVED = 3;

    /**
     * 已取消
     */
    int CANCELLED = 4;

    /**
     * 已超时
     */
    int TIMEOUT = 5;
}
