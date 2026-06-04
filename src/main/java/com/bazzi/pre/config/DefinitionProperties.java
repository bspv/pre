package com.bazzi.pre.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "definition")
public class DefinitionProperties {

    @Value("${spring.cache.redis.key-prefix}")
    private String cachePrefix;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 跨域配置，对应配置文件中的 definition.cors.*
     */
    private Cors cors = new Cors();

    public boolean isProduction() {
        return "prod".equals(activeProfile);
    }

    @Data
    public static class Cors {
        /**
         * 允许跨域访问的源，对应 definition.cors.allowed-origins
         * 支持精确地址（如 https://www.example.com）和通配符模式（如 https://*.example.com）
         */
        private List<String> allowedOrigins = new ArrayList<>();
    }
}
