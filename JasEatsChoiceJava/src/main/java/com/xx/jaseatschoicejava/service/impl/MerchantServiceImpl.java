package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.mapper.MerchantMapper;
import com.xx.jaseatschoicejava.service.MerchantService;
import com.xx.jaseatschoicejava.util.IdGenerator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 商家服务实现
 */
@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public boolean register(Merchant merchant) {
        // 生成商家ID
        Long merchantId = IdGenerator.generateId();
        merchant.setId(merchantId);
        // 加密密码
        String encryptedPassword = passwordEncoder.encode(merchant.getPassword());
        merchant.setPassword(encryptedPassword);
        // 默认新注册商家为营业状态
        merchant.setStatus(true);
        // 设置创建时间
        merchant.setCreateTime(LocalDateTime.now());
        merchant.setUpdateTime(LocalDateTime.now());
        return save(merchant);
    }
}
