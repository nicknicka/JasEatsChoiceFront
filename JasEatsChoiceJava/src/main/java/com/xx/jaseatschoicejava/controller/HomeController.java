package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/home")
public class HomeController {

    @GetMapping("/hot-topic")
    public ResponseResult<String> getHotTopic() {
        // Return a sample hot topic for now
        // In a real implementation, this might come from a database or external service
        return ResponseResult.success("夏日清凉饮食指南");
    }
}
