package com.xx.jaseatschoicejava.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 统一ID生成器
 * 1. 生成16位数字的ID
 * 2. 使用SHA-256哈希算法结合时间戳和随机数
 * 3. 生成后进行随机打乱以确保无序性
 * 4. 提供ID类型转换功能
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

    // 哈希算法
    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * 生成16位数字的ID
     * 流程：
     * 1. 组合当前时间戳和随机数
     * 2. 使用SHA-256哈希算法生成哈希值
     * 3. 从哈希值中提取数字部分
     * 4. 截取16位数字
     * 5. 对16位数字进行随机打乱
     * 6. 返回最终的16位数字ID
     */
    public static synchronized Long generateId() {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);

            // 1. 生成组合字符串：时间戳 + 随机数
            String input = System.currentTimeMillis() + "-" + RANDOM.nextLong();

            // 2. 计算哈希值
            byte[] hashBytes = digest.digest(input.getBytes());

            // 3. 从哈希值中提取数字部分
            StringBuilder digitsBuilder = new StringBuilder();
            for (byte b : hashBytes) {
                // 将字节转换为无符号整数并取后两位
                String byteStr = String.format("%02X", b & 0xFF);
                for (char c : byteStr.toCharArray()) {
                    if (Character.isDigit(c)) {
                        digitsBuilder.append(c);
                        if (digitsBuilder.length() >= ID_LENGTH * 2) { // 获取足够多的数字
                            break;
                        }
                    }
                }
                if (digitsBuilder.length() >= ID_LENGTH * 2) {
                    break;
                }
            }

            // 4. 确保至少有16位数字，如果不够则继续循环
            while (digitsBuilder.length() < ID_LENGTH) {
                // 生成更多数字
                input = System.currentTimeMillis() + "-" + RANDOM.nextLong() + "-" + RANDOM.nextLong();
                hashBytes = digest.digest(input.getBytes());

                for (byte b : hashBytes) {
                    String byteStr = String.format("%02X", b & 0xFF);
                    for (char c : byteStr.toCharArray()) {
                        if (Character.isDigit(c)) {
                            digitsBuilder.append(c);
                            if (digitsBuilder.length() >= ID_LENGTH) {
                                break;
                            }
                        }
                    }
                    if (digitsBuilder.length() >= ID_LENGTH) {
                        break;
                    }
                }
            }

            // 截取16位数字
            String digits = digitsBuilder.substring(0, ID_LENGTH);

            // 5. 随机打乱字符串
            String shuffled = shuffleString(digits);

            // 6. 转换为Long并返回
            return Long.parseLong(shuffled);

        } catch (NoSuchAlgorithmException e) {
            // 如果SHA-256不可用，回退到基于时间戳的方式
            e.printStackTrace();
            // 回退实现，确保系统可用性
            long timestamp = System.currentTimeMillis();
            long random = RANDOM.nextLong() % 10000000000000000L;
            return Math.abs(timestamp * 10000000000L + random % 10000000000L);
        }
    }

    /**
     * 随机打乱字符串
     * @param str 要打乱的字符串
     * @return 打乱后的字符串
     */
    private static String shuffleString(String str) {
        char[] chars = str.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            // 交换字符
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
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
