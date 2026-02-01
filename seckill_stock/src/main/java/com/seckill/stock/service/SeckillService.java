package com.seckill.stock.service;

import com.seckill.stock.dto.SeckillRequest;

/**
 * 秒杀服务接口
 *
 * @author seckill
 * @since 1.0.0
 */
public interface SeckillService {

    /**
     * 执行秒杀
     * <p>
     * 流程：
     * 1. 参数校验
     * 2. 获取分布式锁（防止重复提交）
     * 3. 检查是否已购买
     * 4. 校验商品状态和时间
     * 5. Redis 预扣减库存
     * 6. 发送 MQ 消息异步创建订单
     * 7. 返回秒杀结果
     * </p>
     *
     * @param request 秒杀请求
     * @return 订单号（异步创建中）
     */
    Long doSeckill(SeckillRequest request);

    /**
     * 初始化商品库存到 Redis
     *
     * @param goodsId 商品ID
     */
    void initGoodsStock(Long goodsId);

    /**
     * 初始化所有进行中商品的库存
     */
    void initAllGoodsStock();

    /**
     * 检查用户是否已秒杀该商品
     *
     * @param userId  用户ID
     * @param goodsId 商品ID
     * @return true-已秒杀
     */
    boolean hasKilled(Long userId, Long goodsId);

    /**
     * 标记用户已秒杀
     *
     * @param userId  用户ID
     * @param goodsId 商品ID
     */
    void markKilled(Long userId, Long goodsId);

    /**
     * 清除已秒杀标记
     * <p>
     * 用于秒杀失败回滚时清除标记
     * </p>
     *
     * @param userId  用户ID
     * @param goodsId 商品ID
     */
    void removeKilledMark(Long userId, Long goodsId);
}
