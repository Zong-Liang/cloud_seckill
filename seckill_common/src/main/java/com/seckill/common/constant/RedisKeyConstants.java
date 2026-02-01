package com.seckill.common.constant;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Redis Key 常量管理
 * <p>
 * 统一管理所有 Redis Key，避免分散在各个服务中导致维护困难
 * </p>
 * <p>
 * 使用 Java 8 函数式接口优化 Key 生成
 * </p>
 *
 * @author seckill
 * @since 2.0.0
 */
public final class RedisKeyConstants {

    private RedisKeyConstants() {
        throw new IllegalStateException("Constants class");
    }

    // ==================== 前缀 ====================

    /**
     * 全局前缀
     */
    private static final String PREFIX = "seckill:";

    // ==================== 库存相关 ====================

    /**
     * 库存缓存 Key 前缀
     */
    public static final String STOCK_PREFIX = PREFIX + "stock:";

    /**
     * 已秒杀标记 Key 前缀
     */
    public static final String KILLED_PREFIX = PREFIX + "killed:";

    /**
     * 分布式锁 Key 前缀
     */
    public static final String LOCK_PREFIX = PREFIX + "lock:";

    /**
     * 秒杀锁 Key 前缀
     */
    public static final String SECKILL_LOCK_PREFIX = PREFIX + "lock:seckill:";

    // ==================== 用户相关 ====================

    /**
     * 用户 Token Key 前缀
     */
    public static final String USER_TOKEN_PREFIX = PREFIX + "user:token:";

    // ==================== 补偿任务相关 ====================

    /**
     * 补偿任务 Key 前缀
     */
    public static final String COMPENSATION_PREFIX = PREFIX + "compensation:";

    // ==================== 分布式 ID 相关 ====================

    /**
     * Worker ID 分配 Key
     */
    public static final String WORKER_ID_KEY = PREFIX + "snowflake:worker-id";

    /**
     * Worker ID 计数器
     */
    public static final String WORKER_ID_COUNTER = PREFIX + "snowflake:counter";

    // ==================== 过期时间（秒） ====================

    /**
     * 库存缓存过期时间：7天
     */
    public static final long STOCK_EXPIRE_SECONDS = 7 * 24 * 60 * 60L;

    /**
     * 已秒杀标记过期时间：7天
     */
    public static final long KILLED_EXPIRE_SECONDS = 7 * 24 * 60 * 60L;

    /**
     * 分布式锁默认过期时间：10秒
     */
    public static final long LOCK_DEFAULT_EXPIRE_SECONDS = 10L;

    /**
     * Worker ID 过期时间：1天（服务会定期续约）
     */
    public static final long WORKER_ID_EXPIRE_SECONDS = 24 * 60 * 60L;

    // ==================== Key 生成器（函数式风格） ====================

    /**
     * 库存 Key 生成器
     */
    public static final Function<Long, String> STOCK_KEY = goodsId -> STOCK_PREFIX + goodsId;

    /**
     * 锁 Key 生成器
     */
    public static final Function<String, String> LOCK_KEY = lockName -> LOCK_PREFIX + lockName;

    /**
     * 用户 Token Key 生成器
     */
    public static final Function<Long, String> USER_TOKEN_KEY = userId -> USER_TOKEN_PREFIX + userId;

    /**
     * Worker ID Key 生成器
     */
    public static final Function<String, String> WORKER_ID_KEY_GEN = instanceId -> WORKER_ID_KEY + ":" + instanceId;

    // ==================== Key 生成方法 ====================

    /**
     * 生成库存缓存 Key
     *
     * @param goodsId 商品ID
     * @return Redis Key
     */
    public static String stockKey(Long goodsId) {
        return STOCK_KEY.apply(Objects.requireNonNull(goodsId, "goodsId cannot be null"));
    }

    /**
     * 生成已秒杀标记 Key
     *
     * @param goodsId 商品ID
     * @param userId  用户ID
     * @return Redis Key
     */
    public static String killedKey(Long goodsId, Long userId) {
        Objects.requireNonNull(goodsId, "goodsId cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");
        return String.join(":", KILLED_PREFIX + goodsId, String.valueOf(userId));
    }

    /**
     * 生成通用分布式锁 Key
     *
     * @param lockName 锁名称
     * @return Redis Key
     */
    public static String lockKey(String lockName) {
        return LOCK_KEY.apply(Objects.requireNonNull(lockName, "lockName cannot be null"));
    }

    /**
     * 生成秒杀分布式锁 Key
     *
     * @param goodsId 商品ID
     * @param userId  用户ID
     * @return Redis Key
     */
    public static String seckillLockKey(Long goodsId, Long userId) {
        Objects.requireNonNull(goodsId, "goodsId cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");
        return String.join(":", SECKILL_LOCK_PREFIX + goodsId, String.valueOf(userId));
    }

    /**
     * 生成用户 Token Key
     *
     * @param userId 用户ID
     * @return Redis Key
     */
    public static String userTokenKey(Long userId) {
        return USER_TOKEN_KEY.apply(Objects.requireNonNull(userId, "userId cannot be null"));
    }

    /**
     * 生成 Worker ID Key（基于服务实例标识）
     *
     * @param instanceId 实例标识
     * @return Redis Key
     */
    public static String workerIdKey(String instanceId) {
        return WORKER_ID_KEY_GEN.apply(Objects.requireNonNull(instanceId, "instanceId cannot be null"));
    }

    /**
     * 构建 Key（通用方法，使用 Supplier）
     *
     * @param keySupplier Key 生成器
     * @return Redis Key
     */
    public static String buildKey(Supplier<String> keySupplier) {
        return Objects.requireNonNull(keySupplier, "keySupplier cannot be null").get();
    }
}
