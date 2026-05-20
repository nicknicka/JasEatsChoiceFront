-- MySQL dump 10.13  Distrib 9.4.0, for macos15.4 (arm64)
--
-- Host: localhost    Database: jia_shi_yi_xuan
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `t_user`
--

DROP TABLE IF EXISTS `t_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user` (
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `phone` varchar(11) NOT NULL COMMENT '手机号',
  `password` varchar(64) NOT NULL COMMENT '密码（BCrypt加密）',
  `payment_password` varchar(255) DEFAULT NULL COMMENT 'æ”¯ä»˜å¯†ç ï¼ˆåŠ å¯†å­˜å‚¨ï¼‰',
  `has_payment_password` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦å·²è®¾ç½®æ”¯ä»˜å¯†ç ï¼š0-æœªè®¾ç½®ï¼Œ1-å·²è®¾ç½®',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `height` decimal(5,1) DEFAULT NULL COMMENT '身高(cm)',
  `weight` decimal(5,1) DEFAULT NULL COMMENT '体重(kg)',
  `diet_goal` varchar(20) DEFAULT NULL COMMENT '饮食目标（lose_weight/keep_fit/gain_weight）',
  `allergies` json DEFAULT NULL COMMENT '过敏食材列表',
  `prefer_tags` json DEFAULT NULL COMMENT '饮食偏好标签（JSON格式）',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱地址',
  `disable_weather_recommend` tinyint(1) DEFAULT '0' COMMENT '是否关闭天气推荐',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `avatar` varchar(255) DEFAULT NULL COMMENT '用户头像URL',
  `merchant_id` varchar(64) DEFAULT NULL COMMENT '商家ID',
  `location` varchar(50) DEFAULT NULL COMMENT 'ç”¨æˆ·æ‰€åœ¨åœ°',
  `gender` varchar(10) DEFAULT NULL COMMENT 'æ€§åˆ«ï¼šmale-ç”·ï¼Œfemale-å¥³ï¼Œother-å…¶ä»–',
  `birthday` varchar(20) DEFAULT NULL COMMENT 'ç”Ÿæ—¥',
  `bio` text COMMENT 'ä¸ªäººç®€ä»‹',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user`
--

LOCK TABLES `t_user` WRITE;
/*!40000 ALTER TABLE `t_user` DISABLE KEYS */;
INSERT INTO `t_user` VALUES ('1000000000000000','13800138000','$2a$10$H.WwxnoRrbx1LztA2ocrQOixxGss4ZFRHHuM5Z1NLJJxC9eoDu0Cu',NULL,0,'张三',NULL,NULL,NULL,NULL,NULL,NULL,0,'2025-12-20 20:30:30','2025-12-20 20:37:42',NULL,NULL,NULL,NULL,NULL,NULL),('1000000000000001','13800138001','$2a$10$H.WwxnoRrbx1LztA2ocrQOixxGss4ZFRHHuM5Z1NLJJxC9eoDu0Cu',NULL,0,'李四',NULL,NULL,NULL,NULL,NULL,NULL,0,'2025-12-20 20:30:30','2025-12-20 20:37:42',NULL,NULL,NULL,NULL,NULL,NULL),('1000000000000002','17322222221','$2a$10$H.WwxnoRrbx1LztA2ocrQOixxGss4ZFRHHuM5Z1NLJJxC9eoDu0Cu',NULL,0,'小明',170.0,60.0,'keep_fit','[]','[\"ç´ é£Ÿ\", \"è½»é£Ÿ\", \"å¥åº·é¤\"]',NULL,0,'2025-12-20 20:31:50','2026-05-04 12:30:51',NULL,NULL,NULL,NULL,NULL,NULL),('1000000000000003','17322222223','$2a$10$H.WwxnoRrbx1LztA2ocrQOixxGss4ZFRHHuM5Z1NLJJxC9eoDu0Cu',NULL,0,'小红',160.0,48.0,'lose_weight','[\"æµ·é²œ\"]','[\"ä½Žå¡\", \"å‡è„‚é¤\", \"æ²™æ‹‰\"]',NULL,0,'2025-12-20 20:31:50','2026-05-04 12:30:51',NULL,NULL,NULL,NULL,NULL,NULL),('1000000000000004','17322222224','$2a$10$H.WwxnoRrbx1LztA2ocrQOixxGss4ZFRHHuM5Z1NLJJxC9eoDu0Cu',NULL,0,'小刚',185.0,90.0,'gain_weight','[]','[\"è¥¿é¤\", \"ç‰›æŽ’\", \"æ±‰å ¡\"]',NULL,1,'2025-12-20 20:31:50','2026-05-04 12:30:51',NULL,NULL,NULL,NULL,NULL,NULL),('3384650106421960','17322222222','$2a$10$DgHzNsybwnstPavJi79oxufEcDXE8IgLokzpawuo2u.F.Wm0BYtti',NULL,0,'asd',30.0,5.0,'保持健康',NULL,'{\"dietType\": \"素食\", \"allergies\": [\"鸡蛋\", \"牛奶\"], \"priceRange\": \"中\", \"nutritionNeeds\": \"低卡\"}','test@qq.com',0,'2025-12-19 10:44:14','2025-12-19 10:44:14','/api/uploads/3384650106421960/3f3346f8-bceb-4c53-9f73-e4868d7b4e4d.png','7638432224340229','广东省 佛山市 三水区',NULL,NULL,NULL),('4590322501655448','13822222222','$2a$10$zTOYTi1VHq9cgjXM/kgFTuJShygJ3AVNrcV2mDXXaFRvQpzTyH/5i',NULL,0,'nick',NULL,NULL,NULL,NULL,NULL,'asd@qq.com',0,'2025-12-14 21:08:49','2025-12-26 13:25:53','/api/uploads/4590322501655448/4b88c01d-3ad5-464b-85e8-b67bfcd1ae79.png',NULL,NULL,NULL,NULL,NULL),('8686348687636632','13955550017','$2a$10$G/QkAAOpf2G2unZdqY8wNebWWPm4wyu9ClZ78LBhHq.vhJAt6Aye.',NULL,0,'冒烟测试用户',NULL,NULL,NULL,NULL,NULL,'smoke-user-20260417@example.com',0,'2026-04-17 15:54:31','2026-04-17 15:54:31',NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `t_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_dish`
--

DROP TABLE IF EXISTS `t_dish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_dish` (
  `id` varchar(64) NOT NULL COMMENT '菜品ID',
  `merchant_id` varchar(64) NOT NULL COMMENT '商家ID',
  `name` varchar(100) NOT NULL COMMENT '菜品名称',
  `category` varchar(50) NOT NULL DEFAULT 'å…¶ä»–' COMMENT 'èœå“åˆ†ç±»',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `calorie` int NOT NULL COMMENT '卡路里',
  `estimated_cooking_minutes` int DEFAULT '15' COMMENT 'é¢„ä¼°çƒ¹é¥ªæ—¶é•¿ï¼ˆåˆ†é’Ÿï¼‰',
  `step_template` varchar(20) DEFAULT 'NORMAL' COMMENT 'çƒ¹é¥ªæµç¨‹æ¨¡æ¿ï¼šNORMAL-æ­£é¤æµç¨‹ï¼ŒFAST-å¿«é¤æµç¨‹ï¼ŒCUSTOM-è‡ªå®šä¹‰æµç¨‹',
  `ingredients` json DEFAULT NULL COMMENT '食材列表（JSON格式）',
  `description` text COMMENT '菜品描述',
  `cooking_steps` text COMMENT 'çƒ¹é¥ªæ­¥éª¤ï¼ˆJSONæ ¼å¼ï¼‰',
  `nutrition` json DEFAULT NULL COMMENT 'è¥å…»ä¿¡æ¯ï¼ˆJSONæ ¼å¼ï¼‰',
  `image` varchar(500) DEFAULT NULL COMMENT 'èœå“å›¾ç‰‡URL',
  `score` decimal(5,2) DEFAULT NULL COMMENT 'æŽ¨èå¾—åˆ†',
  `is_online` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦ä¸Šæž¶ï¼š1-ä¸Šæž¶ï¼Œ0-ä¸‹æž¶',
  `audit_status` varchar(20) DEFAULT 'PENDING' COMMENT 'å®¡æ ¸çŠ¶æ€ï¼šPENDING-å¾…å®¡æ ¸, APPROVED-å·²é€šè¿‡, REJECTED-å·²æ‹’ç»',
  `audit_comment` varchar(500) DEFAULT NULL COMMENT 'å®¡æ ¸æ„è§',
  `audit_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ—¶é—´',
  `audit_admin_id` bigint DEFAULT NULL COMMENT 'å®¡æ ¸ç®¡ç†å‘˜ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `view_count` int DEFAULT '0' COMMENT 'æµè§ˆæ¬¡æ•°',
  `order_count` int DEFAULT '0' COMMENT 'ä¸‹å•æ¬¡æ•°',
  `favorite_count` int DEFAULT '0' COMMENT 'æ”¶è—æ¬¡æ•°',
  `avg_rating` decimal(3,2) DEFAULT NULL COMMENT 'å¹³å‡è¯„åˆ†',
  `tags` json DEFAULT NULL COMMENT 'æ ‡ç­¾æ•°ç»„',
  `stock` int DEFAULT '-1' COMMENT 'åº“å­˜æ•°é‡ï¼š-1è¡¨ç¤ºä¸é™é‡ï¼Œ>=0è¡¨ç¤ºå®žé™…åº“å­˜',
  `is_fast_food_virtual` tinyint(1) GENERATED ALWAYS AS ((case when (`step_template` = _utf8mb4'FAST') then 1 else 0 end)) VIRTUAL COMMENT 'æ˜¯å¦ä¸ºå¿«é¤ï¼ˆè™šæ‹Ÿå­—æ®µï¼Œè‡ªåŠ¨è®¡ç®—ï¼‰',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_category` (`category`),
  KEY `idx_calorie` (`calorie`),
  KEY `idx_view_count` (`view_count` DESC),
  KEY `idx_order_count` (`order_count` DESC),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_merchant_audit_time` (`merchant_id`,`audit_status`,`create_time`),
  KEY `idx_category_online_rating` (`category`,`is_online`,`avg_rating`),
  KEY `idx_view_order_count` (`view_count` DESC,`order_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_dish`
--

LOCK TABLES `t_dish` WRITE;
/*!40000 ALTER TABLE `t_dish` DISABLE KEYS */;
INSERT INTO `t_dish` (`id`, `merchant_id`, `name`, `category`, `price`, `calorie`, `estimated_cooking_minutes`, `step_template`, `ingredients`, `description`, `cooking_steps`, `nutrition`, `image`, `score`, `is_online`, `audit_status`, `audit_comment`, `audit_time`, `audit_admin_id`, `create_time`, `update_time`, `view_count`, `order_count`, `favorite_count`, `avg_rating`, `tags`, `stock`) VALUES ('1','7638432224340229','宫保鸡丁','分类1',38.00,450,15,'NORMAL','{\"optional\": [\"11\", \"1\", \"111\", \"2\"], \"mandatory\": [\"12\", \"3\", \"2\", \"1\"]}','经典川菜',NULL,NULL,'https://via.placeholder.com/400x300/FF6B35/FFFFFF?text=宫保鸡丁',NULL,1,'APPROVED','系统默认通过（历史数据）','2026-01-31 20:40:30',NULL,'2025-11-22 11:35:20','2026-05-04 12:30:51',0,0,0,NULL,NULL,0),('1001','7638432224340229','皮蛋瘦肉粥','早餐',12.00,180,15,'NORMAL',NULL,'asd',NULL,NULL,'https://via.placeholder.com/400x300/4CAF50/FFFFFF?text=皮蛋瘦肉粥',NULL,1,'APPROVED','系统默认通过（历史数据）','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-05-04 12:30:51',0,0,0,NULL,NULL,100);
/*!40000 ALTER TABLE `t_dish` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hot_topic`
--

DROP TABLE IF EXISTS `hot_topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hot_topic` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'çƒ­ç‚¹å†…å®¹',
  `priority` int DEFAULT '0' COMMENT 'ä¼˜å…ˆçº§',
  `source_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'MANUAL' COMMENT 'æ¥æºç±»åž‹',
  `source_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ¥æºID',
  `start_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'ç”Ÿæ•ˆå¼€å§‹æ—¶é—´',
  `end_date` datetime DEFAULT NULL COMMENT 'ç”Ÿæ•ˆç»“æŸæ—¶é—´',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE' COMMENT 'çŠ¶æ€',
  `click_count` int DEFAULT '0' COMMENT 'ç‚¹å‡»æ¬¡æ•°',
  `share_count` int DEFAULT '0' COMMENT 'åˆ†äº«æ¬¡æ•°',
  `require_review` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦éœ€è¦å®¡æ ¸',
  `review_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'APPROVED' COMMENT 'å®¡æ ¸çŠ¶æ€',
  `reviewer_id` bigint DEFAULT NULL COMMENT 'å®¡æ ¸äººID',
  `review_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ—¶é—´',
  `review_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å®¡æ ¸æ„è§',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å¤‡æ³¨ä¿¡æ¯',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `create_by` bigint DEFAULT NULL COMMENT 'åˆ›å»ºäººID',
  `update_by` bigint DEFAULT NULL COMMENT 'æ›´æ–°äººID',
  PRIMARY KEY (`id`),
  KEY `idx_start_date` (`start_date`),
  KEY `idx_end_date` (`end_date`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`),
  KEY `idx_source_type` (`source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ä»Šæ—¥çƒ­ç‚¹è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hot_topic`
--

LOCK TABLES `hot_topic` WRITE;
/*!40000 ALTER TABLE `hot_topic` DISABLE KEYS */;
INSERT INTO `hot_topic` VALUES ('1','test',10,'MANUAL',NULL,'2026-02-06 00:00:00','2026-03-03 00:00:00','ACTIVE',3,0,0,'APPROVED',NULL,NULL,NULL,'test','2026-01-29 10:33:50','2026-02-11 18:37:35',NULL,NULL),('2','asd',5,'MANUAL',NULL,'2026-01-29 10:33:50',NULL,'INACTIVE',0,0,0,'APPROVED',NULL,NULL,NULL,'早餐推荐','2026-01-29 10:33:50','2026-05-04 12:30:51',NULL,NULL),('3','888999',3,'MANUAL',NULL,'2026-01-29 10:33:50',NULL,'ACTIVE',1,0,0,'APPROVED',NULL,NULL,NULL,'减脂期饮食推荐','2026-01-29 10:33:50','2026-05-04 12:30:51',NULL,NULL);
/*!40000 ALTER TABLE `hot_topic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_merchant`
--

DROP TABLE IF EXISTS `t_merchant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_merchant` (
  `id` varchar(64) NOT NULL COMMENT '商家ID',
  `name` varchar(100) NOT NULL COMMENT '商家名称',
  `address` varchar(255) DEFAULT NULL,
  `longitude` decimal(10,6) DEFAULT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `business_hours` json DEFAULT NULL,
  `average_price` decimal(10,2) DEFAULT NULL COMMENT '人均消费',
  `status` tinyint(1) DEFAULT '1' COMMENT '商家状态（0-关闭/1-营业）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `business_license` varchar(255) DEFAULT NULL COMMENT '营业执照',
  `business_scope` json DEFAULT NULL,
  `contact_name` varchar(50) DEFAULT NULL COMMENT '联系人姓名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '商家头像',
  `album` json DEFAULT NULL COMMENT '商家相册',
  `rating` decimal(3,2) DEFAULT '0.00',
  `audit_status` varchar(20) DEFAULT 'PENDING' COMMENT 'å®¡æ ¸çŠ¶æ€ï¼šPENDING-å¾…å®¡æ ¸, APPROVED-å·²é€šè¿‡, REJECTED-å·²æ‹’ç»',
  `audit_reason` varchar(500) DEFAULT NULL COMMENT 'å®¡æ ¸å¤‡æ³¨/åŽŸå› ',
  `audit_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ—¶é—´',
  `audit_by` varchar(50) DEFAULT NULL COMMENT 'å®¡æ ¸äººID',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_location` (`longitude`,`latitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_merchant`
--

LOCK TABLES `t_merchant` WRITE;
/*!40000 ALTER TABLE `t_merchant` DISABLE KEYS */;
INSERT INTO `t_merchant` VALUES ('7638432224340229','test','广东省/佛山市/三水区/进港大道1号',112.851080,23.045220,'1、2、3、33、4、5','19233333333','{\"end\": \"21:00\", \"start\": \"11:00\"}',10.00,1,'2025-12-19 15:13:59','2026-03-25 09:48:18','$2a$10$OsR4FjpVRzUjm6NI1O76ROe0a3MS/8GTPQCpzAJVsJyB23IKRiqnS','test@qq.com','asd','[\"快餐\", \"饮品\"]','asd','/uploads/avatar-7638432224340229-1775647486090.png','{\"dishes\": [], \"environment\": [\"http://localhost:8080/api/uploads/1769588015975-a8a81a36-d130-4a31-9db7-e08a202f7d70.png\"]}',4.80,'PENDING',NULL,NULL,NULL);
/*!40000 ALTER TABLE `t_merchant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `announcement`
--

DROP TABLE IF EXISTS `announcement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'å…¬å‘ŠID',
  `merchant_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å•†å®¶IDï¼ŒNULLè¡¨ç¤ºç³»ç»Ÿå…¬å‘Š',
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å…¬å‘Šæ ‡é¢˜',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å…¬å‘Šå†…å®¹',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'system' COMMENT 'å…¬å‘Šç±»åž‹ï¼šsystem-ç³»ç»Ÿå…¬å‘Š, activity-æ´»åŠ¨å…¬å‘Š, urgent-ç´§æ€¥å…¬å‘Š, update-æ›´æ–°è¯´æ˜Ž',
  `priority` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'normal' COMMENT 'ä¼˜å…ˆçº§ï¼šnormal-æ™®é€š, important-é‡è¦, urgent-ç´§æ€¥',
  `target_audience` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'all' COMMENT 'ç›®æ ‡ç¾¤ä½“ï¼šall-å…¨éƒ¨ç”¨æˆ·, merchant-å•†å®¶ç«¯, customer-ç”¨æˆ·ç«¯',
  `read_count` bigint DEFAULT '0' COMMENT 'é˜…è¯»é‡',
  `read_users` bigint DEFAULT '0' COMMENT 'é˜…è¯»äººæ•°',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT 'å…¬å‘ŠçŠ¶æ€ (active: å¯ç”¨, inactive: ç¦ç”¨)',
  `start_time` datetime DEFAULT NULL COMMENT 'å¼€å§‹æ—¶é—´',
  `end_time` datetime DEFAULT NULL COMMENT 'ç»“æŸæ—¶é—´',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2023710121550073858 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='å•†å®¶å…¬å‘Šè¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcement`
--

LOCK TABLES `announcement` WRITE;
/*!40000 ALTER TABLE `announcement` DISABLE KEYS */;
INSERT INTO `announcement` VALUES (1,NULL,'测试公告','这是一个测试公告','system','normal','all',0,0,'active','2026-01-11 06:06:52',NULL,'2026-01-11 10:52:54','2026-02-09 23:24:03'),(2,NULL,'31299','test asd 123 ','system','normal','all',0,0,'inactive','2026-01-04 18:04:00','2026-01-11 06:00:19','2026-01-11 14:00:52','2026-02-09 23:24:03'),(4,NULL,'312','test asd 123 312ws123','system','normal','all',0,0,'inactive','2026-02-20 14:06:03','2026-02-13 00:00:00','2026-01-11 14:01:07','2026-02-09 23:24:03'),(2023710121550073857,'7638432224340229','asd','asdasd','system','normal','all',0,0,'inactive','2026-02-11 08:21:00','2026-02-19 08:00:00','2026-02-17 18:44:26','2026-02-17 18:44:26');
/*!40000 ALTER TABLE `announcement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_role`
--

DROP TABLE IF EXISTS `t_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'è§’è‰²ID',
  `role_name` varchar(50) NOT NULL COMMENT 'è§’è‰²åç§°',
  `role_code` varchar(20) NOT NULL COMMENT 'è§’è‰²ç¼–ç ï¼šSUPER_ADMIN-è¶…çº§ç®¡ç†å‘˜, ADMIN-æ™®é€šç®¡ç†å‘˜, AUDITOR-å®¡æ ¸å‘˜',
  `description` varchar(255) DEFAULT NULL COMMENT 'è§’è‰²æè¿°',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'çŠ¶æ€ï¼šACTIVE-å¯ç”¨, DISABLED-ç¦ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åºåºå·',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `role_code` (`role_code`),
  KEY `idx_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è§’è‰²è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_role`
--

LOCK TABLES `t_role` WRITE;
/*!40000 ALTER TABLE `t_role` DISABLE KEYS */;
INSERT INTO `t_role` VALUES (1,'超级管理员','SUPER_ADMIN','拥有系统所有权限，不受权限系统限制','ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51',1),(2,'普通管理员','ADMIN','拥有大部分管理权限，但不能管理系统配置和角色权限','ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51',2),(3,'审核员','AUDITOR','只能进行内容审核操作','ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51',3);
/*!40000 ALTER TABLE `t_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_permission`
--

DROP TABLE IF EXISTS `t_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_permission` (
  `permission_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'æƒé™ID',
  `permission_name` varchar(100) NOT NULL COMMENT 'æƒé™åç§°',
  `permission_code` varchar(50) NOT NULL COMMENT 'æƒé™ç¼–ç ',
  `resource_type` varchar(20) NOT NULL COMMENT 'èµ„æºç±»åž‹ï¼šMENU-èœå•, BUTTON-æŒ‰é’®, API-æŽ¥å£',
  `parent_id` bigint DEFAULT '0' COMMENT 'çˆ¶æƒé™IDï¼ˆ0è¡¨ç¤ºé¡¶çº§æƒé™ï¼‰',
  `path` varchar(100) DEFAULT NULL COMMENT 'è·¯ç”±è·¯å¾„ï¼ˆèœå•ç±»åž‹ä½¿ç”¨ï¼‰',
  `icon` varchar(50) DEFAULT NULL COMMENT 'å›¾æ ‡',
  `description` varchar(200) DEFAULT NULL COMMENT 'æƒé™æè¿°',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åºåºå·',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'çŠ¶æ€ï¼šACTIVE-å¯ç”¨, DISABLED-ç¦ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`permission_id`),
  UNIQUE KEY `permission_code` (`permission_code`),
  KEY `idx_permission_code` (`permission_code`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=88 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æƒé™è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_permission`
--

LOCK TABLES `t_permission` WRITE;
/*!40000 ALTER TABLE `t_permission` DISABLE KEYS */;
INSERT INTO `t_permission` VALUES (1,'控制台','admin:dashboard','MENU',0,'/admin/dashboard',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(2,'用户管理','admin:user','MENU',0,'/admin/users',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(3,'商家管理','admin:merchant','MENU',0,'/admin/merchants',NULL,NULL,3,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(4,'订单管理','admin:order','MENU',0,'/admin/orders',NULL,NULL,4,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(5,'菜品管理','admin:dish','MENU',0,'/admin/dishes',NULL,NULL,5,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(6,'教程管理','admin:tutorial','MENU',0,'/admin/tutorials',NULL,NULL,6,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(7,'内容管理','admin:content','MENU',0,'/admin/content',NULL,NULL,7,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(8,'财务管理','admin:finance','MENU',0,'/admin/finance',NULL,NULL,8,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(9,'系统设置','admin:setting','MENU',0,'/admin/settings',NULL,NULL,9,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(10,'数据统计','admin:statistics','MENU',0,'/admin/statistics',NULL,NULL,10,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(11,'用户列表','admin:user:list','API',2,'/api/admin/users',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(12,'用户详情','admin:user:detail','API',2,'/api/admin/users/*',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(13,'修改用户状态','admin:user:status','API',2,'/api/admin/users/*/status',NULL,NULL,3,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(14,'删除用户','admin:user:delete','API',2,'/api/admin/users/*',NULL,NULL,4,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(15,'商家列表','admin:merchant:list','API',3,'/api/admin/merchants',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(16,'审核商家','admin:merchant:audit','API',3,'/api/admin/merchants/*/audit',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(17,'商家状态','admin:merchant:status','API',3,'/api/admin/merchants/*/status',NULL,NULL,3,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(18,'订单列表','admin:order:list','API',4,'/api/admin/orders',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(19,'订单详情','admin:order:detail','API',4,'/api/admin/orders/*',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(20,'订单状态','admin:order:status','API',4,'/api/admin/orders/*/status',NULL,NULL,3,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(21,'菜品列表','admin:dish:list','API',5,'/api/admin/dishes',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(22,'审核菜品','admin:dish:audit','API',5,'/api/admin/dishes/*/audit',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(23,'教程列表','admin:tutorial:list','API',6,'/api/admin/tutorials',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(24,'教程审核','admin:tutorial:audit','API',6,'/api/admin/tutorials/*/audit',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(25,'教程删除','admin:tutorial:delete','API',6,'/api/admin/tutorials/*',NULL,NULL,3,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(26,'热点话题列表','admin:topic:list','API',7,'/api/admin/topics',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(27,'热点话题审核','admin:topic:audit','API',7,'/api/admin/topics/*/audit',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(28,'公告列表','admin:announcement:list','API',7,'/api/admin/announcements',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(29,'公告发布','admin:announcement:create','API',7,'/api/admin/announcements',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(30,'提现审核','admin:finance:withdrawal','API',8,'/api/admin/finance/withdrawals',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(31,'充值记录','admin:finance:recharge','API',8,'/api/admin/finance/recharges',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(32,'退款管理','admin:finance:refund','API',8,'/api/admin/finance/refunds',NULL,NULL,3,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(33,'角色管理','admin:setting:role','MENU',9,'/admin/settings/roles',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(34,'角色列表','admin:setting:role:list','API',9,'/admin/roles',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(35,'角色创建','admin:setting:role:create','API',9,'/admin/roles',NULL,NULL,3,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(36,'权限分配','admin:setting:permission','API',9,'/api/admin/settings/permissions',NULL,NULL,4,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(37,'系统日志','admin:setting:log','MENU',9,'/admin/settings/logs',NULL,NULL,5,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(38,'日志查看','admin:setting:log:view','API',9,'/api/admin/settings/logs',NULL,NULL,6,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(39,'统计数据','admin:statistics:view','API',10,'/api/admin/statistics',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-05-04 12:30:51'),(40,'用户管理','user:manage','MENU',0,'/admin/users','User','test',1,'ACTIVE','2026-02-01 14:15:23','2026-05-04 12:30:51'),(41,'编辑用户','admin:user:edit','API',1,NULL,NULL,'编辑用户',3,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(42,'商家管理','merchant:manage','MENU',0,'/admin/merchants','Shop','商家管理',2,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(43,'商家详情','admin:merchant:detail','API',6,NULL,NULL,'查看商家详情',2,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(44,'订单管理','order:manage','MENU',0,'/admin/orders','Document','订单管理',3,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(45,'订单统计','admin:order:view','API',11,NULL,NULL,'查看订单统计',4,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(46,'菜品管理','dish:manage','MENU',0,'/admin/dishes','Food','菜品管理',4,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(47,'菜品详情','admin:dish:detail','API',16,NULL,NULL,'查看菜品详情',2,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(48,'财务管理','finance:manage','MENU',0,'/admin/finance','Money','财务管理',5,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(49,'财务统计','admin:finance:statistics','API',21,NULL,NULL,'查看财务统计',3,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(50,'系统管理','system:manage','MENU',0,'/admin/system','Setting','系统管理',6,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(51,'系统日志','admin:system:logs','API',26,NULL,NULL,'查看系统日志',1,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(52,'清理日志','admin:system:logs:clean','API',26,NULL,NULL,'清理日志',2,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(53,'导出日志','admin:system:logs:export','API',26,NULL,NULL,'导出日志',3,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(54,'角色权限','role:manage','MENU',0,'/admin/roles','Lock','角色权限',7,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(55,'角色列表','admin:role:list','API',31,NULL,NULL,'查看角色列表',1,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(56,'角色详情','admin:role:detail','API',31,NULL,NULL,'查看角色详情',2,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(57,'创建角色','admin:role:create','API',31,NULL,NULL,'创建角色',3,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(58,'更新角色','admin:role:update','API',31,NULL,NULL,'更新角色',4,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(59,'删除角色','admin:role:delete','API',31,'','','asd\n',5,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(60,'分配权限','admin:role:assign','API',31,NULL,NULL,'分配权限',6,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(61,'权限列表','admin:permission:list','API',31,NULL,NULL,'查看权限列表',7,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(62,'权限详情','admin:permission:detail','API',31,NULL,NULL,'查看权限详情',8,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(63,'创建权限','admin:permission:create','API',31,NULL,NULL,'创建权限',9,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(64,'更新权限','admin:permission:update','API',31,NULL,NULL,'更新权限',10,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(65,'删除权限','admin:permission:delete','API',31,NULL,NULL,'删除权限',11,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(66,'数据统计','statistics:manage','MENU',0,'/admin/statistics','DataLine','数据统计',8,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(67,'仪表盘','admin:statistics:dashboard','API',44,NULL,NULL,'查看仪表盘',1,'ACTIVE','2026-02-01 14:22:25','2026-05-04 12:30:51'),(82,'提现审核','admin:finance:withdrawals','API',8,'/api/admin/withdrawals',NULL,NULL,1,'ACTIVE','2026-02-10 00:15:53','2026-05-04 12:30:51'),(83,'充值记录管理','admin:finance:recharges','API',8,'/api/admin/finance/recharges',NULL,NULL,2,'ACTIVE','2026-02-10 00:23:40','2026-05-04 12:30:51'),(84,'退款管理','admin:finance:refunds','API',8,'/api/admin/finance/refunds',NULL,NULL,3,'ACTIVE','2026-02-10 00:23:40','2026-05-04 12:30:51'),(85,'系统配置','admin:setting:config','API',9,'/api/admin/settings/config',NULL,'系统配置管理权限',7,'ACTIVE','2026-02-10 19:29:50','2026-05-04 12:30:51'),(87,'超级权限','admin:super','API',9,'/api/admin/super',NULL,'拥有所有权限的超级权限',0,'ACTIVE','2026-02-10 20:08:29','2026-05-04 12:30:51');
/*!40000 ALTER TABLE `t_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_admin`
--

DROP TABLE IF EXISTS `t_admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_admin` (
  `admin_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ç®¡ç†å‘˜ID',
  `username` varchar(50) NOT NULL COMMENT 'ç®¡ç†å‘˜ç”¨æˆ·å',
  `password` varchar(64) NOT NULL COMMENT 'å¯†ç ï¼ˆåŠ å¯†ï¼‰',
  `real_name` varchar(50) DEFAULT NULL COMMENT 'çœŸå®žå§“å',
  `phone` varchar(11) DEFAULT NULL COMMENT 'æ‰‹æœºå·',
  `email` varchar(100) DEFAULT NULL COMMENT 'é‚®ç®±',
  `avatar` varchar(255) DEFAULT NULL COMMENT 'å¤´åƒURL',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'çŠ¶æ€ï¼šACTIVE-æ´»è·ƒ, LOCKED-é”å®š, DELETED-åˆ é™¤',
  `role_id` bigint DEFAULT NULL COMMENT 'è§’è‰²IDï¼ˆå…³è”t_roleè¡¨ï¼‰',
  `last_login_time` datetime DEFAULT NULL COMMENT 'æœ€åŽç™»å½•æ—¶é—´',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT 'æœ€åŽç™»å½•IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `create_by` bigint DEFAULT NULL COMMENT 'åˆ›å»ºäººID',
  `update_by` bigint DEFAULT NULL COMMENT 'æ›´æ–°äººID',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤‡æ³¨',
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_status` (`status`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç®¡ç†å‘˜è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_admin`
--

LOCK TABLES `t_admin` WRITE;
/*!40000 ALTER TABLE `t_admin` DISABLE KEYS */;
INSERT INTO `t_admin` VALUES (1,'admin','$2a$10$wCbmy/lSuxrVSlArXI1TA.l5Uxhvhj7gn/5xf/Q85XuEU29PM4x0O','asdfghj','13822223333','asd@qq.com','/api/uploads/admin_1/1893e6d0-113e-49ff-bfda-526c15c8fab6.png','ACTIVE',1,'2026-05-04 12:25:19',NULL,'2026-01-31 11:39:20','2026-01-31 16:42:23',NULL,NULL,NULL);
/*!40000 ALTER TABLE `t_admin` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-04 12:30:51
