-- 创建聊天消息表
CREATE TABLE `t_chat_msg` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `from_id` varchar(64) DEFAULT NULL COMMENT '发送者ID',
  `to_id` varchar(64) DEFAULT NULL COMMENT '接收者ID',
  `msg_type` varchar(32) DEFAULT NULL COMMENT '消息类型（single/group/order_sync/order_status）',
  `content` text DEFAULT NULL COMMENT '消息内容',
  `read_status` tinyint(1) DEFAULT '0' COMMENT '已读状态：0-未读，1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_id` (`from_id`),
  KEY `idx_to_id` (`to_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';
