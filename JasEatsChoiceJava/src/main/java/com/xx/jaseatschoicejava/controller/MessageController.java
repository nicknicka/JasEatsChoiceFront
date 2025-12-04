package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/v1/message")
public class MessageController {

    /**
     * 获取消息列表
     */
    @GetMapping("/list")
    public ResponseResult<?> getMessageList(@RequestParam Long userId) {
        // TODO: Implement message list retrieval from the database
        // For now, return mock data
        List<Map<String, Object>> messages = new ArrayList<>();

        // Mock message data
        Map<String, Object> message1 = new HashMap<>();
        message1.put("id", 1L);
        message1.put("senderId", "system");
        message1.put("senderName", "系统");
        message1.put("content", "欢迎使用佳食宜选，您可以开始体验个性化饮食推荐服务啦！");
        message1.put("createTime", "2023-11-28 10:00:00");
        message1.put("readStatus", false);

        Map<String, Object> message2 = new HashMap<>();
        message2.put("id", 2L);
        message2.put("senderId", "merchant123");
        message2.put("senderName", "张三餐馆");
        message2.put("content", "您的订单已经准备好，请及时取餐！");
        message2.put("createTime", "2023-11-28 09:30:00");
        message2.put("readStatus", true);

        messages.add(message1);
        messages.add(message2);

        return ResponseResult.success(messages);
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public ResponseResult<?> sendMessage(@RequestBody Map<String, Object> message) {
        // TODO: Implement message sending logic
        // For now, return success
        return ResponseResult.success("消息发送成功");
    }
}
