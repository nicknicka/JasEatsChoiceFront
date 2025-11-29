package com.xx.jaseatschoicejava.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS配置类 - 全局解决跨域问题
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@SuppressWarnings("NullableProblems") CorsRegistry registry) {
        registry
                // 允许所有路径
                .addMapping("/**")
                // 允许所有来源
                .allowedOriginPatterns("*")
                // 允许所有HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许所有请求头
                .allowedHeaders("*")
                // 允许携带凭证
                .allowCredentials(true);
    }
}