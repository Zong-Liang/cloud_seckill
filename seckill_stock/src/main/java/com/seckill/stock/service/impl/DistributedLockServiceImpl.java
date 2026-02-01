package com.seckill.stock.service.impl;

import com.seckill.common.constant.RedisKeyConstants;
import com.seckill.stock.service.DistributedLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务实现
 * <p>
 * 使用 Java 8 特性重构：
 * </p>
 * <ul>
 * <li>Supplier 封装锁生成逻辑</li>
 * <li>Optional 处理返回值</li>
 * <li>Lambda 简化代码</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockServiceImpl implements DistributedLockService {

    private final StringRedisTemplate redisTemplate;

    /**
     * Lua 脚本：释放锁（只有持有者才能释放）
     */
    private static final String UNLOCK_LUA = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    /**
     * RequestId 生成器
     */
    private static final Supplier<String> REQUEST_ID_GENERATOR = () -> UUID.randomUUID().toString();

    @Override
    public boolean tryLock(String lockKey, String requestId, long expireTime) {
        String key = RedisKeyConstants.lockKey(lockKey);

        boolean success = Optional.ofNullable(
                redisTemplate.opsForValue().setIfAbsent(key, requestId, expireTime, TimeUnit.SECONDS)).orElse(false);

        log.debug("获取分布式锁{} - key: {}, requestId: {}",
                success ? "成功" : "失败", lockKey, requestId);

        return success;
    }

    @Override
    public boolean unlock(String lockKey, String requestId) {
        String key = RedisKeyConstants.lockKey(lockKey);
        Long result = executeUnlockScript(key, requestId);

        boolean success = Optional.ofNullable(result)
                .map(r -> r == 1)
                .orElse(false);

        log.debug("释放分布式锁{} - key: {}, requestId: {}",
                success ? "成功" : "失败", lockKey, requestId);

        return success;
    }

    @Override
    public String lockSeckill(Long goodsId, Long userId, long expireTime) {
        String lockKey = RedisKeyConstants.seckillLockKey(goodsId, userId);
        String requestId = REQUEST_ID_GENERATOR.get();

        boolean success = Optional.ofNullable(
                redisTemplate.opsForValue().setIfAbsent(lockKey, requestId, expireTime, TimeUnit.SECONDS))
                .orElse(false);

        log.debug("获取秒杀锁{} - goodsId: {}, userId: {}",
                success ? "成功" : "失败", goodsId, userId);

        return success ? requestId : null;
    }

    @Override
    public boolean unlockSeckill(Long goodsId, Long userId, String requestId) {
        String lockKey = RedisKeyConstants.seckillLockKey(goodsId, userId);
        Long result = executeUnlockScript(lockKey, requestId);

        boolean success = Optional.ofNullable(result)
                .map(r -> r == 1)
                .orElse(false);

        if (success) {
            log.debug("释放秒杀锁成功 - goodsId: {}, userId: {}", goodsId, userId);
        }

        return success;
    }

    /**
     * 执行解锁脚本
     */
    private Long executeUnlockScript(String key, String requestId) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_LUA, Long.class);
        return redisTemplate.execute(script, Collections.singletonList(key), requestId);
    }
}
