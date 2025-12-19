package com.xx.jaseatschoicejava.controller.advice;

import com.xx.jaseatschoicejava.config.FileUploadConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceController implements WebMvcConfigurer {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源访问路径
        registry.addResourceHandler(fileUploadConfig.getUrlPrefix() + "**")
                .addResourceLocations("file:" + fileUploadConfig.getUploadPath());
    }
}
