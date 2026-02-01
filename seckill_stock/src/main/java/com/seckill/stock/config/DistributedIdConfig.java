package com.seckill.stock.config;

import com.seckill.common.id.DistributedIdGenerator;
import com.seckill.common.id.WorkerIdAllocator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 分布式 ID 配置
 * <p>
 * 使用 Java 8 特性：
 * </p>
 * <ul>
 * <li>Optional 处理默认值</li>
 * <li>Supplier 延迟计算</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Configuration
public class DistributedIdConfig {

    @Value("${spring.application.name:seckill-stock}")
    private String applicationName;

    @Value("${snowflake.datacenter-id:1}")
    private long datacenterId;

    /**
     * 实例 ID 生成器
     */
    private static final Supplier<String> INSTANCE_ID_SUPPLIER = () -> {
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            return hostAddress + ":" + UUID.randomUUID().toString().substring(0, 8);
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    };

    @Bean
    public WorkerIdAllocator workerIdAllocator(StringRedisTemplate redisTemplate) {
        String instanceId = Optional.of(applicationName)
                .map(name -> name + ":" + INSTANCE_ID_SUPPLIER.get())
                .orElseGet(INSTANCE_ID_SUPPLIER);

        return new WorkerIdAllocator(redisTemplate, instanceId);
    }

    @Bean
    public DistributedIdGenerator distributedIdGenerator(WorkerIdAllocator workerIdAllocator) {
        long workerId = workerIdAllocator.allocate();
        log.info("创建分布式 ID 生成器 - workerId: {}, datacenterId: {}", workerId, datacenterId);
        return new DistributedIdGenerator(workerId, datacenterId);
    }
}
