-- 为用户表添加头像字段
ALTER TABLE t_user
ADD COLUMN avatar VARCHAR(255) NULL COMMENT '用户头像URL' AFTER update_time;

-- 为商家表确保头像字段存在
ALTER TABLE t_merchant
ADD COLUMN avatar VARCHAR(255) NULL COMMENT '商家头像URL' AFTER update_time;
