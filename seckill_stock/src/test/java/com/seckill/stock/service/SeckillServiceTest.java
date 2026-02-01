package com.seckill.stock.service;

import com.seckill.common.exception.BusinessException;
import com.seckill.common.id.DistributedIdGenerator;
import com.seckill.stock.dto.SeckillRequest;
import com.seckill.stock.entity.SeckillGoods;
import com.seckill.stock.mq.SeckillMessageProducer;
import com.seckill.stock.service.impl.SeckillServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 秒杀服务单元测试
 * <p>
 * 测试重点：
 * </p>
 * <ul>
 * <li>并发场景下库存不超卖</li>
 * <li>一人一单限制</li>
 * <li>分布式锁正确性</li>
 * <li>失败回滚机制</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("秒杀服务单元测试")
public class SeckillServiceTest {

    @Mock
    private GoodsService goodsService;

    @Mock
    private StockCacheService stockCacheService;

    @Mock
    private DistributedLockService lockService;

    @Mock
    private SeckillMessageProducer messageProducer;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private DistributedIdGenerator idGenerator;

    @InjectMocks
    private SeckillServiceImpl seckillService;

    private SeckillGoods testGoods;

    @BeforeEach
    void setUp() {
        testGoods = new SeckillGoods();
        testGoods.setId(1L);
        testGoods.setGoodsName("测试商品");
        testGoods.setGoodsImg("http://example.com/img.jpg");
        testGoods.setSeckillPrice(new BigDecimal("99.99"));
        testGoods.setStockCount(100);
        testGoods.setStartTime(LocalDateTime.now().minusHours(1));
        testGoods.setEndTime(LocalDateTime.now().plusHours(1));

        // 默认 Redis 模拟行为
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("正常秒杀成功")
    void testDoSeckill_Success() {
        // Given
        Long userId = 1001L;
        Long goodsId = 1L;
        SeckillRequest request = new SeckillRequest();
        request.setUserId(userId);
        request.setGoodsId(goodsId);
        request.setCount(1);
        request.setChannel("PC");

        // Mock dependencies
        when(lockService.lockSeckill(eq(goodsId), eq(userId), anyLong())).thenReturn("lock-request-id");
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(goodsService.checkSeckillable(goodsId)).thenReturn(testGoods);
        when(stockCacheService.deductStock(goodsId, 1)).thenReturn(99L);
        when(idGenerator.nextId()).thenReturn(123456789L);
        doNothing().when(messageProducer).sendSeckillMessage(any(), anyLong());
        when(lockService.unlockSeckill(eq(goodsId), eq(userId), anyString())).thenReturn(true);

        // When
        Long orderNo = seckillService.doSeckill(request);

        // Then
        assertNotNull(orderNo);
        assertEquals(123456789L, orderNo);
        verify(stockCacheService).deductStock(goodsId, 1);
        verify(messageProducer).sendSeckillMessage(any(), eq(123456789L));
    }

    @Test
    @DisplayName("获取锁失败 - 重复提交")
    void testDoSeckill_LockFailed() {
        // Given
        Long userId = 1001L;
        Long goodsId = 1L;
        SeckillRequest request = new SeckillRequest();
        request.setUserId(userId);
        request.setGoodsId(goodsId);
        request.setCount(1);

        // Mock - 获取锁失败
        when(lockService.lockSeckill(eq(goodsId), eq(userId), anyLong())).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> seckillService.doSeckill(request));

        assertTrue(exception.getMessage().contains("重复点击"));
        verify(stockCacheService, never()).deductStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("已秒杀过 - 一人一单限制")
    void testDoSeckill_AlreadyKilled() {
        // Given
        Long userId = 1001L;
        Long goodsId = 1L;
        SeckillRequest request = new SeckillRequest();
        request.setUserId(userId);
        request.setGoodsId(goodsId);
        request.setCount(1);

        when(lockService.lockSeckill(eq(goodsId), eq(userId), anyLong())).thenReturn("lock-id");
        when(redisTemplate.hasKey(anyString())).thenReturn(true); // 已秒杀
        when(lockService.unlockSeckill(eq(goodsId), eq(userId), anyString())).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> seckillService.doSeckill(request));
        verify(stockCacheService, never()).deductStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("库存不足")
    void testDoSeckill_StockNotEnough() {
        // Given
        Long userId = 1001L;
        Long goodsId = 1L;
        SeckillRequest request = new SeckillRequest();
        request.setUserId(userId);
        request.setGoodsId(goodsId);
        request.setCount(1);

        when(lockService.lockSeckill(eq(goodsId), eq(userId), anyLong())).thenReturn("lock-id");
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(goodsService.checkSeckillable(goodsId)).thenReturn(testGoods);
        when(stockCacheService.deductStock(goodsId, 1)).thenReturn(-1L); // 库存不足
        when(lockService.unlockSeckill(eq(goodsId), eq(userId), anyString())).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> seckillService.doSeckill(request));
        verify(messageProducer, never()).sendSeckillMessage(any(), anyLong());
    }

