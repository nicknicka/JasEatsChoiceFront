package com.xx.jaseatschoicejava.util;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 自定义11位用户ID生成器
 */
@Component
public class CustomIdGenerator implements IdentifierGenerator {

    private final Random random = new Random();

    @Override
    public Long nextId(Object entity) {
        // 使用时间戳秒数 + 1位随机数生成11位唯一ID
        // 时间戳秒数是10位（当前时间：2025-12-05大约是1.7e9）
        // 乘以10并加上0-9的随机数得到11位ID
        long timestamp = System.currentTimeMillis() / 1000; // 10位
        int randomDigit = random.nextInt(10); // 0-9的随机数
        return timestamp * 10 + randomDigit;
    }
}
