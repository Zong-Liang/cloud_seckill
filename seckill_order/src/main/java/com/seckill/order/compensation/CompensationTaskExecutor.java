package com.seckill.order.compensation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seckill.common.entity.CompensationTask;
import com.seckill.order.feign.StockFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 补偿任务执行器
 * <p>
 * 使用 Java 8 特性重构：
 * </p>
 * <ul>
 * <li>Stream API 处理集合</li>
 * <li>Optional 链式调用</li>
 * <li>BiConsumer 处理任务执行</li>
 * <li>Map 存储任务处理器</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompensationTaskExecutor {

    private final CompensationTaskService compensationTaskService;
    private final StockFeignClient stockFeignClient;
    private final StringRedisTemplate redisTemplate;

    private static final String KILLED_KEY_PREFIX = "seckill:killed:";

    /**
     * 任务处理器映射（使用 Java 8 Lambda）
     */
    private final Map<String, Consumer<JSONObject>> taskHandlers = Map.of(
            "STOCK_ROLLBACK", this::handleStockRollback,
            "KILLED_MARK_REMOVE", this::handleKilledMarkRemove,
            "STOCK_SYNC", this::handleStockSync);

    /**
     * 定时执行补偿任务（每分钟）
     */
    @Scheduled(fixedDelay = 60000)
    public void executeCompensationTasks() {
        log.debug("开始执行补偿任务扫描");

        Set<String> taskIds = compensationTaskService.getPendingTaskIds();

        // 使用 Stream API 处理任务
        taskIds.stream()
                .map(compensationTaskService::getTask)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(this::shouldExecute)
                .forEach(this::processTask);

        log.debug("补偿任务扫描完成 - 待处理: {}", taskIds.size());
    }

    /**
     * 判断是否应该执行
     */
    private boolean shouldExecute(CompensationTask task) {
        return task.getStatus() == CompensationTask.Status.PENDING
                && task.getNextExecuteTime().isBefore(LocalDateTime.now())
                && task.getRetryCount() < task.getMaxRetries();
    }

    /**
     * 处理单个任务
     */
    private void processTask(CompensationTask task) {
        log.info("执行补偿任务 - taskId: {}, taskType: {}", task.getTaskId(), task.getTaskType());

        try {
            JSONObject payload = JSON.parseObject(task.getPayload());

            // 使用 Optional 查找并执行处理器
            Optional.ofNullable(taskHandlers.get(task.getTaskType()))
                    .ifPresentOrElse(
                            handler -> {
                                handler.accept(payload);
                                compensationTaskService.updateTaskStatus(task.getTaskId(),
                                        CompensationTask.Status.SUCCESS);
                                log.info("补偿任务执行成功 - taskId: {}", task.getTaskId());
                            },
                            () -> log.warn("未知任务类型 - taskType: {}", task.getTaskType()));

        } catch (Exception e) {
            log.error("补偿任务执行失败 - taskId: {}", task.getTaskId(), e);
            compensationTaskService.incrementRetry(task.getTaskId());

            // 达到最大重试次数
            if (task.getRetryCount() >= task.getMaxRetries() - 1) {
                compensationTaskService.updateTaskStatus(task.getTaskId(),
                        CompensationTask.Status.FAILED);
                log.error("补偿任务达到最大重试次数，标记为失败 - taskId: {}", task.getTaskId());
            }
        }
    }

    /**
     * 处理库存回滚
     */
    private void handleStockRollback(JSONObject payload) {
        Long goodsId = payload.getLong("goodsId");
        Integer count = payload.getInteger("count");

        stockFeignClient.rollbackStock(goodsId, count);
        log.info("库存回滚成功 - goodsId: {}, count: {}", goodsId, count);
    }

    /**
     * 处理清除秒杀标记
     */
    private void handleKilledMarkRemove(JSONObject payload) {
        Long userId = payload.getLong("userId");
        Long goodsId = payload.getLong("goodsId");

        String key = KILLED_KEY_PREFIX + goodsId + ":" + userId;
        redisTemplate.delete(key);
        log.info("清除秒杀标记成功 - userId: {}, goodsId: {}", userId, goodsId);
    }

    /**
     * 处理库存同步
     */
    private void handleStockSync(JSONObject payload) {
        Long goodsId = payload.getLong("goodsId");
        Integer count = payload.getInteger("count");

        stockFeignClient.syncDeductStock(goodsId, count);
        log.info("库存同步成功 - goodsId: {}, count: {}", goodsId, count);
    }
}
