package com.seckill.stock.service;

import com.seckill.common.constant.RedisKeyConstants;
import com.seckill.stock.service.impl.DistributedLockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 分布式锁服务单元测试
 *
 * @author seckill
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("分布式锁服务单元测试")
class DistributedLockServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private DistributedLockServiceImpl lockService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("获取锁成功")
    void testTryLock_Success() {
        String lockKey = "test-lock";
        String requestId = "request-123";
        long expireTime = 10L;

        when(valueOperations.setIfAbsent(
                eq(RedisKeyConstants.lockKey(lockKey)),
                eq(requestId),
                eq(expireTime),
                eq(TimeUnit.SECONDS))).thenReturn(true);

        boolean result = lockService.tryLock(lockKey, requestId, expireTime);

        assertTrue(result);
    }

    @Test
    @DisplayName("获取锁失败 - 锁已被占用")
    void testTryLock_Failed() {
        String lockKey = "test-lock";
        String requestId = "request-123";
        long expireTime = 10L;

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                .thenReturn(false);

        boolean result = lockService.tryLock(lockKey, requestId, expireTime);

        assertFalse(result);
    }

    @Test
    @DisplayName("释放锁成功")
    @SuppressWarnings("unchecked")
    void testUnlock_Success() {
        String lockKey = "test-lock";
        String requestId = "request-123";

        when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString()))
                .thenReturn(1L);

        boolean result = lockService.unlock(lockKey, requestId);

        assertTrue(result);
    }

    @Test
    @DisplayName("释放锁失败 - 非持有者")
    @SuppressWarnings("unchecked")
    void testUnlock_Failed_NotOwner() {
        String lockKey = "test-lock";
        String requestId = "wrong-request-id";

        when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString()))
                .thenReturn(0L);

        boolean result = lockService.unlock(lockKey, requestId);

        assertFalse(result);
    }

    @Test
    @DisplayName("获取秒杀锁成功")
    void testLockSeckill_Success() {
        Long goodsId = 1L;
        Long userId = 1001L;
        long expireTime = 10L;

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                .thenReturn(true);

        String requestId = lockService.lockSeckill(goodsId, userId, expireTime);

        assertNotNull(requestId);
    }

    @Test
    @DisplayName("获取秒杀锁失败")
    void testLockSeckill_Failed() {
        Long goodsId = 1L;
        Long userId = 1001L;
        long expireTime = 10L;

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                .thenReturn(false);

        String requestId = lockService.lockSeckill(goodsId, userId, expireTime);

        assertNull(requestId);
    }

    @Test
    @DisplayName("释放秒杀锁成功")
    @SuppressWarnings("unchecked")
    void testUnlockSeckill_Success() {
        Long goodsId = 1L;
        Long userId = 1001L;
        String requestId = "request-123";

        when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString()))
                .thenReturn(1L);

        boolean result = lockService.unlockSeckill(goodsId, userId, requestId);

        assertTrue(result);
    }
}
