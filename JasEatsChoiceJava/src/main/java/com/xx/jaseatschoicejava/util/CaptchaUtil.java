package com.xx.jaseatschoicejava.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 验证码工具类，用于验证码的生成和校验
 */
@Component
public class CaptchaUtil {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 验证验证码是否有效
     * @param captcha 前端传入的验证码
     * @param captchaKey 验证码对应的Key
     * @return 验证结果，true表示验证通过，false表示验证失败
     */
    public boolean validateCaptcha(String captcha, String captchaKey) {
        // 检查参数是否为空
        if (captcha == null || captchaKey == null) {
            return false;
        }

        // 从Redis获取验证码
        String redisCaptcha = redisTemplate.opsForValue().get("captcha:" + captchaKey);

        // 检查验证码是否存在
        if (redisCaptcha == null) {
            return false;
        }

        // 比较验证码
        return captcha.equals(redisCaptcha);
    }

    /**
     * 验证验证码是否有效，并在验证通过后删除Redis中的验证码
     * @param captcha 前端传入的验证码
     * @param captchaKey 验证码对应的Key
     * @return 验证结果，true表示验证通过，false表示验证失败
     */
    public boolean validateCaptchaAndDelete(String captcha, String captchaKey) {
        boolean isValid = validateCaptcha(captcha, captchaKey);
        if (isValid) {
            // 验证通过后删除Redis中的验证码
            redisTemplate.delete("captcha:" + captchaKey);
        }
        return isValid;
    }
}
