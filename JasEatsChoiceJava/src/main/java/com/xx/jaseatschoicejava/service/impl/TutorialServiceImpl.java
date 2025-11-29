package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Tutorial;
import com.xx.jaseatschoicejava.mapper.TutorialMapper;
import com.xx.jaseatschoicejava.service.TutorialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TutorialServiceImpl extends ServiceImpl<TutorialMapper, Tutorial> implements TutorialService {

    @Autowired
    private TutorialMapper tutorialMapper;

    @Override
    public List<Tutorial> getFeaturedTutorials() {
        return tutorialMapper.selectFeaturedTutorials();
    }

    @Override
    public List<Tutorial> getAllTutorials() {
        return list();
    }
}