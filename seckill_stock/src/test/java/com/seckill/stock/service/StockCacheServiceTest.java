package com.seckill.stock.service;

import com.seckill.common.constant.RedisKeyConstants;
import com.seckill.stock.service.impl.StockCacheServiceImpl;
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
 * 库存缓存服务单元测试
 *
 * @author seckill
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("库存缓存服务单元测试")
class StockCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private StockCacheServiceImpl stockCacheService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("初始化库存")
    void testInitStock() {
        Long goodsId = 1L;
        Integer stockCount = 100;

        stockCacheService.initStock(goodsId, stockCount);

        verify(valueOperations).set(
                eq(RedisKeyConstants.stockKey(goodsId)),
                eq(String.valueOf(stockCount)),
                eq(RedisKeyConstants.STOCK_EXPIRE_SECONDS),
                eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("扣减库存成功")
    @SuppressWarnings("unchecked")
    void testDeductStock_Success() {
        Long goodsId = 1L;
        Integer count = 1;

        when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString()))
                .thenReturn(99L);

        Long result = stockCacheService.deductStock(goodsId, count);

        assertEquals(99L, result);
    }

    @Test
    @DisplayName("扣减库存 - 库存不足")
    @SuppressWarnings("unchecked")
    void testDeductStock_NotEnough() {
        Long goodsId = 1L;
        Integer count = 1;

        when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString()))
                .thenReturn(-1L);

        Long result = stockCacheService.deductStock(goodsId, count);

        assertEquals(-1L, result);
    }

    @Test
    @DisplayName("扣减库存 - 未初始化")
    @SuppressWarnings("unchecked")
    void testDeductStock_NotInitialized() {
        Long goodsId = 1L;
        Integer count = 1;

        when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString()))
                .thenReturn(-2L);

        Long result = stockCacheService.deductStock(goodsId, count);

        assertEquals(-2L, result);
    }

    @Test
    @DisplayName("回滚库存")
    @SuppressWarnings("unchecked")
    void testRollbackStock() {
        Long goodsId = 1L;
        Integer count = 1;

        when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString()))
                .thenReturn(100L);

        Long result = stockCacheService.rollbackStock(goodsId, count);

        assertEquals(100L, result);
    }

    @Test
    @DisplayName("获取库存")
    void testGetStock() {
        Long goodsId = 1L;
        String key = RedisKeyConstants.stockKey(goodsId);

        when(valueOperations.get(key)).thenReturn("50");

        Long stock = stockCacheService.getStock(goodsId);

        assertEquals(50L, stock);
    }

    @Test
    @DisplayName("检查库存 - 充足")
    void testHasStock_Enough() {
        Long goodsId = 1L;
        String key = RedisKeyConstants.stockKey(goodsId);

        when(valueOperations.get(key)).thenReturn("50");

        boolean hasStock = stockCacheService.hasStock(goodsId, 10);

        assertTrue(hasStock);
    }

    @Test
    @DisplayName("检查库存 - 不足")
    void testHasStock_NotEnough() {
        Long goodsId = 1L;
        String key = RedisKeyConstants.stockKey(goodsId);

        when(valueOperations.get(key)).thenReturn("5");

        boolean hasStock = stockCacheService.hasStock(goodsId, 10);

        assertFalse(hasStock);
    }

    @Test
    @DisplayName("删除库存缓存")
    void testDeleteStock() {
        Long goodsId = 1L;
        String key = RedisKeyConstants.stockKey(goodsId);

        stockCacheService.deleteStock(goodsId);

        verify(redisTemplate).delete(key);
    }
}
