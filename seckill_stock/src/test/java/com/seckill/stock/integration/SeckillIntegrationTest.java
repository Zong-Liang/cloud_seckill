package com.seckill.stock.integration;

import com.alibaba.fastjson.JSON;
import com.seckill.common.result.Result;
import com.seckill.stock.dto.SeckillRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 秒杀流程集成测试
 * <p>
 * 测试完整的秒杀流程，包括正常秒杀、并发秒杀、库存不足等场景
 * </p>
 * 
 * 运行前提：
 * 1. 启动 Redis
 * 2. 启动 RocketMQ
 * 3. 启动数据库
 *
 * @author seckill
 * @since 2.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("需要完整基础设施才能运行")
class SeckillIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final Long TEST_GOODS_ID = 1L;
    private static final Long TEST_USER_ID = 1001L;

    @BeforeAll
    static void beforeAll() {
        System.out.println("========== 秒杀集成测试开始 ==========");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("========== 秒杀集成测试结束 ==========");
    }

    @Test
    @Order(1)
    @DisplayName("初始化商品库存")
    void testInitStock() throws Exception {
        mockMvc.perform(post("/stock/init/{goodsId}", TEST_GOODS_ID)
                .param("stockCount", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(2)
    @DisplayName("查询商品库存")
    void testGetStock() throws Exception {
        mockMvc.perform(get("/stock/{goodsId}", TEST_GOODS_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @Order(3)
    @DisplayName("正常秒杀流程")
    void testNormalSeckill() throws Exception {
        SeckillRequest request = new SeckillRequest();
        request.setUserId(TEST_USER_ID);
        request.setGoodsId(TEST_GOODS_ID);
        request.setCount(1);
        request.setChannel("PC");

        MvcResult result = mockMvc.perform(post("/seckill/do")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println("秒杀响应: " + response);
    }

    @Test
    @Order(4)
    @DisplayName("重复秒杀检测")
    void testRepeatSeckill() throws Exception {
        SeckillRequest request = new SeckillRequest();
        request.setUserId(TEST_USER_ID);
        request.setGoodsId(TEST_GOODS_ID);
        request.setCount(1);
        request.setChannel("PC");

        // 重复秒杀应该返回错误
        mockMvc.perform(post("/seckill/do")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(50001)); // REPEAT_ORDER
    }

    @Test
    @Order(5)
    @DisplayName("并发秒杀测试 - 100并发抢购10件商品")
    void testConcurrentSeckill() throws Exception {
        int threadCount = 100;
        int stockCount = 10;
        Long goodsId = 999L; // 使用独立的商品ID

        // 初始化库存
        mockMvc.perform(post("/stock/init/{goodsId}", goodsId)
                .param("stockCount", String.valueOf(stockCount)))
                .andExpect(status().isOk());

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final long userId = 10000L + i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    SeckillRequest request = new SeckillRequest();
                    request.setUserId(userId);
                    request.setGoodsId(goodsId);
                    request.setCount(1);
                    request.setChannel("PC");

                    MvcResult result = mockMvc.perform(post("/seckill/do")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON.toJSONString(request)))
                            .andReturn();

                    String response = result.getResponse().getContentAsString();
                    Result<?> r = JSON.parseObject(response, Result.class);

                    if (r.getCode() == 200) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }

                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 所有线程同时开始
        startLatch.countDown();

        // 等待所有线程完成
        endLatch.await();
        executor.shutdown();

        System.out.println("========== 并发测试结果 ==========");
        System.out.println("总请求数: " + threadCount);
        System.out.println("成功数: " + successCount.get());
        System.out.println("失败数: " + failCount.get());
        System.out.println("库存数: " + stockCount);

        // 断言：成功数不应超过库存数
        Assertions.assertTrue(successCount.get() <= stockCount,
                "超卖检测: 成功数(" + successCount.get() + ") 不应超过库存数(" + stockCount + ")");
    }

    @Test
    @Order(6)
    @DisplayName("库存不足场景")
    void testStockNotEnough() throws Exception {
        Long goodsId = 888L;

        // 初始化只有1个库存
        mockMvc.perform(post("/stock/init/{goodsId}", goodsId)
                .param("stockCount", "1"))
                .andExpect(status().isOk());

        // 第一个用户秒杀成功
        SeckillRequest request1 = new SeckillRequest();
        request1.setUserId(20001L);
        request1.setGoodsId(goodsId);
        request1.setCount(1);

        mockMvc.perform(post("/seckill/do")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(request1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 第二个用户秒杀失败 - 库存不足
        SeckillRequest request2 = new SeckillRequest();
        request2.setUserId(20002L);
        request2.setGoodsId(goodsId);
        request2.setCount(1);

        mockMvc.perform(post("/seckill/do")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001)); // STOCK_NOT_ENOUGH
    }
}
