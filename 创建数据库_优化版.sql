-- --------------------------------------------------------
-- 佳食宜选数据库创建脚本(优化版)
-- 版本: 2.0
-- 日期: 2025-12-13
-- 优化点：
-- 1. 添加外键约束确保数据一致性
-- 2. 优化字段类型和长度
-- 3. 合并冗余表
-- 4. 优化索引设计
-- --------------------------------------------------------

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `jia_shi_yi_xuan`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 切换到数据库
USE `jia_shi_yi_xuan`;

-- --------------------------------------------------------
-- 创建表: 用户表(t_user) - 合并了用户偏好表的功能
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` varchar(11) NOT NULL COMMENT '手机号',
  `password` varchar(64) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `height` decimal(5,1) DEFAULT NULL COMMENT '身高(cm)',
  `weight` decimal(5,1) DEFAULT NULL COMMENT '体重(kg)',
  `diet_goal` varchar(20) DEFAULT NULL COMMENT '饮食目标（lose_weight/keep_fit/gain_weight）',
  `allergies` json DEFAULT NULL COMMENT '过敏食材列表',
  `prefer_tags` json DEFAULT NULL COMMENT '饮食偏好标签（JSON格式）',
  `tag_weights` json DEFAULT '{}' COMMENT '标签权重，JSON格式',
  `disable_weather_recommend` tinyint(1) DEFAULT '0' COMMENT '是否关闭天气推荐',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- --------------------------------------------------------
-- 创建表: 商家表(t_merchant)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_merchant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `name` varchar(100) NOT NULL COMMENT '商家名称',
  `address` varchar(255) NOT NULL COMMENT '商家地址',
  `longitude` decimal(10,6) NOT NULL COMMENT '经度',
  `latitude` decimal(10,6) NOT NULL COMMENT '纬度',
  `category` varchar(50) NOT NULL COMMENT '商家分类',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `business_hours` json NOT NULL COMMENT '营业时间（JSON格式）',
  `average_price` decimal(10,2) DEFAULT NULL COMMENT '人均消费',
  `status` tinyint(1) DEFAULT '1' COMMENT '商家状态（0-关闭/1-营业）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_location` (`longitude`,`latitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

-- --------------------------------------------------------
-- 创建表: 菜品表(t_dish)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_dish` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜品ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `name` varchar(100) NOT NULL COMMENT '菜品名称',
  `category` varchar(50) NOT NULL COMMENT '菜品分类',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `calorie` int(11) NOT NULL COMMENT '卡路里',
  `ingredients` json DEFAULT NULL COMMENT '食材列表（JSON格式）',
  `description` text DEFAULT NULL COMMENT '菜品描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '菜品状态（0-下架/1-上架）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_category` (`category`),
  KEY `idx_calorie` (`calorie`),
  CONSTRAINT `fk_dish_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `t_merchant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- --------------------------------------------------------
-- 创建表: 菜单表(t_menu)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `name` varchar(100) NOT NULL COMMENT '菜单名称',
  `type` varchar(20) NOT NULL COMMENT '菜单类型（workday/weekend/afternoon_tea/package等）',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '菜单状态（active/draft/inactive）',
  `auto_start_time` datetime DEFAULT NULL COMMENT '自动上架时间',
  `auto_end_time` datetime DEFAULT NULL COMMENT '自动下架时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_type_status` (`type`,`status`),
  CONSTRAINT `fk_menu_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `t_merchant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- --------------------------------------------------------
-- 创建表: 菜单菜品关联表(t_menu_dish)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_menu_dish` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `dish_id` bigint(20) NOT NULL COMMENT '菜品ID',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_dish` (`menu_id`,`dish_id`),
  KEY `idx_menu` (`menu_id`),
  KEY `idx_dish` (`dish_id`),
  CONSTRAINT `fk_menu_dish_menu` FOREIGN KEY (`menu_id`) REFERENCES `t_menu` (`id`),
  CONSTRAINT `fk_menu_dish_dish` FOREIGN KEY (`dish_id`) REFERENCES `t_dish` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单菜品关联表';

-- --------------------------------------------------------
-- 创建表: 订单表(t_order) - 优化：关联地址表
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `address_id` bigint(20) NOT NULL COMMENT '地址ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '订单状态（0-待支付/1-待接单/2-备菜中/3-烹饪中/4-待上菜/5-已完成/6-已取消）',
  `remark` varchar(255) DEFAULT NULL COMMENT '订单备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_address` (`address_id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`),
  CONSTRAINT `fk_order_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `t_merchant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- --------------------------------------------------------
-- 创建表: 订单菜品表(t_order_dish)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_order_dish` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `dish_id` bigint(20) NOT NULL COMMENT '菜品ID',
  `quantity` int(11) NOT NULL COMMENT '数量',
  `price` decimal(10,2) NOT NULL COMMENT '单价',
  `customization` varchar(255) DEFAULT NULL COMMENT '定制要求',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_dish` (`dish_id`),
  CONSTRAINT `fk_order_dish_order` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`id`),
  CONSTRAINT `fk_order_dish_dish` FOREIGN KEY (`dish_id`) REFERENCES `t_dish` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单菜品表';

