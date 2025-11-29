package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI能力控制器
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AIController {

    /**
     * AI菜品识别
     */
    @PostMapping("/dish-recognize")
    public ResponseResult<?> dishRecognize(@RequestBody Map<String, Object> params) {
        // TODO: Integrate with third-party AI service for dish recognition
        // For now, return mock data

        List<Map<String, Object>> dishes = new ArrayList<>();

        Map<String, Object> dish1 = new HashMap<>();
        dish1.put("name", "宫保鸡丁");
        dish1.put("calorie", 150.5);
        dish1.put("difficulty", "easy");
        dish1.put("ingredients", "鸡肉, 花生米, 辣椒");
        dish1.put("steps", "1. 鸡肉切丁; 2. 炒制; 3. 加入花生米");

        Map<String, Object> dish2 = new HashMap<>();
        dish2.put("name", "番茄炒蛋");
        dish2.put("calorie", 120.3);
        dish2.put("difficulty", "easy");
        dish2.put("ingredients", "番茄, 鸡蛋");
        dish2.put("steps", "1. 鸡蛋打散; 2. 炒鸡蛋; 3. 加入番茄");

        dishes.add(dish1);
        dishes.add(dish2);

        return ResponseResult.success(dishes);
    }

    /**
     * AI食谱上传与优化
     */
    @PostMapping("/recipe-upload")
    public ResponseResult<?> recipeUpload(@RequestBody Map<String, Object> params) {
        // TODO: Integrate with third-party AI service for recipe optimization
        // For now, return mock data with optimized recipe

        Map<String, Object> recipe = (Map<String, Object>) params.get("recipe");
        Map<String, Object> optimizedRecipe = new HashMap<>(recipe);

        // Add mock optimization
        optimizedRecipe.put("optimization", "AI优化建议：减少油盐用量，增加蔬菜比例");
        optimizedRecipe.put("calorie", 250.0); // Mock calorie calculation

        return ResponseResult.success(optimizedRecipe);
    }

    /**
     * AI聊天接口
     */
    @PostMapping("/chat")
    public ResponseResult<?> chat(@RequestBody Map<String, Object> params) {
        // TODO: Integrate with third-party AI service for chat functionality
        // For now, return mock data
        String message = params.get("message").toString();

        Map<String, Object> response = new HashMap<>();
        response.put("content", "我已经收到您的问题：" + message + "。这是一个模拟的AI回复，实际应用中将连接后端AI服务获取智能饮食建议。");

        return ResponseResult.success(response);
    }

    /**
     * AI营养分析接口
     */
    @PostMapping("/nutrient")
    public ResponseResult<?> nutrient(@RequestBody Map<String, Object> params) {
        // TODO: Integrate with third-party AI service for nutrient analysis
        // For now, return mock data

        String foodName = params.get("foodName").toString();

        Map<String, Object> response = new HashMap<>();
        response.put("foodName", foodName);
        response.put("calorie", 350.5); // 模拟卡路里
        response.put("protein", 20.3);   // 模拟蛋白质(g)
        response.put("fat", 15.7);       // 模拟脂肪(g)
        response.put("carbohydrate", 40.2); // 模拟碳水化合物(g)
        response.put("fiber", 5.8);      // 模拟膳食纤维(g)

        return ResponseResult.success(response);
    }

    /**
     * AI食谱推荐接口
     */
    @PostMapping("/recipe")
    public ResponseResult<?> recipe(@RequestBody Map<String, Object> params) {
        // TODO: Integrate with third-party AI service for recipe recommendation
        // For now, return mock data

        List<Map<String, Object>> recipes = new ArrayList<>();

        // 模拟食谱数据
        Map<String, Object> recipe1 = new HashMap<>();
        recipe1.put("name", "清蒸鲈鱼");
        recipe1.put("calorie", 280.0);
        recipe1.put("difficulty", "easy");
        recipe1.put("ingredients", "鲈鱼, 姜, 葱, 料酒, 蒸鱼豉油");
        recipe1.put("steps", "1. 鲈鱼处理干净; 2. 姜葱切好铺在盘子上; 3. 鲈鱼放姜葱上; 4. 蒸8分钟; 5. 倒蒸鱼豉油, 热油浇上");

        Map<String, Object> recipe2 = new HashMap<>();
        recipe2.put("name", "清炒时蔬");
        recipe2.put("calorie", 120.3);
        recipe2.put("difficulty", "easy");
        recipe2.put("ingredients", "西兰花, 胡萝卜, 蒜, 盐, 鸡精");
        recipe2.put("steps", "1. 时蔬洗净切好; 2. 蒜切末; 3. 热油炒蒜末; 4. 加入时蔬翻炒; 5. 加盐和鸡精调味");

        recipes.add(recipe1);
        recipes.add(recipe2);

        return ResponseResult.success(recipes);
    }
}
