package com.xx.jaseatschoicejava.util;

import java.security.SecureRandom;

/**
 * 统一ID生成器
 * 1. 生成16位数字的ID
 * 2. 结合时间戳和随机数，确保ID的唯一性
 * 3. 提供ID类型转换功能
 */
public class IdGenerator {

    // 随机数生成器
    private static final SecureRandom RANDOM = new SecureRandom();

    // ID类型前缀
    public static final String USER_ID_PREFIX = "U";
    public static final String MERCHANT_ID_PREFIX = "M";
    public static final String GROUP_ID_PREFIX = "G";

    // ID长度
    public static final int ID_LENGTH = 16;

    // 时间戳开始时间（2025-01-01 00:00:00）
    private static final long START_TIMESTAMP = 1735689600000L;

    /**
     * 生成16位数字的ID
     * 结构：时间戳偏移量（10位） + 递增序列号（6位）
     * 时间戳部分确保了ID的大致递增顺序
     * 序列号部分确保了在同一毫秒内生成的ID不会重复
     */

    // 上次生成ID的时间戳
    private static long lastTimestamp = -1L;

    // 同一毫秒内的序列号
    private static long sequence = 0L;

    // 序列号的最大值（6位数字）
    private static final long MAX_SEQUENCE = 999999L;

    public static synchronized Long generateId() {
        // 获取当前时间戳
        long currentTimestamp = System.currentTimeMillis() - START_TIMESTAMP;

        // 如果当前时间戳与上次相同
        if (currentTimestamp == lastTimestamp) {
            // 序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;

            // 如果序列号超出范围，等待下一毫秒
            if (sequence == 0) {
                // 循环等待直到时间戳变化
                while (System.currentTimeMillis() - START_TIMESTAMP <= lastTimestamp) {
                    // 空循环
                }
                currentTimestamp = System.currentTimeMillis() - START_TIMESTAMP;
            }
        } else {
            // 如果时间戳不同，重置序列号
            sequence = 0L;
        }

        // 更新上次时间戳
        lastTimestamp = currentTimestamp;

        // 将时间戳转换为字符串，取后10位，确保10位长度
        String timestampStr = String.valueOf(currentTimestamp);
        if (timestampStr.length() > 10) {
            timestampStr = timestampStr.substring(timestampStr.length() - 10);
        }
        // 格式化为10位，不足则补前导0
        timestampStr = String.format("%010d", Long.parseLong(timestampStr));

        // 将序列号转换为6位字符串
        String sequenceStr = String.format("%06d", sequence);

        // 拼接并转换为Long
        return Long.parseLong(timestampStr + sequenceStr);
    }

    /**
     * 将用户ID转换为带U前缀的字符串
     * @param userId 用户ID
     * @return 带U前缀的用户ID字符串
     */
    public static String toUserIdString(Long userId) {
        if (userId == null) {
            return null;
        }
        return USER_ID_PREFIX + userId;
    }

    /**
     * 将商家ID转换为带M前缀的字符串
     * @param merchantId 商家ID
     * @return 带M前缀的商家ID字符串
     */
    public static String toMerchantIdString(Long merchantId) {
        if (merchantId == null) {
            return null;
        }
        return MERCHANT_ID_PREFIX + merchantId;
    }

    /**
     * 将群ID转换为带G前缀的字符串
     * @param groupId 群ID
     * @return 带G前缀的群ID字符串
     */
    public static String toGroupIdString(Long groupId) {
        if (groupId == null) {
            return null;
        }
        return GROUP_ID_PREFIX + groupId;
    }

    /**
     * 从带前缀的ID字符串中提取原始Long类型ID
     * @param prefixedId 带前缀的ID字符串
     * @return 原始Long类型ID
     */
    public static Long toLongId(String prefixedId) {
        if (prefixedId == null || prefixedId.length() < 2) {
            throw new IllegalArgumentException("无效的ID格式: " + prefixedId);
        }
        // 提取除第一位前缀外的部分
        String idPart = prefixedId.substring(1);
        try {
            return Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的ID格式: " + prefixedId, e);
        }
    }

    /**
     * 从带前缀的ID字符串中提取ID类型
     * @param prefixedId 带前缀的ID字符串
     * @return ID类型 (USER/MERCHANT/GROUP)
     */
    public static String extractIdType(String prefixedId) {
        if (prefixedId == null || prefixedId.length() < 1) {
            throw new IllegalArgumentException("无效的ID格式");
        }

        char prefix = prefixedId.charAt(0);
        switch (prefix) {
            case 'U':
                return "USER";
            case 'M':
                return "MERCHANT";
            case 'G':
                return "GROUP";
            default:
                throw new IllegalArgumentException("未知的ID前缀: " + prefix);
        }
    }

    /**
     * 验证带前缀的ID格式是否有效
     * @param prefixedId 带前缀的ID字符串
     * @return 是否是有效的ID
     */
    public static boolean isValidId(String prefixedId) {
        // ID 格式：前缀(1位) + 数字ID(16位)，总共至少17位
        if (prefixedId == null || prefixedId.length() < ID_LENGTH + 1) {
            return false;
        }

        char prefix = prefixedId.charAt(0);
        if (prefix != 'U' && prefix != 'M' && prefix != 'G') {
            return false;
        }

        // 验证剩下的部分是否都是数字
        for (int i = 1; i < prefixedId.length(); i++) {
            if (!Character.isDigit(prefixedId.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
