package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.mapper.UserMapper;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 注册用户并对密码进行加密
     * @param user 用户对象
     * @return 注册成功返回true，否则返回false
     */
    @Override
    public boolean register(User user) {
        // 对密码进行加密
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        return save(user);
    }

    /**
     * 用户登录，如果成功则返回JWT令牌
     * @param account 登录账号（手机号码）
     * @param password 密码
     * @return 登录成功返回JWT令牌，否则返回null
     */
    @Override
    public String login(String account, String password) {
        // 在我们的系统中，登录账号始终是手机号码
        User user = lambdaQuery()
                .eq(User::getPhone, account)
                .one();

        // 检查用户是否存在并验证密码是否正确
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 生成JWT令牌
            return JwtUtil.generateToken(user.getUserId(), user.getPhone());
        }
        return null;
    }
}
