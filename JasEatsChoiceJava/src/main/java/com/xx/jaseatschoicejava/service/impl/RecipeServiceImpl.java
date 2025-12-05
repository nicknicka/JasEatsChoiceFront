package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Recipe;
import com.xx.jaseatschoicejava.mapper.RecipeMapper;
import com.xx.jaseatschoicejava.service.RecipeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 食谱Service实现
 */
@Service
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {

    @Override
    public Map<String, Object> getTodayRecipes() {
        // 查询今日食谱
        QueryWrapper<Recipe> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_today", true);
        queryWrapper.orderByDesc("create_time");

        List<Recipe> recipes = list(queryWrapper);

        // 按照餐点类型分组
        Map<String, List<String>> recipesByType = recipes.stream()
                .collect(Collectors.groupingBy(
                        Recipe::getType,
                        Collectors.mapping(Recipe::getContent, Collectors.toList())
                ));

        // 计算营养总摄入
        int totalCalories = recipes.stream().mapToInt(Recipe::getCalories).sum();
        int totalProtein = recipes.stream().mapToInt(Recipe::getProtein).sum();
        int totalCarbs = recipes.stream().mapToInt(Recipe::getCarbs).sum();
        int totalFat = recipes.stream().mapToInt(Recipe::getFat).sum();

        // 营养摄入数据
        Map<String, Object> nutrition = new HashMap<>();
        nutrition.put("calories", totalCalories);
        nutrition.put("protein", totalProtein);
        nutrition.put("carbs", totalCarbs);
        nutrition.put("fat", totalFat);

        // 构造响应数据
        Map<String, Object> result = new HashMap<>();
        result.put("recipes", recipes);
        result.put("nutrition", nutrition);
        result.put("date", LocalDateTime.now());

        return result;
    }

    @Override
    public List<Recipe> getFavoriteRecipes() {
        // 查询收藏的食谱
        QueryWrapper<Recipe> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_favorite", true);
        queryWrapper.orderByDesc("create_time");

        return list(queryWrapper);
    }

    @Override
    public List<Recipe> getRecommendedRecipes() {
        // 查询推荐的食谱
        QueryWrapper<Recipe> queryWrapper = new QueryWrapper<>();
        // 这里可以根据实际业务逻辑调整推荐算法，比如按卡路里排序或随机推荐
        queryWrapper.orderByAsc("calories");
        queryWrapper.last("LIMIT 3"); // 只返回前3条

        return list(queryWrapper);
    }
}
