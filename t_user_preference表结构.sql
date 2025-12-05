-- 创建用户偏好设置表
CREATE TABLE `t_user_preference` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tag_weights` varchar(1000) DEFAULT '{}' COMMENT '标签权重，JSON格式（如 {"spicy": 0.1, "sweet": 0.8}）',
  `disable_weather_recommend` bit(1) DEFAULT b'0' COMMENT '是否关闭天气推荐',
  `diet_goal` varchar(50) DEFAULT NULL COMMENT '饮食目标（如 low_calorie, high_protein 等）',
  `allergies` varchar(500) DEFAULT '[]' COMMENT '过敏食材列表，JSON格式',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户推荐偏好表';

-- 创建评价表的索引
ALTER TABLE `t_review` ADD INDEX `idx_rating_create_time` (`rating`, `create_time`);
