package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/v1/legacy/message")
public class MessageController {

    /**
     * 获取消息列表
     */
    @GetMapping("/list")
    public ResponseResult<?> getMessageList(@RequestParam Long userId) {
        // TODO: Implement message list retrieval from the database
        // 返回空的消息列表，前端将处理无数据的情况
        List<Map<String, Object>> messages = new ArrayList<>();
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
