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
                    ((String) responseMap.get("rectangle")).split(";")[0].split(",")[0] : 121.4737); // 默认经度
                location.put("latitude", responseMap.get("rectangle") != null ?
                    ((String) responseMap.get("rectangle")).split(";")[0].split(",")[1] : 31.2304); // 默认纬度
                location.put("address",
                    (responseMap.get("province") != null ? responseMap.get("province").toString() : "") +
                    (responseMap.get("city") != null ? responseMap.get("city").toString() : "") +
                    (responseMap.get("district") != null ? responseMap.get("district").toString() : ""));

                return location;
            }
        } catch (Exception e) {
            logger.error("Failed to fetch real location data from Gaode Map API: {}", e.getMessage());
            e.printStackTrace();
        }

        // API调用失败时返回模拟数据
        logger.info("Using mock location data...");
        Map<String, Object> mockLocation = new HashMap<>();
        mockLocation.put("city", "上海");
        mockLocation.put("district", "浦东新区");
        mockLocation.put("longitude", 121.4737);
        mockLocation.put("latitude", 31.2304);
        mockLocation.put("address", "上海市浦东新区陆家嘴");

        return mockLocation;
    }

    @Override
    public List<Map<String, Object>> getCascaderLocationData() {
        try {
            // 调用高德地图API获取中国所有省市区数据
            String url = String.format("%s/config/district?keywords=中国&subdistrict=2&key=%s", gaodeApiUrl, gaodeApiKey);

            // 从API获取原始数据
            String response = restTemplate.getForObject(url, String.class);

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
                    // 如果解析结果为空，返回模拟数据
                    return getMockCascaderData();
                }
            } else {
                // 如果API调用成功但返回错误状态，返回模拟数据
                return getMockCascaderData();
            }

            return cascaderData;
        } catch (Exception e) {
            logger.error("Failed to fetch location data from Gaode Map API: {}", e.getMessage());
            e.printStackTrace();

            // API调用失败时返回模拟数据
            return getMockCascaderData();
        }
    }

    /**
     * 获取模拟的级联选择器地址数据
     */
    private List<Map<String, Object>> getMockCascaderData() {
        List<Map<String, Object>> mockData = new ArrayList<>();

        // 模拟几个省份
        String[][] provincesCitiesAreas = {
            {"北京市", "北京市", "东城区"},
            {"上海市", "上海市", "浦东新区"},
            {"广东省", "广州市", "天河区"},
            {"广东省", "深圳市", "南山区"},
            {"江苏省", "南京市", "玄武区"},
            {"浙江省", "杭州市", "西湖区"}
        };

        Map<String, Map<String, List<String>>> provinceCityAreaMap = new LinkedHashMap<>();

        for (String[] pca : provincesCitiesAreas) {
            String province = pca[0];
            String city = pca[1];
            String area = pca[2];

            provinceCityAreaMap.computeIfAbsent(province, k -> new LinkedHashMap<>());
            Map<String, List<String>> cityAreaMap = provinceCityAreaMap.get(province);
            cityAreaMap.computeIfAbsent(city, k -> new ArrayList<>());
            cityAreaMap.get(city).add(area);
        }

        for (Map.Entry<String, Map<String, List<String>>> provinceEntry : provinceCityAreaMap.entrySet()) {
            Map<String, Object> provinceNode = new HashMap<>();
            provinceNode.put("value", provinceEntry.getKey());
            provinceNode.put("label", provinceEntry.getKey());

            List<Map<String, Object>> cityNodes = new ArrayList<>();
            for (Map.Entry<String, List<String>> cityEntry : provinceEntry.getValue().entrySet()) {
                Map<String, Object> cityNode = new HashMap<>();
                cityNode.put("value", cityEntry.getKey());
                cityNode.put("label", cityEntry.getKey());

                List<Map<String, Object>> areaNodes = new ArrayList<>();
                for (String area : cityEntry.getValue()) {
                    Map<String, Object> areaNode = new HashMap<>();
                    areaNode.put("value", area);
                    areaNode.put("label", area);
                    areaNodes.add(areaNode);
                }

                cityNode.put("children", areaNodes);
                cityNodes.add(cityNode);
            }

            provinceNode.put("children", cityNodes);
            mockData.add(provinceNode);
        }

        return mockData;
    }
}
