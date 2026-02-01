package com.seckill.stock.service.impl;

import com.seckill.common.constant.RedisKeyConstants;
import com.seckill.common.dto.SeckillMessage;
import com.seckill.common.exception.BusinessException;
import com.seckill.common.id.DistributedIdGenerator;
import com.seckill.common.result.ResultCode;
import com.seckill.stock.dto.SeckillRequest;
import com.seckill.stock.entity.SeckillGoods;
import com.seckill.stock.mq.SeckillMessageProducer;
import com.seckill.stock.service.DistributedLockService;
import com.seckill.stock.service.GoodsService;
import com.seckill.stock.service.SeckillService;
import com.seckill.stock.service.StockCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 秒杀服务实现
 * <p>
 * 使用 Java 8 特性重构：
 * </p>
 * <ul>
 * <li>Optional 优雅处理空值</li>
 * <li>Lambda 简化回调逻辑</li>
 * <li>Supplier/Function 函数式编程</li>
 * <li>方法引用提升可读性</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final GoodsService goodsService;
    private final StockCacheService stockCacheService;
    private final DistributedLockService lockService;
    private final SeckillMessageProducer messageProducer;
    private final StringRedisTemplate redisTemplate;
    private final DistributedIdGenerator idGenerator;

    /**
     * 分布式锁过期时间（秒）
     */
    private static final long LOCK_EXPIRE_SECONDS = 10L;

    @Override
    public Long doSeckill(SeckillRequest request) {
        Objects.requireNonNull(request, "request cannot be null");

        final Long userId = request.getUserId();
        final Long goodsId = request.getGoodsId();
        final Integer count = Optional.ofNullable(request.getCount()).orElse(1);

        log.info("开始秒杀 - userId: {}, goodsId: {}, count: {}", userId, goodsId, count);

        // 1. 获取分布式锁
        String lockRequestId = Optional.ofNullable(lockService.lockSeckill(goodsId, userId, LOCK_EXPIRE_SECONDS))
                .orElseThrow(() -> {
                    log.warn("获取秒杀锁失败 - userId: {}, goodsId: {}", userId, goodsId);
                    return new BusinessException(ResultCode.RATE_LIMIT.getCode(), "请勿重复点击，请稍后再试");
                });

        // 回滚状态追踪
        SeckillContext context = new SeckillContext();

        try {
            // 2. 检查是否已秒杀
            if (hasKilled(userId, goodsId)) {
                log.warn("用户已秒杀过该商品 - userId: {}, goodsId: {}", userId, goodsId);
                throw new BusinessException(ResultCode.REPEAT_ORDER);
            }

            // 3. 校验商品状态
            SeckillGoods goods = goodsService.checkSeckillable(goodsId);

            // 4. Redis 预扣减库存
            Long remaining = deductStockWithRetry(goodsId, count);
            context.setStockDeducted(true);

            // 5. 生成订单号
            Long orderNo = idGenerator.nextId();
            context.setOrderNo(orderNo);

            // 6. 标记用户已秒杀
            markKilled(userId, goodsId);
            context.setKilledMarked(true);

            // 7. 发送 MQ 消息
            sendSeckillMessage(goods, request, orderNo);

            log.info("秒杀成功 - userId: {}, goodsId: {}, orderNo: {}", userId, goodsId, orderNo);
            return orderNo;

        } catch (BusinessException e) {
            rollbackOnFailure(goodsId, userId, count, context);
            throw e;
        } catch (Exception e) {
            log.error("秒杀异常 - userId: {}, goodsId: {}", userId, goodsId, e);
            rollbackOnFailure(goodsId, userId, count, context);
            throw new BusinessException(ResultCode.SYSTEM_ERROR);
        } finally {
            lockService.unlockSeckill(goodsId, userId, lockRequestId);
        }
    }

    /**
     * 扣减库存（带重试）
     */
    private Long deductStockWithRetry(Long goodsId, Integer count) {
        // 使用 Supplier 封装重试逻辑
        Supplier<Long> deductOperation = () -> stockCacheService.deductStock(goodsId, count);

        Long remaining = deductOperation.get();

        // 库存未初始化，尝试初始化后重试
        if (remaining != null && remaining == -2) {
            initGoodsStock(goodsId);
            remaining = deductOperation.get();
        }

        return Optional.ofNullable(remaining)
                .filter(r -> r >= 0)
                .orElseThrow(() -> {
                    log.warn("Redis 库存不足 - goodsId: {}, count: {}", goodsId, count);
                    return new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
                });
    }

    /**
     * 发送秒杀消息
     */
    private void sendSeckillMessage(SeckillGoods goods, SeckillRequest request, Long orderNo) {
        SeckillMessage message = new SeckillMessage();
        message.setUserId(request.getUserId());
        message.setGoodsId(request.getGoodsId());
        message.setGoodsName(goods.getGoodsName());
        message.setGoodsImg(goods.getGoodsImg());
        message.setSeckillPrice(goods.getSeckillPrice());
        message.setCount(request.getCount());
        message.setChannel(request.getChannel());
        message.setOrderNo(orderNo);
        message.setTimestamp(System.currentTimeMillis());

        messageProducer.sendSeckillMessage(message, orderNo);
    }

    /**
     * 失败回滚
     */
    private void rollbackOnFailure(Long goodsId, Long userId, Integer count, SeckillContext context) {
        try {
            // 使用 Optional 链式调用回滚逻辑
            Optional.of(context)
                    .filter(SeckillContext::isStockDeducted)
                    .ifPresent(ctx -> {
                        stockCacheService.rollbackStock(goodsId, count);
                        log.info("回滚 Redis 库存成功 - goodsId: {}, count: {}", goodsId, count);
                    });

            Optional.of(context)
                    .filter(SeckillContext::isKilledMarked)
                    .ifPresent(ctx -> {
                        removeKilledMark(userId, goodsId);
                        log.info("清除已秒杀标记成功 - userId: {}, goodsId: {}", userId, goodsId);
                    });

            log.info("秒杀失败回滚完成 - userId: {}, goodsId: {}", userId, goodsId);

        } catch (Exception rollbackEx) {
            log.error("回滚操作失败 - userId: {}, goodsId: {}", userId, goodsId, rollbackEx);
        }
    }

    @Override
    public void removeKilledMark(Long userId, Long goodsId) {
        String key = RedisKeyConstants.killedKey(goodsId, userId);
        redisTemplate.delete(key);
        log.debug("清除已秒杀标记 - userId: {}, goodsId: {}", userId, goodsId);
    }

    @Override
    public void initGoodsStock(Long goodsId) {
        Optional.ofNullable(goodsService.getById(goodsId))
                .ifPresent(goods -> {
                    stockCacheService.initStock(goodsId, goods.getStockCount());
                    log.info("初始化商品库存到 Redis - goodsId: {}, stock: {}", goodsId, goods.getStockCount());
                });
    }

    @Override
    public void initAllGoodsStock() {
        // 使用 Stream API 批量初始化
        goodsService.list().stream()
                .filter(Objects::nonNull)
                .forEach(goods -> stockCacheService.initStock(goods.getId(), goods.getStockCount()));

        log.info("初始化所有商品库存到 Redis");
    }

    @Override
    public boolean hasKilled(Long userId, Long goodsId) {
        String key = RedisKeyConstants.killedKey(goodsId, userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void markKilled(Long userId, Long goodsId) {
        String key = RedisKeyConstants.killedKey(goodsId, userId);
        redisTemplate.opsForValue().set(key, "1", RedisKeyConstants.KILLED_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 秒杀上下文（用于回滚状态追踪）
     */
    private static class SeckillContext {
        private boolean stockDeducted;
        private boolean killedMarked;
        private Long orderNo;

        boolean isStockDeducted() {
            return stockDeducted;
        }

        void setStockDeducted(boolean stockDeducted) {
            this.stockDeducted = stockDeducted;
        }

        boolean isKilledMarked() {
            return killedMarked;
        }

        void setKilledMarked(boolean killedMarked) {
            this.killedMarked = killedMarked;
        }

        void setOrderNo(Long orderNo) {
            this.orderNo = orderNo;
        }
    }
}
