package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.Tutorial;

import java.util.List;

public interface TutorialService extends IService<Tutorial> {
    List<Tutorial> getFeaturedTutorials(); // 获取首页推荐教程
    List<Tutorial> getAllTutorials(); // 获取所有教程
}