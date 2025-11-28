package com.xx.jaseatschoicejava.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.xx.jaseatschoicejava.service.WeatherService;

/**
 * 天气服务实现
 */
@Service
public class WeatherServiceImpl implements WeatherService {
    @Override
    public Map<String, Object> getWeatherInfo(String city) {
        // TODO: 未来这里可以替换为真正的第三方天气API调用
        // 目前返回模拟天气数据

        Map<String, Object> weather = new HashMap<>();
        weather.put("city", city);
        weather.put("temperature", 28);
        weather.put("humidity", 75);
        weather.put("condition", "晴天");
        weather.put("windSpeed", "微风");

        return weather;
    }
}
