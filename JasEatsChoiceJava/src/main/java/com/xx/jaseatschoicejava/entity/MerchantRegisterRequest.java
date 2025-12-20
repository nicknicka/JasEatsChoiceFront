package com.xx.jaseatschoicejava.entity;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 商家注册请求参数
 */
public class MerchantRegisterRequest {
    /**
     * 商家名称
     */
    private String name;

    /**
     * 营业执照号
     */
    private String businessLicense;

    /**
     * 经营范围
     */
    private String[] businessScope;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 验证码会话key
     */
    private String captchaKey;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBusinessLicense() {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }

    public String[] getBusinessScope() {
        return businessScope;
    }

    public void setBusinessScope(String[] businessScope) {
        this.businessScope = businessScope;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getCaptchaKey() {
        return captchaKey;
    }

    public void setCaptchaKey(String captchaKey) {
        this.captchaKey = captchaKey;
    }
}
