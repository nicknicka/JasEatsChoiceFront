
package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 优惠活动实体类
 */
@Data
@TableName("discount")
public class Discount {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "优惠ID")
    private Long id;

    @ApiModelProperty(value = "商家ID")
    private String merchantId; // 商家ID

    @ApiModelProperty(value = "优惠名称")
    private String name;

    @ApiModelProperty(value = "优惠类型")
    private String type; // 满减、折扣、新人优惠、限时特惠等

    @ApiModelProperty(value = "优惠描述")
    private String description;

    @ApiModelProperty(value = "优惠状态")
    private String status; // active: 启用, inactive: 禁用

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
