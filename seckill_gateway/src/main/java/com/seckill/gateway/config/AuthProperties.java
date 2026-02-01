package com.seckill.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证配置类
 *
 * @author seckill
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /**
     * 白名单路径列表（无需登录的接口）
     */
    private List<String> whitelist = new ArrayList<>();
}
