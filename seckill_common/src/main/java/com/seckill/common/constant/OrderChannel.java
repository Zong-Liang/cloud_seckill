package com.seckill.common.constant;

/**
 * 下单渠道常量
 *
 * @author seckill
 * @since 1.0.0
 */
public interface OrderChannel {

    /**
     * PC 网页端
     */
    int PC = 1;

    /**
     * Android App
     */
    int ANDROID = 2;

    /**
     * iOS App
     */
    int IOS = 3;

    /**
     * 微信小程序
     */
    int MINI_PROGRAM = 4;
}
