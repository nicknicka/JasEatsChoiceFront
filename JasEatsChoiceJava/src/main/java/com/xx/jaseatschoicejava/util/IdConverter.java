package com.xx.jaseatschoicejava.util;

/**
 * ID转换器
 * 用于在数据库存储的纯数字ID和后端使用的带前缀ID之间进行转换
 */
public class IdConverter {

    // ID类型前缀
    public static final String USER_ID_PREFIX = "U";
    public static final String MERCHANT_ID_PREFIX = "M";
    public static final String GROUP_ID_PREFIX = "G";

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
        if (prefixedId == null || prefixedId.length() < 2) {
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
