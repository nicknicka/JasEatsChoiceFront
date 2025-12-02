package com.xx.jaseatschoicejava.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 验证码配置类
 */
@Configuration
public class CaptchaConfig {

    @Bean
    public DefaultKaptcha getDefaultKaptcha() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();

        // 验证码图片宽度
        properties.put("kaptcha.image.width", "150");
        // 验证码图片高度
        properties.put("kaptcha.image.height", "50");
        // 验证码文本字符大小
        properties.put("kaptcha.textproducer.font.size", "40");
        // 验证码文本字符颜色
        properties.put("kaptcha.textproducer.font.color", "blue");
        // 验证码文本字符长度
        properties.put("kaptcha.textproducer.char.length", "4");
        // 验证码文本字符集
        properties.put("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        // 验证码噪声颜色
        properties.put("kaptcha.noise.color", "black");
        // 验证码噪声实现类
        properties.put("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");
        // 验证码文本渲染样式
        properties.put("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.ShadowGimpy");
        // 验证码背景渐变颜色
        properties.put("kaptcha.background.clear.from", "white");
        properties.put("kaptcha.background.clear.to", "white");

        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
