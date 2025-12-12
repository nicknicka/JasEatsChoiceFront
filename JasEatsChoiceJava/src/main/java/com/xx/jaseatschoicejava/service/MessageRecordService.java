package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.MessageRecord;

import java.util.List;

/**
 * 消息记录Service
 *
 * @author xx
 * @date 2025-01-26
 */
public interface MessageRecordService extends IService<MessageRecord> {

    /**
     * 根据用户ID获取消息记录列表
     *
     * @param userId 用户ID
     * @param pageSize 分页大小
     * @param pageNum 当前页码
     * @return 消息记录列表
     */
    List<MessageRecord> getMessageRecordsByUserId(Long userId, Integer pageSize, Integer pageNum);

    /**
     * 根据用户ID获取未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Integer getUnreadMessageCountByUserId(Long userId);

    /**
     * 发送消息
     *
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param content 消息内容
     * @param messageType 消息类型
     * @return 发送结果
     */
    Boolean sendMessage(Long senderId, Long receiverId, String content, String messageType);

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @return 标记结果
     */
    Boolean markMessageAsRead(Long messageId);

    /**
     * 标记所有消息为已读
     *
     * @param userId 用户ID
     * @return 标记结果
     */
    Boolean markAllMessagesAsRead(Long userId);
}
