package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Tutorial;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface TutorialMapper extends BaseMapper<Tutorial> {
    // 自定义查询：获取首页推荐的教程
    List<Tutorial> selectFeaturedTutorials();
}