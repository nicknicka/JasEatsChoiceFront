package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.entity.Tutorial;
import com.xx.jaseatschoicejava.service.TutorialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tutorial")
public class TutorialController {

    @Autowired
    private TutorialService tutorialService;

    // 获取首页推荐的教程
    @GetMapping("/featured")
    public ResponseEntity<List<Tutorial>> getFeaturedTutorials() {
        List<Tutorial> tutorials = tutorialService.getFeaturedTutorials();
        return ResponseEntity.ok(tutorials);
    }

    // 获取所有教程
    @GetMapping("/list")
    public ResponseEntity<List<Tutorial>> getAllTutorials() {
        List<Tutorial> tutorials = tutorialService.getAllTutorials();
        return ResponseEntity.ok(tutorials);
    }
}