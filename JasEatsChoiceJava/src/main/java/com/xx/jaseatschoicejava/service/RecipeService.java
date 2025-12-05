package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.Recipe;

import java.util.List;
import java.util.Map;

/**
 * 食谱Service
 */
public interface RecipeService extends IService<Recipe> {

    /**
     * 获取今日食谱
     * @param userId 用户ID
     * @return 今日食谱和营养信息
     */
    Map<String, Object> getTodayRecipes(Long userId);

    /**
     * 获取我的食谱
     * @param userId 用户ID
     * @return 我的食谱列表
     */
    List<Recipe> getFavoriteRecipes(Long userId);

    /**
     * 获取推荐食谱
     * @return 推荐食谱列表
     */
    List<Recipe> getRecommendedRecipes();
}
