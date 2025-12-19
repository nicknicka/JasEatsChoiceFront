-- 为用户表添加头像字段
ALTER TABLE t_user
ADD COLUMN avatar VARCHAR(255) NULL COMMENT '用户头像URL' AFTER update_time;
