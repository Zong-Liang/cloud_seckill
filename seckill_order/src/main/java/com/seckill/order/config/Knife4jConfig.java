package com.seckill.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 配置
 *
 * @author seckill
 * @since 1.0.0
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cloud Seckill - 订单服务 API")
                        .description("秒杀系统订单服务接口文档（含用户管理）")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Seckill Team")
                                .email("seckill@example.com")
                                .url("https://github.com/seckill"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
