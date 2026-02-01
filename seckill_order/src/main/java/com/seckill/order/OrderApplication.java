package com.seckill.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动类
 *
 * @author seckill
 * @since 2.0.0
 */
@SpringBootApplication(scanBasePackages = { "com.seckill.order", "com.seckill.common" })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.seckill.order.feign")
@MapperScan("com.seckill.order.mapper")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
        System.out.println("========================================");
        System.out.println("    Seckill Order Service Started!     ");
        System.out.println("    Port: 9001                         ");
        System.out.println("    API Doc: http://localhost:9001/doc.html");
        System.out.println("========================================");
    }
}