-- --------------------------------------------------------
-- 创建表: 卡路里摄入记录表(t_calorie_record)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_calorie_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `dish_id` bigint(20) DEFAULT NULL COMMENT '菜品ID',
  `calorie` int(11) NOT NULL COMMENT '卡路里',
  `meal_time` varchar(20) NOT NULL COMMENT '餐次（breakfast/lunch/dinner/snack）',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`record_time`),
  KEY `idx_meal_time` (`meal_time`),
  KEY `idx_user_meal_time` (`user_id`, `meal_time`, `record_time`),
  CONSTRAINT `fk_calorie_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`),
  CONSTRAINT `fk_calorie_dish` FOREIGN KEY (`dish_id`) REFERENCES `t_dish` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡路里摄入记录表';

-- --------------------------------------------------------
-- 创建表: 聊天消息表(t_chat_msg) - 优化：使用bigint代替varchar存储ID
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_chat_msg` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `from_id` bigint(20) NOT NULL COMMENT '发送方ID',
  `to_id` bigint(20) NOT NULL COMMENT '接收方ID',
  `msg_type` varchar(20) NOT NULL COMMENT '消息类型（single/group/order_sync）',
  `content` text NOT NULL COMMENT '消息内容',
  `read_status` tinyint(1) DEFAULT '0' COMMENT '已读状态（0-未读/1-已读）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_to` (`from_id`,`to_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- --------------------------------------------------------
-- 创建表: 系统通知表(t_notification)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `title` varchar(100) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `read_status` tinyint(1) DEFAULT '0' COMMENT '已读状态（0-未读/1-已读）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_read_status` (`read_status`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- --------------------------------------------------------
-- 创建表: 用户地址表(t_address)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `province` varchar(50) NOT NULL COMMENT '省份',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `district` varchar(50) NOT NULL COMMENT '区县',
  `detail` varchar(255) NOT NULL COMMENT '详细地址',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认地址（0-否/1-是）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_is_default` (`is_default`),
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

-- --------------------------------------------------------
-- 创建表: 群信息表(t_group)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '群ID',
  `group_name` varchar(100) NOT NULL COMMENT '群名称',
  `creator_id` bigint(20) NOT NULL COMMENT '创建者ID',
  `description` varchar(500) DEFAULT NULL COMMENT '群描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`),
  CONSTRAINT `fk_group_creator` FOREIGN KEY (`creator_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群信息表';

-- --------------------------------------------------------
-- 创建表: 群订单表(t_group_order)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_group_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '群订单ID',
  `initiator_id` bigint(20) NOT NULL COMMENT '群订单发起者ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `group_id` bigint(20) NOT NULL COMMENT '群ID',
  `address_id` bigint(20) NOT NULL COMMENT '地址ID',
  `remark` varchar(255) DEFAULT NULL COMMENT '订单备注',
  `status` tinyint(4) DEFAULT '0' COMMENT '订单状态：0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已完成、6-已取消',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  CONSTRAINT `fk_group_order_initiator` FOREIGN KEY (`initiator_id`) REFERENCES `t_user` (`id`),
  CONSTRAINT `fk_group_order_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `t_merchant` (`id`),
  CONSTRAINT `fk_group_order_group` FOREIGN KEY (`group_id`) REFERENCES `t_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群订单表';

-- --------------------------------------------------------
-- 创建表: 群订单菜品表(t_group_order_dish)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_group_order_dish` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `group_order_id` bigint(20) NOT NULL COMMENT '群订单ID',
  `dish_id` bigint(20) NOT NULL COMMENT '菜品ID',
  `quantity` int(11) NOT NULL COMMENT '数量',
  `customization` varchar(255) DEFAULT NULL COMMENT '定制要求',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`),
  KEY `idx_group_order_id` (`group_order_id`),
  KEY `idx_dish_id` (`dish_id`),
  CONSTRAINT `fk_group_order_dish_order` FOREIGN KEY (`group_order_id`) REFERENCES `t_group_order` (`id`),
  CONSTRAINT `fk_group_order_dish_dish` FOREIGN KEY (`dish_id`) REFERENCES `t_dish` (`id`),
  CONSTRAINT `fk_group_order_dish_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群订单菜品表';

-- --------------------------------------------------------
-- 创建表: 联系人关系表(t_contact)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_contact` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '联系人ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `target_id` bigint(20) NOT NULL COMMENT '目标ID（好友ID或群ID）',
  `relation_type` varchar(20) NOT NULL COMMENT '关系类型：friend(好友)、group(群成员)',
  `status` varchar(20) NOT NULL COMMENT '关系状态：normal(正常)、pending(待验证)、blocked(已拉黑)',
  `role` varchar(20) DEFAULT NULL COMMENT '群角色：creator(创建者)、admin(管理员)、member(普通成员)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_target` (`user_id`, `target_id`, `relation_type`),
  KEY `idx_target` (`target_id`, `relation_type`),
  CONSTRAINT `fk_contact_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联系人关系表';

-- --------------------------------------------------------
-- 创建表: 用户评价表(t_review) - 优化：调整字段类型和索引
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `dish_id` bigint DEFAULT NULL COMMENT '菜品ID',
  `rating` tinyint NOT NULL COMMENT '评价星级(1-5)',
  `content` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `images` text COMMENT '评价图片URL列表(JSON格式)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint DEFAULT '0' COMMENT '评价状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_merchant_rating_time` (`merchant_id`, `rating`, `create_time`),
  CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`),
  CONSTRAINT `fk_review_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `t_merchant` (`id`),
  CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`id`),
  CONSTRAINT `fk_review_dish` FOREIGN KEY (`dish_id`) REFERENCES `t_dish` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户评价表';

-- --------------------------------------------------------
-- 创建表: 评价回复表(t_review_reply)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_review_reply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `review_id` bigint NOT NULL COMMENT '评价ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `content` varchar(500) DEFAULT NULL COMMENT '回复内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_review_id` (`review_id`),
  CONSTRAINT `fk_review_reply_review` FOREIGN KEY (`review_id`) REFERENCES `t_review` (`id`),
  CONSTRAINT `fk_review_reply_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `t_merchant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价回复表';

-- --------------------------------------------------------
-- 创建表: 用户收藏表(t_user_collection)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_user_collection` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_id` bigint NOT NULL COMMENT '收藏目标ID（商家/菜品/食谱等）',
  `target_type` varchar(20) NOT NULL COMMENT '收藏类型：merchant/dish/recipe',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`),
  CONSTRAINT `fk_collection_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- --------------------------------------------------------
-- 创建表: 用户消费记录表(t_consume_history)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_consume_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消费记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `amount` decimal(10,2) NOT NULL COMMENT '消费金额',
  `consume_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '消费时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_merchant` (`user_id`, `merchant_id`),
  CONSTRAINT `fk_consume_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`),
  CONSTRAINT `fk_consume_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `t_merchant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户消费记录表';

-- --------------------------------------------------------
-- 数据库创建完成
-- --------------------------------------------------------