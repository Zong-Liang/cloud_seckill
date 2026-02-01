package com.seckill.common.id;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * 分布式 ID 生成器
 * <p>
 * 基于雪花算法实现，使用 Java 8 特性：
 * </p>
 * <ul>
 * <li>LongSupplier 函数式接口</li>
 * <li>AtomicLong 原子操作</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
public class DistributedIdGenerator implements LongSupplier {

    /**
     * 开始时间戳 (2024-01-01 00:00:00)
     */
    private static final long EPOCH = 1704067200000L;

    /**
     * 机器ID所占位数
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 数据中心ID所占位数
     */
    private static final long DATACENTER_ID_BITS = 5L;

    /**
     * 序列所占位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 机器ID最大值
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 数据中心ID最大值
     */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /**
     * 序列最大值
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * 机器ID左移位数
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据中心ID左移位数
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间戳左移位数
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private final long workerId;
    private final long datacenterId;

    private final AtomicLong sequence = new AtomicLong(0);
    private volatile long lastTimestamp = -1L;

    /**
     * 构造函数
     *
     * @param workerId     机器ID
     * @param datacenterId 数据中心ID
     */
    public DistributedIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("Worker ID 必须在 0 到 %d 之间", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("Datacenter ID 必须在 0 到 %d 之间", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        log.info("初始化分布式ID生成器 - workerId: {}, datacenterId: {}", workerId, datacenterId);
    }

    /**
     * 生成下一个ID
     *
     * @return 唯一ID
     */
    public synchronized long nextId() {
        long timestamp = currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("时钟回拨，拒绝生成ID，回拨时间: %d ms", lastTimestamp - timestamp));
        }

        if (timestamp == lastTimestamp) {
            long seq = sequence.incrementAndGet() & MAX_SEQUENCE;
            if (seq == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence.set(0);
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence.get();
    }

    /**
     * 实现 LongSupplier 接口
     */
    @Override
    public long getAsLong() {
        return nextId();
    }

    /**
     * 等待下一毫秒
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     */
    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取 Worker ID
     */
    public long getWorkerId() {
        return workerId;
    }

    /**
     * 获取 Datacenter ID
     */
    public long getDatacenterId() {
        return datacenterId;
    }
}
