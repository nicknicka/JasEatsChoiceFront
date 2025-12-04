package com.xx.jaseatschoicejava.service;

import java.util.List;
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

    /**
     * 获取级联选择器地址数据
     * @return 地址数据列表
     */
    List<Map<String, Object>> getCascaderLocationData();
}
