package com.seckill.stock.service;

/**
 * 分布式锁服务接口
 * <p>
 * 基于 Redis 实现分布式锁，保证并发安全
 * </p>
 *
 * @author seckill
 * @since 1.0.0
 */
public interface DistributedLockService {

    /**
     * 尝试获取锁
     *
     * @param lockKey    锁的 Key
     * @param requestId  请求标识（用于释放锁时校验）
     * @param expireTime 过期时间（秒）
     * @return true-获取成功, false-获取失败
     */
    boolean tryLock(String lockKey, String requestId, long expireTime);

    /**
     * 释放锁
     *
     * @param lockKey   锁的 Key
     * @param requestId 请求标识
     * @return true-释放成功, false-释放失败
     */
    boolean unlock(String lockKey, String requestId);

    /**
     * 获取商品秒杀锁
     *
     * @param goodsId    商品ID
     * @param userId     用户ID
     * @param expireTime 过期时间（秒）
     * @return 请求ID，null表示获取失败
     */
    String lockSeckill(Long goodsId, Long userId, long expireTime);

    /**
     * 释放商品秒杀锁
     *
     * @param goodsId   商品ID
     * @param userId    用户ID
     * @param requestId 请求ID
     * @return true-释放成功
     */
    boolean unlockSeckill(Long goodsId, Long userId, String requestId);
}
