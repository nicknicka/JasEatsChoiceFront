package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.service.LocationService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 定位服务实现
 */
@Service
public class LocationServiceImpl implements LocationService {
    @Override
    public Map<String, Object> getCurrentLocation() {
        // TODO: 未来这里可以替换为真正的第三方定位API调用
        // 目前返回模拟定位数据

        Map<String, Object> location = new HashMap<>();
        location.put("city", "上海");
        location.put("district", "浦东新区");
        location.put("longitude", 121.4737);
        location.put("latitude", 31.2304);
        location.put("address", "上海市浦东新区陆家嘴");

        return location;
    }
}
