package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.LocationService;
import com.xx.jaseatschoicejava.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 定位与天气控制器
 */
@RestController
@RequestMapping("/api/v1")
public class LocationWeatherController {

    @Autowired
    private LocationService locationService;

    @Autowired
    private WeatherService weatherService;

    /**
     * 获取当前定位
     */
    @GetMapping("/location")
    public ResponseResult<?> getCurrentLocation() {
        // 调用定位服务获取当前定位
        Map<String, Object> location = locationService.getCurrentLocation();
        return ResponseResult.success(location);
    }

    /**
     * 获取天气信息
     */
    @GetMapping("/weather")
    public ResponseResult<?> getWeatherInfo(@RequestParam String city) {
        // 调用天气服务获取天气信息
        Map<String, Object> weather = weatherService.getWeatherInfo(city);
        return ResponseResult.success(weather);
    }
}
