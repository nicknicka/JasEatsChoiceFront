package com.xx.jaseatschoicejava.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

/**
 * 定位服务实现
 */
@Service
public class LocationServiceImpl implements LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${gaode.map.api.key}")
    private String gaodeApiKey;

    @Value("${gaode.map.api.url}")
    private String gaodeApiUrl;

    @Override
    public Map<String, Object> getCurrentLocation() {
        try {
            // 调用高德地图IP定位API获取当前位置
            String url = String.format("%s/ip?key=%s", gaodeApiUrl, gaodeApiKey);

            // 从API获取原始数据
            String response = restTemplate.getForObject(url, String.class);

//            logger.info("高德地图返回的response =  {}", response);
            // 使用Jackson解析JSON
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response, Map.class);


            // 解析定位数据
            boolean success = responseMap != null && "1".equals(responseMap.get("status"));

            if (success) {
                Map<String, Object> location = new HashMap<>();
                location.put("city", responseMap.get("city"));
                location.put("district", responseMap.get("district"));
                location.put("longitude", responseMap.get("rectangle") != null ?
                    ((String) responseMap.get("rectangle")).split(";")[0].split(",")[0] : null); // 不再返回默认值
                location.put("latitude", responseMap.get("rectangle") != null ?
                    ((String) responseMap.get("rectangle")).split(";")[0].split(",")[1] : null); // 不再返回默认值
                location.put("address",
                    (responseMap.get("province") != null ? responseMap.get("province").toString() : "") +
                    (responseMap.get("city") != null ? responseMap.get("city").toString() : "") +
                    (responseMap.get("district") != null ? responseMap.get("district").toString() : ""));

                // 如果解析结果有效，返回真实数据，否则返回空数据
                if (location.get("city") != null || location.get("longitude") != null) {
                    return location;
                } else {
                    logger.warn("从高德地图API获取有效的定位数据失败: 所有字段都是null");
                    return new HashMap<>();
                }
            }
        } catch (Exception e) {
            logger.error("从高德地图API获取真实定位数据失败: {}", e.getMessage());
            e.printStackTrace();
            logger.warn("由于API获取失败，返回空的定位数据");
        }

        // API调用失败或未获取到有效数据时返回空数据
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> getCascaderLocationData() {
        try {
            // 调用高德地图API获取中国所有省市区数据
            String url = String.format("%s/config/district?keywords=中国&subdistrict=2&key=%s", gaodeApiUrl, gaodeApiKey);

            // 从API获取原始数据
            String response = restTemplate.getForObject(url, String.class);
//            logger.info("高德地图返回的response =  {}", response);
            // 使用Jackson解析JSON
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response, Map.class);

            // 解析省份数据
            List<Map<String, Object>> cascaderData = new ArrayList<>();

            boolean success = responseMap != null && "1".equals(responseMap.get("status"));

            if (success) {
                List<Map<String, Object>> districts = (List<Map<String, Object>>) responseMap.get("districts");

                if (districts != null && !districts.isEmpty()) {
                    Map<String, Object> china = districts.get(0);
                    List<Map<String, Object>> provinces = (List<Map<String, Object>>) china.get("districts");

                    for (Map<String, Object> province : provinces) {
                        // 创建省份节点
                        Map<String, Object> provinceNode = new HashMap<>();
                        provinceNode.put("value", province.get("name"));
                        provinceNode.put("label", province.get("name"));

                        // 获取城市数据
                        List<Map<String, Object>> cities = (List<Map<String, Object>>) province.get("districts");
                        List<Map<String, Object>> cityNodes = new ArrayList<>();

                        for (Map<String, Object> city : cities) {
                            // 创建城市节点
                            Map<String, Object> cityNode = new HashMap<>();
                            cityNode.put("value", city.get("name"));
                            cityNode.put("label", city.get("name"));

                            // 获取区域数据
                            List<Map<String, Object>> areas = (List<Map<String, Object>>) city.get("districts");
                            List<Map<String, Object>> areaNodes = new ArrayList<>();

                            for (Map<String, Object> area : areas) {
                                // 创建区域节点
                                Map<String, Object> areaNode = new HashMap<>();
                                areaNode.put("value", area.get("name"));
                                areaNode.put("label", area.get("name"));

                                areaNodes.add(areaNode);
                            }

                            cityNode.put("children", areaNodes);
                            cityNodes.add(cityNode);
                        }

                        provinceNode.put("children", cityNodes);
                        cascaderData.add(provinceNode);
                    }
                }

                if (cascaderData.isEmpty()) {
                    // 如果解析结果为空，返回空数据并记录日志
                    logger.warn("从高德地图API获取并解析有效的位置数据失败: 解析结果为空数据");
                    return new ArrayList<>();
                }
            } else {
                // 如果API调用成功但返回错误状态，返回空数据并记录日志
                logger.warn("从高德地图API获取位置数据失败: API返回错误状态");
                return new ArrayList<>();
            }

            return cascaderData;
        } catch (Exception e) {
            logger.error("从高德地图API获取位置数据失败: {}", e.getMessage());
            e.printStackTrace();
            logger.warn("由于API获取失败，返回空的位置数据");
            return new ArrayList<>();
        }
    }

}
