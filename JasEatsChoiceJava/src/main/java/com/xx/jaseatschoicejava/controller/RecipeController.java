package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Recipe;
import com.xx.jaseatschoicejava.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 食谱控制器
 */
@RestController
@RequestMapping("/v1/recipe")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;



    /**
     * 获取今日食谱
     */
    @GetMapping("/today")
    public ResponseResult<?> getTodayRecipes(@RequestParam(value = "userId") String userId) {
        Map<String, Object> result = recipeService.getTodayRecipes(userId);
        return ResponseResult.success(result);
    }

    /**
     * 获取我的食谱
     */
    @GetMapping("/favorite")
    public ResponseResult<?> getFavoriteRecipes(@RequestParam(value = "userId", required = false) String userId) {
        List<Recipe> recipes = recipeService.getFavoriteRecipes(userId);
        return ResponseResult.success(recipes);
    }

    /**
     * 获取推荐食谱
     */
    @GetMapping("/recommend")
    public ResponseResult<?> getRecommendedRecipes() {
        List recipes = recipeService.getRecommendedRecipes();
        return ResponseResult.success(recipes);
    }
}
