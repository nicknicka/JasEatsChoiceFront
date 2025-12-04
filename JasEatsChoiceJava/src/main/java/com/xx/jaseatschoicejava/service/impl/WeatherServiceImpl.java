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
import java.util.Map;

/**
 * 天气服务实现
 */
@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${gaode.map.api.key}")
    private String gaodeApiKey;

    @Value("${gaode.map.api.url}")
    private String gaodeApiUrl;

    @Value("${gaode.map.api.weatherUrl}")
    private String gaodeApiWeatherUrl;

    @Override
    public Map<String, Object> getWeatherInfo(String city) {
        try {
            // 调用高德地图天气API获取天气信息
            String url = String.format("%s?key=%s&city=%s", gaodeApiWeatherUrl, gaodeApiKey, city);

            // 从API获取原始数据
            String response = restTemplate.getForObject(url, String.class);
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
                // 这里可以根据高德API返回的实际结构进行解析
                // 目前返回简单的模拟结构，需要根据实际API返回调整
                weather.put("city", city);
                weather.put("temperature", "28");
                weather.put("humidity", "75%");
                weather.put("condition", "晴天");
                weather.put("windSpeed", "微风");

                logger.info("成功从高德地图API获取天气信息: {}", response);
                return weather;
            } else {
                logger.warn("从高德地图API获取天气信息失败: API返回错误状态，status={}, info={}, infocode={}",
                    responseMap.get("status"), responseMap.get("info"), responseMap.get("infocode"));
            }
        } catch (Exception e) {
            logger.error("从高德地图API获取天气信息失败: {}", e.getMessage());
            e.printStackTrace();
        }

        // API调用失败时返回空数据
        logger.warn("由于API获取失败，返回空的天气数据");
        return new HashMap<>();
    }
}
