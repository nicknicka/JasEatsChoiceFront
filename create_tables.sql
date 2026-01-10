-- 创建消息记录表
CREATE TABLE IF NOT EXISTS `t_message_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sender_id` bigint DEFAULT NULL COMMENT '发送者ID',
  `receiver_id` bigint DEFAULT NULL COMMENT '接收者ID',
  `content` text COMMENT '消息内容',
  `message_type` varchar(20) DEFAULT 'text' COMMENT '消息类型 (text: 文本, image: 图片, file: 文件)',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `read_status` tinyint DEFAULT '0' COMMENT '阅读状态 (0: 未读, 1: 已读)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_read_status` (`read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息记录表';