    @Test
    @DisplayName("MQ发送失败 - 触发回滚")
    void testDoSeckill_MQSendFailed_Rollback() {
        // Given
        Long userId = 1001L;
        Long goodsId = 1L;
        SeckillRequest request = new SeckillRequest();
        request.setUserId(userId);
        request.setGoodsId(goodsId);
        request.setCount(1);
        request.setChannel("PC");

        when(lockService.lockSeckill(eq(goodsId), eq(userId), anyLong())).thenReturn("lock-id");
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(goodsService.checkSeckillable(goodsId)).thenReturn(testGoods);
        when(stockCacheService.deductStock(goodsId, 1)).thenReturn(99L);
        when(idGenerator.nextId()).thenReturn(123456789L);
        doThrow(new RuntimeException("MQ失败")).when(messageProducer).sendSeckillMessage(any(), anyLong());
        when(stockCacheService.rollbackStock(goodsId, 1)).thenReturn(100L); // 回滚
        when(lockService.unlockSeckill(eq(goodsId), eq(userId), anyString())).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> seckillService.doSeckill(request));

        // 验证回滚
        verify(stockCacheService).rollbackStock(goodsId, 1);
    }

    @Test
    @DisplayName("并发秒杀 - 库存不超卖")
    void testDoSeckill_Concurrent_NoOversell() throws Exception {
        // Given
        int totalStock = 10;
        int concurrentUsers = 100;
        AtomicInteger remainingStock = new AtomicInteger(totalStock);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(concurrentUsers);

        // Mock - 模拟并发库存扣减
        when(lockService.lockSeckill(anyLong(), anyLong(), anyLong())).thenAnswer(inv -> "lock-" + inv.getArgument(1));
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(goodsService.checkSeckillable(anyLong())).thenReturn(testGoods);
        when(idGenerator.nextId()).thenAnswer(inv -> System.nanoTime());
        when(lockService.unlockSeckill(anyLong(), anyLong(), anyString())).thenReturn(true);

        // 模拟原子库存扣减
        when(stockCacheService.deductStock(anyLong(), anyInt())).thenAnswer(inv -> {
            int current = remainingStock.getAndDecrement();
            return (long) (current - 1);
        });

        // When
        for (int i = 0; i < concurrentUsers; i++) {
            final long userId = 1000L + i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    SeckillRequest request = new SeckillRequest();
                    request.setUserId(userId);
                    request.setGoodsId(1L);
                    request.setCount(1);
                    request.setChannel("PC");

                    try {
                        seckillService.doSeckill(request);
                        successCount.incrementAndGet();
                    } catch (BusinessException e) {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 同时开始
        endLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        System.out.println("成功秒杀: " + successCount.get() + ", 失败: " + failCount.get());
        assertTrue(successCount.get() <= totalStock, "成功数不应超过库存");
    }
}
