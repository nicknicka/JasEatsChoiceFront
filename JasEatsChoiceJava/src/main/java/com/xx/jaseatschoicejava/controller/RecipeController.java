package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 食谱控制器
 */
@RestController
@RequestMapping("/api/v1/recipe")
public class RecipeController {

    /**
     * 获取今日食谱
     */
    @GetMapping("/today")
    public ResponseResult<?> getTodayRecipes() {
        // 模拟今日食谱数据
        List<Map<String, Object>> recipes = new ArrayList<>();

        Map<String, Object> breakfast = new HashMap<>();
        breakfast.put("id", 1);
        breakfast.put("name", "早餐");
        breakfast.put("type", "breakfast");
        breakfast.put("items", Arrays.asList("牛奶燕麦粥", "水煮蛋", "苹果"));
        recipes.add(breakfast);

        Map<String, Object> lunch = new HashMap<>();
        lunch.put("id", 2);
        lunch.put("name", "午餐");
        lunch.put("type", "lunch");
        lunch.put("items", Arrays.asList("番茄炒蛋", "清炒菠菜", "杂粮饭"));
        recipes.add(lunch);

        Map<String, Object> dinner = new HashMap<>();
        dinner.put("id", 3);
        dinner.put("name", "晚餐");
        dinner.put("type", "dinner");
        dinner.put("items", Arrays.asList("清蒸鲈鱼", "凉拌黄瓜", "小米粥"));
        recipes.add(dinner);

        // 营养摄入数据
        Map<String, Object> nutrition = new HashMap<>();
        nutrition.put("calories", 1850);
        nutrition.put("protein", 85);
        nutrition.put("carbs", 220);
        nutrition.put("fat", 55);

        // 构造响应数据
        Map<String, Object> result = new HashMap<>();
        result.put("recipes", recipes);
        result.put("nutrition", nutrition);
        result.put("date", new Date());

        return ResponseResult.success(result);
    }

    /**
     * 获取我的食谱
     */
    @GetMapping("/favorite")
    public ResponseResult<?> getFavoriteRecipes() {
        // 模拟我的食谱数据
        List<Map<String, Object>> myRecipes = new ArrayList<>();

        Map<String, Object> recipe1 = new HashMap<>();
        recipe1.put("id", 1);
        recipe1.put("name", "健康早餐组合");
        recipe1.put("type", "早餐");
        recipe1.put("calories", 380);
        recipe1.put("time", "5分钟");
        recipe1.put("favorite", true);
        myRecipes.add(recipe1);

        Map<String, Object> recipe2 = new HashMap<>();
        recipe2.put("id", 2);
        recipe2.put("name", "减脂午餐");
        recipe2.put("type", "午餐");
        recipe2.put("calories", 450);
        recipe2.put("time", "15分钟");
        recipe2.put("favorite", false);
        myRecipes.add(recipe2);

        Map<String, Object> recipe3 = new HashMap<>();
        recipe3.put("id", 3);
        recipe3.put("name", "轻食晚餐");
        recipe3.put("type", "晚餐");
        recipe3.put("calories", 320);
        recipe3.put("time", "10分钟");
        recipe3.put("favorite", true);
        myRecipes.add(recipe3);

        Map<String, Object> recipe4 = new HashMap<>();
        recipe4.put("id", 4);
        recipe4.put("name", "健身餐");
        recipe4.put("type", "加餐");
        recipe4.put("calories", 280);
        recipe4.put("time", "8分钟");
        recipe4.put("favorite", true);
        myRecipes.add(recipe4);

        return ResponseResult.success(myRecipes);
    }

    /**
     * 获取推荐食谱
     */
    @GetMapping("/recommend")
    public ResponseResult<?> getRecommendedRecipes() {
        // 模拟推荐食谱数据
        List<Map<String, Object>> recommendedRecipes = new ArrayList<>();

        Map<String, Object> dish1 = new HashMap<>();
        dish1.put("name", "冰镇西瓜汁");
        dish1.put("kcal", "180kcal");
        dish1.put("rating", 5);
        dish1.put("type", "drink");
        recommendedRecipes.add(dish1);

        Map<String, Object> dish2 = new HashMap<>();
        dish2.put("name", "泰式青木瓜沙拉");
        dish2.put("kcal", "120kcal");
        dish2.put("rating", 4);
        dish2.put("type", "salad");
        recommendedRecipes.add(dish2);

        Map<String, Object> dish3 = new HashMap<>();
        dish3.put("name", "凉面套餐");
        dish3.put("kcal", "320kcal");
        dish3.put("rating", 5);
        dish3.put("type", "meal");
        recommendedRecipes.add(dish3);

        return ResponseResult.success(recommendedRecipes);
    }
}
