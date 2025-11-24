package com.xx.jaseatschoicejava.service;

import java.util.Map;

/**
 * 定位服务接口
 */
public interface LocationService {
    /**
     * 获取当前定位
     * @return 定位信息
     */
    Map<String, Object> getCurrentLocation();
}
