package com.seckill.common.constant;

/**
 * 商品状态常量
 *
 * @author seckill
 * @since 1.0.0
 */
public interface GoodsStatus {

    /**
     * 未开始
     */
    int NOT_STARTED = 0;

    /**
     * 进行中
     */
    int ONGOING = 1;

    /**
     * 已结束
     */
    int ENDED = 2;

    /**
     * 已下架
     */
    int OFF_SHELF = 3;
}
