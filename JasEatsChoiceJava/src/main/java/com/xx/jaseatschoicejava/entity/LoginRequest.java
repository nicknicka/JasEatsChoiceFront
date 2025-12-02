package com.xx.jaseatschoicejava.entity;

import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginRequest {
    /**
     * 用户名/手机号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 验证码会话key
     */
    private String checkCodeKey;

    /**
     * 手机号（兼容之前的登录方式）
     */
    private String phone;
}
