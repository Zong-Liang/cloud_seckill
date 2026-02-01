package com.seckill.stock.runner;

import com.seckill.stock.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 库存初始化 Runner
 * <p>
 * 服务启动时自动将商品库存加载到 Redis
 * </p>
 *
 * @author seckill
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockInitRunner implements ApplicationRunner {

    private final SeckillService seckillService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始初始化商品库存到 Redis...");
        try {
            seckillService.initAllGoodsStock();
            log.info("商品库存初始化完成");
        } catch (Exception e) {
            log.error("商品库存初始化失败: {}", e.getMessage(), e);
        }
    }
}
