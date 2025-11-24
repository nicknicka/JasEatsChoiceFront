-- 创建推荐拒绝行为表
CREATE TABLE `t_reject_recommendation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `dish_id` bigint(20) NOT NULL COMMENT '菜品ID',
  `reject_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拒绝时间',
  `reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_reject_time` (`reject_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐拒绝行为表';
