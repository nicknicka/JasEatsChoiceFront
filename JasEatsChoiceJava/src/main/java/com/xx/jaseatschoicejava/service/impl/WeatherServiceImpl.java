package com.xx.jaseatschoicejava.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 天气服务实现
 */
@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherServiceImpl.class);

    private final RestTemplate restTemplate;

    private final String gaodeApiKey;
    // private final String gaodeApiUrl; // 已废弃，使用gaodeApiWeatherUrl替代
    private final String gaodeApiWeatherUrl;

    @Autowired
    public WeatherServiceImpl(RestTemplate restTemplate,
                              @Value("${gaode.map.api.key}") String gaodeApiKey,
                              @Value("${gaode.map.api.weatherUrl}") String gaodeApiWeatherUrl) {
        this.restTemplate = restTemplate;
        this.gaodeApiKey = gaodeApiKey;
        this.gaodeApiWeatherUrl = gaodeApiWeatherUrl;
    }

    @Override
    public Map<String, Object> getWeatherInfo(String city) {
        try {
            // 调用高德地图天气API获取天气信息
            String url = String.format("%s?key=%s&city=%s", gaodeApiWeatherUrl, gaodeApiKey, city);

            // 从API获取原始数据
            String response = restTemplate.getForObject(url, String.class);
            logger.info("请求url {} ", url);
            logger.info("高德地图天气返回的response =  {}", response);

            // 检查API调用是否返回数据
            if (response == null) {
                logger.warn("从高德地图API获取天气信息失败: API未返回数据");
                return new HashMap<>();
            }

            // 使用Jackson解析JSON
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response, Map.class);

            // 解析天气数据
            boolean success = responseMap != null && "1".equals(responseMap.get("status"));

            if (success) {
                Map<String, Object> weather = new HashMap<>();

                // 解析高德地图API实际返回的结构
                Map<String, Object> lives = null;
                // 高德地图API返回的lives是数组结构
                if (responseMap.containsKey("lives") && responseMap.get("lives") instanceof List) {
                    List<?> livesList = (List<?>) responseMap.get("lives");
                    if (!livesList.isEmpty() && livesList.get(0) instanceof Map) {
                        lives = (Map<String, Object>) livesList.get(0);
                    }
                }

                // 从实际返回数据中提取字段
                if (lives != null) {
                    weather.put("city", lives.getOrDefault("city", city));
                    weather.put("temperature", lives.getOrDefault("temperature", "")); // 温度
                    weather.put("humidity", lives.getOrDefault("humidity", "")); // 湿度
                    weather.put("condition", lives.getOrDefault("weather", "")); // 天气情况
                    weather.put("windSpeed", lives.getOrDefault("windpower", "")); // 风速
                    weather.put("windDirection", lives.getOrDefault("winddirection", "")); // 风向
                    weather.put("reportTime", lives.getOrDefault("reporttime", "")); // 发布时间
                }

                logger.info("成功从高德地图API获取天气信息: {}", response);
                return weather;
            } else {
                logger.warn("从高德地图API获取天气信息失败: API返回错误状态，status={}, info={}, infocode={}",
                    responseMap.getOrDefault("status", "unknown"),
                    responseMap.getOrDefault("info", "unknown"),
                    responseMap.getOrDefault("infocode", "unknown"));
            }
        } catch (Exception e) {
            logger.error("从高德地图API获取天气信息失败: ", e);
        }

        // API调用失败时返回空数据
        logger.warn("由于API获取失败，返回空的天气数据");
        return new HashMap<>();
    }
}
