package com.seckill.order.compensation;

import com.seckill.common.entity.CompensationTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 补偿任务服务
 * <p>
 * 使用 Java 8 特性重构：
 * </p>
 * <ul>
 * <li>ConcurrentHashMap 线程安全</li>
 * <li>Optional 处理空值</li>
 * <li>Supplier 延迟初始化</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompensationTaskService {

    private final StringRedisTemplate redisTemplate;

    private static final String TASK_KEY_PREFIX = "seckill:compensation:task:";
    private static final String PENDING_SET_KEY = "seckill:compensation:pending";
    private static final long TASK_EXPIRE_HOURS = 24L;

    /**
     * 内存缓存（用于快速访问）
     */
    private final Map<String, CompensationTask> taskCache = new ConcurrentHashMap<>();

    /**
     * 任务 ID 生成器
     */
    private static final Supplier<String> TASK_ID_GENERATOR = () -> UUID.randomUUID().toString().replace("-", "");

    /**
     * 创建补偿任务
     */
    public String createTask(String taskType, String bizKey, String payload) {
        Objects.requireNonNull(taskType, "taskType cannot be null");
        Objects.requireNonNull(bizKey, "bizKey cannot be null");

        String taskId = TASK_ID_GENERATOR.get();

        CompensationTask task = CompensationTask.builder()
                .taskId(taskId)
                .taskType(taskType)
                .bizKey(bizKey)
                .payload(payload)
                .status(CompensationTask.Status.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .createTime(LocalDateTime.now())
                .nextExecuteTime(LocalDateTime.now())
                .build();

        // 存储到 Redis 和本地缓存
        saveTask(task);

        log.info("创建补偿任务 - taskId: {}, taskType: {}, bizKey: {}", taskId, taskType, bizKey);
        return taskId;
    }

    /**
     * 创建库存回滚任务
     */
    public String createStockRollbackTask(Long goodsId, Integer count) {
        String payload = String.format("{\"goodsId\":%d,\"count\":%d}", goodsId, count);
        return createTask("STOCK_ROLLBACK", "goods:" + goodsId, payload);
    }

    /**
     * 创建清除秒杀标记任务
     */
    public String createKilledMarkRemoveTask(Long userId, Long goodsId) {
        String payload = String.format("{\"userId\":%d,\"goodsId\":%d}", userId, goodsId);
        return createTask("KILLED_MARK_REMOVE", "user:" + userId + ":goods:" + goodsId, payload);
    }

    /**
     * 获取任务
     */
    public Optional<CompensationTask> getTask(String taskId) {
        // 优先从缓存获取
        return Optional.ofNullable(taskCache.get(taskId))
                .or(() -> loadFromRedis(taskId));
    }

    /**
     * 更新任务状态
     */
    public void updateTaskStatus(String taskId, CompensationTask.Status status) {
        getTask(taskId).ifPresent(task -> {
            task.setStatus(status);
            task.setUpdateTime(LocalDateTime.now());

            if (status == CompensationTask.Status.SUCCESS || status == CompensationTask.Status.FAILED) {
                // 从待处理集合移除
                redisTemplate.opsForSet().remove(PENDING_SET_KEY, taskId);
            }

            saveTask(task);
            log.info("更新任务状态 - taskId: {}, status: {}", taskId, status);
        });
    }

    /**
     * 增加重试次数
     */
    public void incrementRetry(String taskId) {
        getTask(taskId).ifPresent(task -> {
            task.setRetryCount(task.getRetryCount() + 1);
            task.setNextExecuteTime(LocalDateTime.now().plusMinutes(
                    (long) Math.pow(2, task.getRetryCount())));
            task.setUpdateTime(LocalDateTime.now());
            saveTask(task);
            log.info("增加任务重试次数 - taskId: {}, retryCount: {}", taskId, task.getRetryCount());
        });
    }

    /**
     * 保存任务到 Redis
     */
    private void saveTask(CompensationTask task) {
        String key = TASK_KEY_PREFIX + task.getTaskId();
        redisTemplate.opsForValue().set(key, task.toJson(), TASK_EXPIRE_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForSet().add(PENDING_SET_KEY, task.getTaskId());
        taskCache.put(task.getTaskId(), task);
    }

    /**
     * 从 Redis 加载任务
     */
    private Optional<CompensationTask> loadFromRedis(String taskId) {
        String key = TASK_KEY_PREFIX + taskId;
        return Optional.ofNullable(redisTemplate.opsForValue().get(key))
                .map(json -> {
                    CompensationTask task = CompensationTask.fromJson(json);
                    taskCache.put(taskId, task);
                    return task;
                });
    }

    /**
     * 获取所有待处理任务 ID
     */
    public java.util.Set<String> getPendingTaskIds() {
        return Optional.ofNullable(redisTemplate.opsForSet().members(PENDING_SET_KEY))
                .orElse(java.util.Collections.emptySet());
    }
}
