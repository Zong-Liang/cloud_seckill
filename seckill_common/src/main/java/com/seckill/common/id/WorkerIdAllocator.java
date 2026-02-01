package com.seckill.common.id;

import com.seckill.common.constant.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Worker ID 分配器
 * <p>
 * 使用 Redis 实现分布式 Worker ID 分配，使用 Java 8 特性：
 * </p>
 * <ul>
 * <li>Optional 处理空值</li>
 * <li>Supplier 延迟计算</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class WorkerIdAllocator {

    private final StringRedisTemplate redisTemplate;
    private final String instanceId;

    private static final long MAX_WORKER_ID = 31L;

    /**
     * 分配 Worker ID
     *
     * @return Worker ID
     */
    public long allocate() {
        String key = RedisKeyConstants.workerIdKey(instanceId);

        // 检查是否已分配
        Supplier<Long> existingId = () -> Optional.ofNullable(redisTemplate.opsForValue().get(key))
                .map(Long::parseLong)
                .orElse(null);

        Long existing = existingId.get();
        if (existing != null) {
            log.info("使用已分配的 Worker ID - instanceId: {}, workerId: {}", instanceId, existing);
            renewLease(key);
            return existing;
        }

        // 分配新的 Worker ID
        Long workerId = redisTemplate.opsForValue()
                .increment(RedisKeyConstants.WORKER_ID_COUNTER);

        // 使用模运算确保在范围内
        long finalWorkerId = Optional.ofNullable(workerId)
                .map(id -> id % (MAX_WORKER_ID + 1))
                .orElse(0L);

        // 保存分配结果
        redisTemplate.opsForValue().set(
                key,
                String.valueOf(finalWorkerId),
                RedisKeyConstants.WORKER_ID_EXPIRE_SECONDS,
                TimeUnit.SECONDS);

        log.info("分配新的 Worker ID - instanceId: {}, workerId: {}", instanceId, finalWorkerId);
        return finalWorkerId;
    }

    /**
     * 续约 Worker ID
     */
    private void renewLease(String key) {
        redisTemplate.expire(key, RedisKeyConstants.WORKER_ID_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 释放 Worker ID
     */
    public void release() {
        String key = RedisKeyConstants.workerIdKey(instanceId);
        redisTemplate.delete(key);
        log.info("释放 Worker ID - instanceId: {}", instanceId);
    }
}
