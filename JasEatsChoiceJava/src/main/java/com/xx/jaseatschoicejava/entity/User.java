package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user")
@ApiModel(description = "用户实体")
public class User {

    @TableId(value = "user_id", type = com.baomidou.mybatisplus.annotation.IdType.INPUT)
    @ApiModelProperty(value = "用户ID (11位数字)")
    private Long userId; // 用户ID (11位数字)

    @TableField("phone")
    @ApiModelProperty(value = "手机号码（登录账号）")
    private String phone; // 手机号码（登录账号）

    @TableField("password")
    @ApiModelProperty(value = "密码")
    private String password; // 密码

    @TableField("nickname")
    @ApiModelProperty(value = "昵称")
    private String nickname; // 昵称

    @TableField("height")
    @ApiModelProperty(value = "身高（单位：cm）")
    private Double height; // 身高（单位：cm）

    @TableField("weight")
    @ApiModelProperty(value = "体重（单位：kg）")
    private Double weight; // 体重（单位：kg）

    @TableField("diet_goal")
    @ApiModelProperty(value = "饮食目标（如：减肥、增肌、保持健康）")
    private String dietGoal; // 饮食目标（如：减肥、增肌、保持健康）

    @TableField("allergies")
    @ApiModelProperty(value = "过敏食材列表（JSON格式）")
    private JsonNode allergies; // 过敏食材列表（JSON格式）

    @TableField("prefer_tags")
    @ApiModelProperty(value = "偏好标签列表（JSON格式）")
    private JsonNode preferTags; // 偏好标签列表（JSON格式）

    @TableField("email")
    @ApiModelProperty(value = "邮箱地址")
    private String email; // 邮箱地址

    @TableField("disable_weather_recommend")
    @ApiModelProperty(value = "是否关闭天气推荐：true-关闭，false-开启")
    private Boolean disableWeatherRecommend; // 是否关闭天气推荐：true-关闭，false-开启

    @TableField("merchant_id")
    @ApiModelProperty(value = "商家ID")
    private Long merchantId; // 商家ID，如果不为空表示用户已注册为商家

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间

    @TableField("avatar")
    @ApiModelProperty(value = "用户头像URL")
    private String avatar; // 用户头像URL

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getDietGoal() {
        return dietGoal;
    }

    public void setDietGoal(String dietGoal) {
        this.dietGoal = dietGoal;
    }

    public JsonNode getAllergies() {
        return allergies;
    }

    public void setAllergies(JsonNode allergies) {
        this.allergies = allergies;
    }

    public JsonNode getPreferTags() {
        return preferTags;
    }

    public void setPreferTags(JsonNode preferTags) {
        this.preferTags = preferTags;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getDisableWeatherRecommend() {
        return disableWeatherRecommend;
    }

    public void setDisableWeatherRecommend(Boolean disableWeatherRecommend) {
        this.disableWeatherRecommend = disableWeatherRecommend;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
