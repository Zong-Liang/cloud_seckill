package com.seckill.stock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 库存服务启动类
 *
 * @author seckill
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = { "com.seckill.stock", "com.seckill.common" })
@EnableDiscoveryClient
@MapperScan("com.seckill.stock.mapper")
public class StockApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockApplication.class, args);
        System.out.println("========================================");
        System.out.println("    Seckill Stock Service Started!     ");
        System.out.println("    Port: 9002                         ");
        System.out.println("    API Doc: http://localhost:9002/doc.html");
        System.out.println("========================================");
    }
}
