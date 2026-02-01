package com.seckill.stock.benchmark;

import com.alibaba.fastjson.JSON;
import com.seckill.stock.dto.SeckillRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 秒杀压力测试工具
 * <p>
 * 使用 Java 11 HttpClient 进行压力测试
 * </p>
 *
 * @author seckill
 * @since 2.0.0
 */
public class SeckillBenchmark {

    private static final String BASE_URL = "http://localhost:9002";
    private static final String SECKILL_URL = BASE_URL + "/seckill/do";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .executor(Executors.newFixedThreadPool(200))
            .build();

    public static void main(String[] args) throws Exception {
        // 测试参数
        int totalRequests = 1000; // 总请求数
        int concurrency = 100; // 并发数
        Long goodsId = 1L; // 商品ID
        int stockCount = 100; // 库存数

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║              秒杀系统压力测试工具 v2.0                   ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.printf("║ 目标地址: %-45s ║%n", BASE_URL);
        System.out.printf("║ 总请求数: %-45d ║%n", totalRequests);
        System.out.printf("║ 并发数: %-47d ║%n", concurrency);
        System.out.printf("║ 商品ID: %-47d ║%n", goodsId);
        System.out.printf("║ 库存数: %-47d ║%n", stockCount);
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();

        // 1. 初始化库存
        System.out.println(">>> 步骤1: 初始化库存...");
        initStock(goodsId, stockCount);
        System.out.println("    库存初始化完成");
        System.out.println();

        // 2. 执行压力测试
        System.out.println(">>> 步骤2: 开始压力测试...");
        BenchmarkResult result = runBenchmark(totalRequests, concurrency, goodsId);

        // 3. 输出结果
        printResult(result, totalRequests, concurrency, stockCount);
    }

    private static void initStock(Long goodsId, int stockCount) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/seckill/init/" + goodsId + "?stockCount=" + stockCount))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static BenchmarkResult runBenchmark(int totalRequests, int concurrency, Long goodsId) throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(totalRequests);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Long> responseTimes = new CopyOnWriteArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(concurrency);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalRequests; i++) {
            final long userId = 100000L + i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    SeckillRequest req = new SeckillRequest();
                    req.setUserId(userId);
                    req.setGoodsId(goodsId);
                    req.setCount(1);
                    req.setChannel("BENCHMARK");

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(SECKILL_URL))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(req)))
                            .build();

                    long reqStart = System.currentTimeMillis();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    long reqEnd = System.currentTimeMillis();

                    responseTimes.add(reqEnd - reqStart);

                    if (response.statusCode() == 200) {
                        String body = response.body();
                        if (body.contains("\"code\":200")) {
                            successCount.incrementAndGet();
                        } else {
                            failCount.incrementAndGet();
                        }
                    } else {
                        errorCount.incrementAndGet();
                    }

                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 所有线程同时开始
        startLatch.countDown();

        // 等待所有请求完成
        endLatch.await();

        long endTime = System.currentTimeMillis();
        executor.shutdown();

        return new BenchmarkResult(
                successCount.get(),
                failCount.get(),
                errorCount.get(),
                endTime - startTime,
                responseTimes);
    }

    private static void printResult(BenchmarkResult result, int totalRequests, int concurrency, int stockCount) {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║                    测试结果报告                          ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.printf("║ 总请求数: %-45d ║%n", totalRequests);
        System.out.printf("║ 成功数: %-47d ║%n", result.successCount);
        System.out.printf("║ 业务失败数: %-43d ║%n", result.failCount);
        System.out.printf("║ 系统错误数: %-43d ║%n", result.errorCount);
        System.out.printf("║ 总耗时: %-42.2f s ║%n", result.totalTime / 1000.0);
        System.out.printf("║ QPS: %-49.2f ║%n", totalRequests * 1000.0 / result.totalTime);
        System.out.println("╠════════════════════════════════════════════════════════╣");

        if (!result.responseTimes.isEmpty()) {
            LongSummaryStatistics stats = result.responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .summaryStatistics();

            List<Long> sorted = new ArrayList<>(result.responseTimes);
            sorted.sort(Long::compare);
            long p50 = sorted.get((int) (sorted.size() * 0.50));
            long p90 = sorted.get((int) (sorted.size() * 0.90));
            long p99 = sorted.get((int) (sorted.size() * 0.99));

            System.out.printf("║ 平均响应时间: %-38.2f ms ║%n", stats.getAverage());
            System.out.printf("║ 最小响应时间: %-42d ms ║%n", stats.getMin());
            System.out.printf("║ 最大响应时间: %-42d ms ║%n", stats.getMax());
            System.out.printf("║ P50 响应时间: %-42d ms ║%n", p50);
            System.out.printf("║ P90 响应时间: %-42d ms ║%n", p90);
            System.out.printf("║ P99 响应时间: %-42d ms ║%n", p99);
        }

        System.out.println("╠════════════════════════════════════════════════════════╣");

        // 超卖检测
        if (result.successCount > stockCount) {
            System.out.printf("║ ⚠️  超卖告警: 成功数(%d) > 库存数(%d)                      ║%n",
                    result.successCount, stockCount);
        } else {
            System.out.printf("║ ✅ 超卖检测: 通过 (成功数 %d <= 库存数 %d)                ║%n",
                    result.successCount, stockCount);
        }

        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    record BenchmarkResult(
            int successCount,
            int failCount,
            int errorCount,
            long totalTime,
            List<Long> responseTimes) {
    }
}
