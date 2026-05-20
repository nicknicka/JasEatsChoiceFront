# 管理端乱码排查报告

- 数据库：`jia_shi_yi_xuan`
- 典型乱码判定：`å|ç|æ|é|è|ä|ï|ð`

| 表 | 字段 | 说明 | 问题数量 | 示例ID | 原始值 | 修复预览 |
| --- | --- | --- | ---: | --- | --- | --- |
| `t_user` | `nickname` | 用户昵称 | 0 | - | - | - |
| `t_user` | `location` | 用户所在地 | 0 | - | - | - |
| `t_user` | `bio` | 用户简介 | 0 | - | - | - |
| `t_dish` | `name` | 菜品名称 | 0 | - | - | - |
| `t_dish` | `description` | 菜品描述 | 0 | - | - | - |
| `t_dish` | `audit_comment` | 菜品审核意见 | 0 | - | - | - |
| `hot_topic` | `content` | 热点内容 | 0 | - | - | - |
| `hot_topic` | `remark` | 热点备注 | 0 | - | - | - |
| `hot_topic` | `review_comment` | 热点审核意见 | 0 | - | - | - |
| `t_merchant` | `name` | 商家名称 | 0 | - | - | - |
| `t_merchant` | `address` | 商家地址 | 0 | - | - | - |
| `t_merchant` | `contact_name` | 商家联系人 | 0 | - | - | - |
| `t_merchant` | `audit_reason` | 商家审核备注 | 0 | - | - | - |
| `announcement` | `title` | 公告标题 | 0 | - | - | - |
| `announcement` | `content` | 公告内容 | 0 | - | - | - |
| `t_role` | `role_name` | 角色名称 | 0 | - | - | - |
| `t_role` | `description` | 角色描述 | 0 | - | - | - |
| `t_permission` | `permission_name` | 权限名称 | 0 | - | - | - |
| `t_permission` | `description` | 权限描述 | 0 | - | - | - |
| `t_admin` | `real_name` | 管理员姓名 | 0 | - | - | - |
| `t_admin` | `remark` | 管理员备注 | 0 | - | - | - |

## 汇总

- 业务字段命中总数：`0`
- 管理端相关表注释乱码数：`5`
- 管理端相关列注释乱码数：`99`

## 说明

- 该脚本只做排查，不写库。
- 修复预览使用 `latin1 -> utf8mb4` 转换，适用于典型 UTF-8 被错误写入西文编码的历史数据。
