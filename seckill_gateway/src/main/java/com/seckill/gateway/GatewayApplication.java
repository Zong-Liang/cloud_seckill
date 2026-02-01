package com.seckill.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关服务启动类
 *
 * @author seckill
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("========================================");
        System.out.println("    Seckill Gateway Service Started!   ");
        System.out.println("    Port: 9000                         ");
        System.out.println("    Stock API: /api/stock/**           ");
        System.out.println("    Order API: /api/order/**           ");
        System.out.println("    User API:  /api/user/**            ");
        System.out.println("========================================");
    }
}
