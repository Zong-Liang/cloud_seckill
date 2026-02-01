package com.seckill.common.entity;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 补偿任务实体
 * <p>
 * 使用 Java 8 特性：
 * </p>
 * <ul>
 * <li>Builder 模式构建对象</li>
 * <li>Optional 处理空值</li>
 * <li>静态工厂方法</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompensationTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务类型（STOCK_ROLLBACK, KILLED_MARK_REMOVE, STOCK_SYNC）
     */
    private String taskType;

    /**
     * 业务键
     */
    private String bizKey;

    /**
     * 任务负载（JSON格式）
     */
    private String payload;

    /**
     * 任务状态
     */
    private Status status;

    /**
     * 已重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecuteTime;

    /**
     * 任务状态枚举
     */
    public enum Status {
        /**
         * 待处理
         */
        PENDING,
        /**
         * 处理中
         */
        PROCESSING,
        /**
         * 成功
         */
        SUCCESS,
        /**
         * 失败
         */
        FAILED
    }

    /**
     * 转换为 JSON
     */
    public String toJson() {
        return JSON.toJSONString(this);
    }

    /**
     * 从 JSON 解析
     */
    public static CompensationTask fromJson(String json) {
        return Optional.ofNullable(json)
                .map(j -> JSON.parseObject(j, CompensationTask.class))
                .orElse(null);
    }

    /**
     * 创建库存回滚任务
     */
    public static CompensationTask stockRollback(Long goodsId, Integer count) {
        String payload = String.format("{\"goodsId\":%d,\"count\":%d}", goodsId, count);
        return CompensationTask.builder()
                .taskType("STOCK_ROLLBACK")
                .bizKey("goods:" + goodsId)
                .payload(payload)
                .status(Status.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .createTime(LocalDateTime.now())
                .nextExecuteTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建清除秒杀标记任务
     */
    public static CompensationTask killedMarkRemove(Long userId, Long goodsId) {
        String payload = String.format("{\"userId\":%d,\"goodsId\":%d}", userId, goodsId);
        return CompensationTask.builder()
                .taskType("KILLED_MARK_REMOVE")
                .bizKey("user:" + userId + ":goods:" + goodsId)
                .payload(payload)
                .status(Status.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .createTime(LocalDateTime.now())
                .nextExecuteTime(LocalDateTime.now())
                .build();
    }

    /**
     * 是否可重试
     */
    public boolean canRetry() {
        return Optional.ofNullable(retryCount)
                .flatMap(rc -> Optional.ofNullable(maxRetries).map(mr -> rc < mr))
                .orElse(true);
    }

    /**
     * 是否待处理
     */
    public boolean isPending() {
        return status == Status.PENDING;
    }

    /**
     * 是否可执行
     */
    public boolean isExecutable() {
        return isPending()
                && canRetry()
                && Optional.ofNullable(nextExecuteTime)
                        .map(t -> t.isBefore(LocalDateTime.now()) || t.isEqual(LocalDateTime.now()))
                        .orElse(true);
    }
}
