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
import java.math.BigDecimal;

/**
 * 商家实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_merchant")
@ApiModel(description = "商家实体")
public class Merchant {

    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.INPUT)
    @ApiModelProperty(value = "商家ID")
    private Long id; // 商家ID

    @TableField("name")
    @ApiModelProperty(value = "商家名称")
    private String name; // 商家名称

    @TableField("address")
    @ApiModelProperty(value = "商家地址")
    private String address; // 商家地址

    @TableField("longitude")
    @ApiModelProperty(value = "经度")
    private BigDecimal longitude; // 经度

    @TableField("latitude")
    @ApiModelProperty(value = "纬度")
    private BigDecimal latitude; // 纬度

    @TableField("category")
    @ApiModelProperty(value = "商家分类")
    private String category; // 商家分类

    @TableField("phone")
    @ApiModelProperty(value = "联系电话")
    private String phone; // 联系电话

    @TableField("password")
    @ApiModelProperty(value = "密码")
    private String password; // 密码

    @TableField("email")
    @ApiModelProperty(value = "邮箱")
    private String email; // 邮箱

    @TableField("business_license")
    @ApiModelProperty(value = "营业执照号")
    private String businessLicense; // 营业执照号

    @TableField("business_scope")
    @ApiModelProperty(value = "经营范围")
    private String businessScope; // 经营范围

    @TableField("contact_name")
    @ApiModelProperty(value = "联系人姓名")
    private String contactName; // 联系人姓名
    @TableField("avatar")
    private String avatar; // 商家头像

    @TableField("rating")
    @ApiModelProperty(value = "商家评分")
    private BigDecimal rating; // 商家评分

    @TableField("business_hours")
    @ApiModelProperty(value = "营业时间")
    private String businessHours; // 营业时间

    @TableField("average_price")
    @ApiModelProperty(value = "人均消费")
    private BigDecimal averagePrice; // 人均消费

    @TableField("status")
    @ApiModelProperty(value = "状态：true-营业，false-休息")
    private Boolean status; // 状态：true-营业，false-休息

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间

    @TableField("album")
    @ApiModelProperty(value = "店铺相册（JSON格式，包含environment和dishes两个数组）")
    private JsonNode album; // 店铺相册（JSON格式，包含environment和dishes两个数组）

    // 手动添加缺失的getter/setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBusinessLicense() {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }

    public String getBusinessScope() {
        return businessScope;
    }

    public void setBusinessScope(String businessScope) {
        this.businessScope = businessScope;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
}
