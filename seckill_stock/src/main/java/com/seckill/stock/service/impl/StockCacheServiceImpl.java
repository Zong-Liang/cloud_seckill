package com.seckill.stock.service.impl;

import com.seckill.common.constant.RedisKeyConstants;
import com.seckill.stock.service.StockCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * 库存缓存服务实现
 * <p>
 * 使用 Java 8 特性重构：
 * </p>
 * <ul>
 * <li>Optional 处理空值</li>
 * <li>BiFunction 封装操作</li>
 * <li>Lambda 简化日志</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockCacheServiceImpl implements StockCacheService {

    private final StringRedisTemplate redisTemplate;

    /**
     * Lua 脚本：原子性扣减库存
     */
    private static final String DEDUCT_STOCK_LUA = "local stock = redis.call('get', KEYS[1]) " +
            "if stock == false then return -2 end " +
            "local stockNum = tonumber(stock) " +
            "local count = tonumber(ARGV[1]) " +
            "if stockNum < count then return -1 end " +
            "local newStock = stockNum - count " +
            "redis.call('set', KEYS[1], newStock) " +
            "return newStock";

    /**
     * Lua 脚本：回滚库存
     */
    private static final String ROLLBACK_STOCK_LUA = "local stock = redis.call('get', KEYS[1]) " +
            "if stock == false then return -1 end " +
            "local newStock = tonumber(stock) + tonumber(ARGV[1]) " +
            "redis.call('set', KEYS[1], newStock) " +
            "return newStock";

    /**
     * 脚本执行器（函数式风格）
     */
    private final BiFunction<String, String, Long> luaExecutor = (script, args) -> {
        throw new UnsupportedOperationException("Use executeScript method");
    };

    @Override
    public void initStock(Long goodsId, Integer stockCount) {
        String key = RedisKeyConstants.stockKey(goodsId);
        redisTemplate.opsForValue().set(
                key,
                String.valueOf(stockCount),
                RedisKeyConstants.STOCK_EXPIRE_SECONDS,
                TimeUnit.SECONDS);
        log.info("初始化商品库存到 Redis - goodsId: {}, stock: {}", goodsId, stockCount);
    }

    @Override
    public Long deductStock(Long goodsId, Integer count) {
        String key = RedisKeyConstants.stockKey(goodsId);
        Long result = executeScript(DEDUCT_STOCK_LUA, key, count.toString());

        // 使用 Optional 处理日志
        Optional.ofNullable(result).ifPresent(r -> {
            if (r >= 0) {
                log.debug("Redis 库存扣减成功 - goodsId: {}, count: {}, remaining: {}", goodsId, count, r);
            } else if (r == -1) {
                log.warn("Redis 库存不足 - goodsId: {}, count: {}", goodsId, count);
            } else if (r == -2) {
                log.warn("Redis 库存未初始化 - goodsId: {}", goodsId);
            }
        });

        return result;
    }

    @Override
    public Long rollbackStock(Long goodsId, Integer count) {
        String key = RedisKeyConstants.stockKey(goodsId);
        Long result = executeScript(ROLLBACK_STOCK_LUA, key, count.toString());

        Optional.ofNullable(result)
                .filter(r -> r >= 0)
                .ifPresent(r -> log.info("Redis 库存回滚成功 - goodsId: {}, count: {}, current: {}",
                        goodsId, count, r));

        return result;
    }

    @Override
    public Long getStock(Long goodsId) {
        String key = RedisKeyConstants.stockKey(goodsId);

        // 使用 Optional 优雅处理空值和转换
        return Optional.ofNullable(redisTemplate.opsForValue().get(key))
                .map(Long::parseLong)
                .orElse(null);
    }

    @Override
    public boolean hasStock(Long goodsId, Integer count) {
        // 使用 Optional 链式判断
        return Optional.ofNullable(getStock(goodsId))
                .filter(stock -> stock >= count)
                .isPresent();
    }

    @Override
    public void deleteStock(Long goodsId) {
        String key = RedisKeyConstants.stockKey(goodsId);
        redisTemplate.delete(key);
        log.info("删除商品库存缓存 - goodsId: {}", goodsId);
    }

    /**
     * 执行 Lua 脚本
     */
    private Long executeScript(String scriptContent, String key, String arg) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(scriptContent, Long.class);
        return redisTemplate.execute(script, Collections.singletonList(key), arg);
    }
}
