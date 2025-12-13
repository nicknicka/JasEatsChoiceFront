-- 佳食宜选数据库 - 收藏和消费历史表创建脚本

-- 使用数据库
USE `jia_shi_yi_xuan`;

-- --------------------------------------------------------
-- 创建表: 用户收藏表(t_collection)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_collection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `collectable_type` varchar(50) NOT NULL COMMENT '收藏类型（merchant, dish, article, recipe等）',
  `collectable_id` bigint(20) NOT NULL COMMENT '收藏对象ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_collectable` (`user_id`,`collectable_type`,`collectable_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_type` (`collectable_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- --------------------------------------------------------
-- 创建表: 消费历史表(t_consume_history)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_consume_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '类型：recharge（充值）、consume（消费）、withdraw（提现）',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `status` varchar(20) NOT NULL DEFAULT 'success' COMMENT '状态：success（成功）、failed（失败）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费历史表';
