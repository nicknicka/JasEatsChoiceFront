package com.xx.jaseatschoicejava.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.xx.jaseatschoicejava.common.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码控制器
 */
@RestController
@RequestMapping("/")
@Api(tags = "验证码接口")
public class CaptchaController {

    @Autowired
    private DefaultKaptcha defaultKaptcha;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 生成验证码
     */
    @GetMapping("/checkCode")
    @ApiOperation("生成验证码")
    public ResponseResult<?> generateCaptcha() {
        try {
            // 生成验证码文本
            String captchaText = defaultKaptcha.createText();

            // 生成验证码图片
            BufferedImage captchaImage = defaultKaptcha.createImage(captchaText);

            // 将图片转换为Base64编码
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(captchaImage, "png", outputStream);
            String captchaBase64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            outputStream.close();

            // 生成唯一key用于验证
            String checkCodeKey = UUID.randomUUID().toString().replace("-", "");

            // 将验证码存入Redis，有效期5分钟
            redisTemplate.opsForValue().set("captcha:" + checkCodeKey, captchaText, 5, TimeUnit.MINUTES);

            // 构造返回结果
            Map<String, String> result = new HashMap<>();
            result.put("checkCode", captchaBase64);
            result.put("checkCodeKey", checkCodeKey);

            return ResponseResult.success(result);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "生成验证码失败");
        }
    }
}
