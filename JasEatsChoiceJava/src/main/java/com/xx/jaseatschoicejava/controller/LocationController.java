package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 定位控制器
 */
@RestController
@RequestMapping("/v1/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

    /**
     * 获取当前定位
     * @param latitude 纬度
     * @param longitude 经度
     * @return 定位信息
     */
    @GetMapping
    public ResponseResult<?> getCurrentLocation(@RequestParam(required = false) Double latitude, @RequestParam(required = false) Double longitude) {
        // 调用定位服务获取当前定位
        Map<String, Object> location = locationService.getCurrentLocation(latitude, longitude);
        return ResponseResult.success(location);
    }

    /**
     * 获取级联选择器地址数据
     */
    @GetMapping("/cascader")
    public ResponseResult<?> getCascaderLocationData() {
        // 调用定位服务获取级联选择器地址数据
        List<Map<String, Object>> cascaderData = locationService.getCascaderLocationData();
        return ResponseResult.success(cascaderData);
    }

    /**
     * 地址搜索
     * @param address 搜索关键词
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseResult<?> searchAddress(@RequestParam String address) {
        List<Map<String, Object>> searchResults = locationService.searchAddress(address);
        return ResponseResult.success(searchResults);
    }
}
