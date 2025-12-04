package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.LoginRequest;
import com.xx.jaseatschoicejava.entity.RegisterRequest;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseResult<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            log.info("Received register request: {}", registerRequest.toString());
            // 验证验证码
            if (registerRequest.getCaptcha() == null || registerRequest.getCheckCodeKey() == null) {
                return ResponseResult.fail("400", "验证码不能为空");
            }

            // 从Redis获取验证码
            String redisCaptcha = redisTemplate.opsForValue().get("captcha:" + registerRequest.getCheckCodeKey());
            if (redisCaptcha == null) {
                return ResponseResult.fail("400", "验证码已过期");
            }

            // 比较验证码 - 使用严格的区分大小写比较
            if (!registerRequest.getCaptcha().equals(redisCaptcha)) {
                return ResponseResult.fail("400", "验证码错误");
            }

            // 验证码验证通过后，删除Redis中的验证码
            redisTemplate.delete("captcha:" + registerRequest.getCheckCodeKey());

            // 创建User对象并设置属性
            User user = new User();
            user.setPhone(registerRequest.getPhone());
            user.setPassword(registerRequest.getPassword());
            user.setNickname(registerRequest.getNickname());

            // 调用注册服务
            boolean success = userService.register(user);
            if (success) {
                return ResponseResult.success("注册成功");
            }
            return ResponseResult.fail("500", "注册失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "注册失败");
        }
    }

    /**
     * 用户登录
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("/login")
    public ResponseResult<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("Received login request: {}", loginRequest.toString());
            // 验证验证码
            if (loginRequest.getCaptcha() == null || loginRequest.getCheckCodeKey() == null) {
                return ResponseResult.fail("400", "验证码不能为空");
            }

            // 从Redis获取验证码
            String redisCaptcha = redisTemplate.opsForValue().get("captcha:" + loginRequest.getCheckCodeKey());
            log.info("Redis captcha: {}", redisCaptcha);
            log.info("Login request captcha: {}", loginRequest.getCaptcha());
            if (redisCaptcha == null) {
                return ResponseResult.fail("400", "验证码已过期");
            }

            // 比较验证码 - 使用严格的区分大小写比较
            if (!loginRequest.getCaptcha().equals(redisCaptcha)) {
                return ResponseResult.fail("400", "验证码错误");
            }

            // 验证码验证通过后，删除Redis中的验证码
            redisTemplate.delete("captcha:" + loginRequest.getCheckCodeKey());

            // 处理登录账号：前端传phone或username（统一作为手机号处理，因为User实体没有username字段）
            String account = loginRequest.getPhone();
            String password = loginRequest.getPassword();

            // 调用登录服务
            String token = userService.login(account, password);
            if (token != null) {
                return ResponseResult.success(token);
            }
            return ResponseResult.fail("500", "手机号或密码错误");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "登录失败");
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{userId}")
    public ResponseResult<?> getUserInfo(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user != null) {
            return ResponseResult.success(user);
        }
        return ResponseResult.fail("404", "用户不存在");
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    public ResponseResult<?> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> updateData) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return ResponseResult.fail("404", "用户不存在");
            }

            // Update phone if provided
            if (updateData.containsKey("phone")) {
                String newPhone = (String) updateData.get("phone");
                // TODO: Validate new phone format

                // Check verification code if phone is changed
                if (!user.getPhone().equals(newPhone)) {
                    // TODO: Verify SMS verification code from updateData
                }

                user.setPhone(newPhone);
            }

            // Update email if provided
            if (updateData.containsKey("email")) {
                String newEmail = (String) updateData.get("email");
                // TODO: Validate new email format

                // Check verification code if email is changed
                if (!user.getEmail().equals(newEmail)) {
                    // TODO: Verify email verification code from updateData
                }

                user.setEmail(newEmail);
            }

            // Update the user
            boolean success = userService.updateById(user);
            if (success) {
                return ResponseResult.success("更新成功");
            }
            return ResponseResult.fail("500", "更新失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "更新失败");
        }
    }

    /**
     * 获取用户偏好设置
     */
    @GetMapping("/{userId}/preferences")
    public ResponseResult<?> getPreferences(@PathVariable Long userId) {
        // TODO: Implement preference retrieval from the database
        // For now, return mock data
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("dietaryRestrictions", "无");
        preferences.put("calorieTarget", 2000);
        preferences.put("preferredCuisine", "中式");
        preferences.put("allergies", new ArrayList<>());

        return ResponseResult.success(preferences);
    }

    /**
     * 发送手机验证码
     */
    @PostMapping("/send-sms-code")
    public ResponseResult<?> sendSmsCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || phone.isEmpty()) {
            return ResponseResult.fail("400", "手机号不能为空");
        }

        // TODO: Implement actual SMS sending logic here
        // For demonstration, generate a random 6-digit code
        String code = String.format("%06d", (int)(Math.random() * 1000000));

        // Store code in Redis with 5 minutes expiration
        redisTemplate.opsForValue().set("sms-code:" + phone, code, 5, TimeUnit.MINUTES);

        return ResponseResult.success("手机验证码已发送");
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-email-code")
    public ResponseResult<?> sendEmailCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseResult.fail("400", "邮箱地址不能为空");
        }

        // TODO: Implement actual email sending logic here
        // For demonstration, generate a random 6-digit code
        String code = String.format("%06d", (int)(Math.random() * 1000000));

        // Store code in Redis with 5 minutes expiration
        redisTemplate.opsForValue().set("email-code:" + email, code, 5, TimeUnit.MINUTES);

        return ResponseResult.success("邮箱验证码已发送");
    }

    /**
     * 更新用户偏好设置
     */
    @PutMapping("/{userId}/preferences")
    public ResponseResult<?> updatePreferences(@PathVariable Long userId, @RequestBody Map<String, Object> preferences) {
        // TODO: Implement preference update in the database
        // For now, return success
        return ResponseResult.success("用户偏好设置已更新");
    }
}
