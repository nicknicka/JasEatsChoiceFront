package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.LoginRequest;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
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
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseResult<?> register(@RequestBody User user) {
        boolean success = userService.register(user);
        if (success) {
            return ResponseResult.success("注册成功");
        }
        return ResponseResult.fail("500", "注册失败");
    }

    /**
     * 用户登录
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("/login")
    public ResponseResult<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 验证验证码
            if (loginRequest.getCaptcha() == null || loginRequest.getCheckCodeKey() == null) {
                return ResponseResult.fail("400", "验证码不能为空");
            }

            // 从Redis获取验证码
            String redisCaptcha = redisTemplate.opsForValue().get("captcha:" + loginRequest.getCheckCodeKey());
            if (redisCaptcha == null) {
                return ResponseResult.fail("400", "验证码已过期");
            }

            // 比较验证码
            if (!loginRequest.getCaptcha().equalsIgnoreCase(redisCaptcha)) {
                return ResponseResult.fail("400", "验证码错误");
            }

            // 验证码验证通过后，删除Redis中的验证码
            redisTemplate.delete("captcha:" + loginRequest.getCheckCodeKey());

            // 兼容处理：如果前端发送的是username，将其作为phone处理
            String phone = loginRequest.getPhone() != null ? loginRequest.getPhone() : loginRequest.getUsername();
            String password = loginRequest.getPassword();

            // 调用登录服务
            String token = userService.login(phone, password);
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
