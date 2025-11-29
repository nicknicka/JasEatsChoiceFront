package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    @PostMapping("/login")
    public ResponseResult<?> login(@RequestBody User loginUser) {
        String token = userService.login(loginUser.getPhone(), loginUser.getPassword());
        if (token != null) {
            return ResponseResult.success(token);
        }
        return ResponseResult.fail("500", "手机号或密码错误");
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
     * 更新用户偏好设置
     */
    @PutMapping("/{userId}/preferences")
    public ResponseResult<?> updatePreferences(@PathVariable Long userId, @RequestBody Map<String, Object> preferences) {
        // TODO: Implement preference update in the database
        // For now, return success
        return ResponseResult.success("用户偏好设置已更新");
    }
}
