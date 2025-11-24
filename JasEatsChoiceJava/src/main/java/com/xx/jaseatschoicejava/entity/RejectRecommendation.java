package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐拒绝行为实体
 */
@Data
@TableName("t_reject_recommendation")
public class RejectRecommendation {
    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 菜品ID
     */
    private Long dishId;

    /**
     * 拒绝时间
     */
    private LocalDateTime rejectTime;

    /**
     * 拒绝原因
     */
    private String reason;
}
