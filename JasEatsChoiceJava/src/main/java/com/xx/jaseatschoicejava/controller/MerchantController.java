package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.service.MerchantService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.service.DishService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 商家控制器
 */
@RestController
@RequestMapping("/api/v1/merchant")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDishService orderDishService;

    @Autowired
    private DishService dishService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取商家列表
     */
    @GetMapping
    public ResponseResult<?> getMerchants(@RequestParam(required = false) String category,
                                           @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();
        if (category != null) {
            queryWrapper.eq(Merchant::getCategory, category);
        }
        if (keyword != null) {
            queryWrapper.like(Merchant::getName, keyword);
        }
        // 只显示营业的商家
        queryWrapper.eq(Merchant::getStatus, true);
        List<Merchant> merchants = merchantService.list(queryWrapper);
        return ResponseResult.success(merchants);
    }

    /**
     * 获取商家详情
     */
    @GetMapping("/{merchantId}")
    public ResponseResult<?> getMerchantDetail(@PathVariable Long merchantId) {
        Merchant merchant = merchantService.getById(merchantId);
        if (merchant != null) {
            return ResponseResult.success(merchant);
        }
        return ResponseResult.fail("404", "商家不存在");
    }

    /**
     * 获取店铺相册
     */
    @GetMapping("/{merchantId}/album")
    public ResponseResult<?> getMerchantAlbum(@PathVariable Long merchantId) {
        Merchant merchant = merchantService.getById(merchantId);
        if (merchant != null) {
            JsonNode album = merchant.getAlbum();
            if (album != null) {
                return ResponseResult.success(album);
            } else {
                // 如果相册为空，返回默认结构
                ObjectNode defaultAlbum = objectMapper.createObjectNode();
                defaultAlbum.set("environment", objectMapper.createArrayNode());
                defaultAlbum.set("dishes", objectMapper.createArrayNode());
                return ResponseResult.success(defaultAlbum);
            }
        }
        return ResponseResult.fail("404", "商家不存在");
    }

    /**
     * 上传店铺相册照片
     */
    @PostMapping("/{merchantId}/album")
    public ResponseResult<?> uploadMerchantAlbum(@PathVariable Long merchantId,
                                                @RequestParam("images") MultipartFile[] images,
                                                @RequestParam("albumType") String albumType) {
        // 模拟图片上传，实际应保存到文件系统或云存储，并返回URL
        try {
            Merchant merchant = merchantService.getById(merchantId);
            if (merchant != null) {
                JsonNode album = merchant.getAlbum();
                ObjectNode albumObject;

                if (album != null) {
                    albumObject = (ObjectNode) album;
                } else {
                    albumObject = objectMapper.createObjectNode();
                    albumObject.set("environment", objectMapper.createArrayNode());
                    albumObject.set("dishes", objectMapper.createArrayNode());
                }

                // 获取对应类型的相册数组
                ArrayNode albumArray = (ArrayNode) albumObject.get(albumType);

                // 模拟上传，将图片转为URL并添加到相册
                for (MultipartFile image : images) {
                    // 实际开发中应保存图片并生成URL
                    String imageUrl = "http://localhost:8080/uploads/" + image.getOriginalFilename();
                    albumArray.add(imageUrl);
                }

                // 更新商家相册
                merchant.setAlbum(albumObject);
                merchantService.updateById(merchant);

                // 返回上传的图片URL
                return ResponseResult.success(albumArray);
            }
            return ResponseResult.fail("404", "商家不存在");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "上传失败");
        }
    }

    /**
     * 删除店铺相册照片
     */
    @DeleteMapping("/{merchantId}/album")
    public ResponseResult<?> deleteMerchantAlbum(@PathVariable Long merchantId,
                                                @RequestParam("imageUrl") String imageUrl,
                                                @RequestParam("albumType") String albumType) {
        try {
            Merchant merchant = merchantService.getById(merchantId);
            if (merchant != null) {
                JsonNode album = merchant.getAlbum();
                if (album != null) {
                    ObjectNode albumObject = (ObjectNode) album;
                    ArrayNode albumArray = (ArrayNode) albumObject.get(albumType);

                    // 查找并删除图片URL
                    for (int i = 0; i < albumArray.size(); i++) {
                        if (albumArray.get(i).asText().equals(imageUrl)) {
                            albumArray.remove(i);
                            break;
                        }
                    }

                    // 更新商家相册
                    merchant.setAlbum(albumObject);
                    merchantService.updateById(merchant);

                    return ResponseResult.success("删除成功");
                }
            }
            return ResponseResult.fail("404", "商家不存在");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "删除失败");
        }
    }

    /**
     * 获取今日营业概览
     */
    @GetMapping("/{merchantId}/business-overview")
    public ResponseResult<?> getBusinessOverview(@PathVariable Long merchantId) {
        try {
            // 从数据库获取真实数据
            Map<String, Object> overview = new HashMap<>();

            // 获取当前日期
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusSeconds(1);

            // 查询今日订单
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getMerchantId, merchantId)
                       .ge(Order::getCreateTime, startOfDay)
                       .le(Order::getCreateTime, endOfDay);

            // 获取订单列表
            List<Order> orders = orderService.list(queryWrapper);

            // 计算营业额（排除已取消的订单）
            BigDecimal totalSales = orders.stream()
                    .filter(order -> order.getStatus() != 6) // status 6 是已取消
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 计算订单数（排除已取消的订单）
            long totalOrders = orders.stream()
                    .filter(order -> order.getStatus() != 6) // status 6 是已取消
                    .count();

            // 设置结果
            overview.put("sales", totalSales.doubleValue());  // 营业额
            overview.put("orders", totalOrders);               // 订单数

            // 新增评价和未读消息功能暂未实现，这里使用模拟数据
            overview.put("newComments", 5);                   // 新增评价
            overview.put("unreadMessages", 3);                // 未读消息

            return ResponseResult.success(overview);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "获取营业概览失败");
        }
    }

    /**
     * 获取经营统计数据
     */
    @GetMapping("/{merchantId}/statistics")
    public ResponseResult<?> getStatistics(@PathVariable Long merchantId, @RequestParam String timeRange) {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startTime = null;
            LocalDateTime endTime = LocalDate.now().atStartOfDay().plusDays(1).minusSeconds(1);

            // 根据时间范围确定查询时间
            switch (timeRange) {
                case "today":
                    startTime = today.atStartOfDay();
                    break;
                case "yesterday":
                    startTime = today.minusDays(1).atStartOfDay();
                    endTime = today.atStartOfDay().minusSeconds(1);
                    break;
                case "week":
                    startTime = today.minusDays(today.getDayOfWeek().getValue() - 1).atStartOfDay();
                    break;
                case "month":
                    startTime = today.withDayOfMonth(1).atStartOfDay();
                    break;
            }

            // 查询订单数据
            LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
            orderQuery.eq(Order::getMerchantId, merchantId)
                      .ge(Order::getCreateTime, startTime)
                      .le(Order::getCreateTime, endTime)
                      .ne(Order::getStatus, 6); // 排除已取消订单

            List<Order> orders = orderService.list(orderQuery);

            // 计算基本统计数据
            Map<String, Object> basicStats = new HashMap<>();
            long totalOrders = orders.size();
            double totalAmount = orders.stream()
                                      .map(Order::getTotalAmount)
                                      .reduce(BigDecimal.ZERO, BigDecimal::add)
                                      .doubleValue();
            double avgAmount = totalOrders > 0 ? totalAmount / totalOrders : 0;
            long newCustomers = orders.stream()
                                      .map(Order::getUserId)
                                      .distinct()
                                      .count();

            basicStats.put("orders", totalOrders);
            basicStats.put("totalAmount", totalAmount);
            basicStats.put("avgAmount", avgAmount);
            basicStats.put("newCustomers", newCustomers);

            // 准备响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("basicStats", basicStats);

            // 计算订单趋势数据
            List<Map<String, Object>> orderTrend = new ArrayList<>();

            // 根据时间范围计算不同的时间间隔
            if ("today".equals(timeRange) || "yesterday".equals(timeRange)) {
                // 按小时统计
                for (int i = 0; i < 24; i++) {
                    String time = String.format("%02d:00", i);
                    LocalDateTime hourStart = startTime.withHour(i).withMinute(0).withSecond(0);
                    LocalDateTime hourEnd = startTime.withHour(i).withMinute(59).withSecond(59);

                    // 处理昨天的情况
                    final LocalDateTime finalStart;
                    final LocalDateTime finalEnd;
                    if ("yesterday".equals(timeRange)) {
                        finalStart = hourStart.minusDays(1);
                        finalEnd = hourEnd.minusDays(1);
                    } else {
                        finalStart = hourStart;
                        finalEnd = hourEnd;
                    }

                    long count = orders.stream()
                                      .filter(order -> order.getCreateTime().isAfter(finalStart) && order.getCreateTime().isBefore(finalEnd))
                                      .count();

                    Map<String, Object> trendItem = new HashMap<>();
                    trendItem.put("time", time);
                    trendItem.put("orders", count);
                    orderTrend.add(trendItem);
                }
            } else if ("week".equals(timeRange)) {
                // 按星期统计
                String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

                for (int i = 0; i < 7; i++) {
                    LocalDateTime dayStart = startTime.plusDays(i);
                    LocalDateTime dayEnd = startTime.plusDays(i).plusDays(1).minusSeconds(1);

                    long count = orders.stream()
                                      .filter(order -> order.getCreateTime().isAfter(dayStart) && order.getCreateTime().isBefore(dayEnd))
                                      .count();

                    Map<String, Object> trendItem = new HashMap<>();
                    trendItem.put("time", weekdays[i]);
                    trendItem.put("orders", count);
                    orderTrend.add(trendItem);
                }
            } else if ("month".equals(timeRange)) {
                // 按月统计
                String[] months = {"1日", "2日", "3日", "4日", "5日", "6日", "7日", "8日", "9日", "10日",
                                 "11日", "12日", "13日", "14日", "15日", "16日", "17日", "18日", "19日", "20日",
                                 "21日", "22日", "23日", "24日", "25日", "26日", "27日", "28日", "29日", "30日", "31日"};

                // 统计前5天
                for (int i = 0; i < 5; i++) {
                    LocalDateTime dayStart = startTime.plusDays(i);
                    LocalDateTime dayEnd = startTime.plusDays(i).plusDays(1).minusSeconds(1);

                    long count = orders.stream()
                                      .filter(order -> order.getCreateTime().isAfter(dayStart) && order.getCreateTime().isBefore(dayEnd))
                                      .count();

                    Map<String, Object> trendItem = new HashMap<>();
                    trendItem.put("time", months[i]);
                    trendItem.put("orders", count);
                    orderTrend.add(trendItem);
                }
            }

            response.put("orderTrend", orderTrend);

            // 计算菜品销量排行数据
            // 先获取所有订单ID
            List<Long> orderIds = orders.stream()
                                      .map(Order::getId)
                                      .collect(Collectors.toList());

            List<Map<String, Object>> dishSalesRank = new ArrayList<>();

            if (!orderIds.isEmpty()) {
                // 查询所有订单菜品
                LambdaQueryWrapper<OrderDish> orderDishQuery = new LambdaQueryWrapper<>();
                orderDishQuery.in(OrderDish::getOrderId, orderIds);

                List<OrderDish> orderDishes = orderDishService.list(orderDishQuery);

                // 按菜品ID分组，统计销量
                Map<Long, Integer> dishSalesMap = orderDishes.stream()
                                                            .collect(Collectors.groupingBy(OrderDish::getDishId,
                                                                                         Collectors.summingInt(OrderDish::getQuantity)));

                // 获取菜品信息并计算销售额
                for (Map.Entry<Long, Integer> entry : dishSalesMap.entrySet()) {
                    Long dishId = entry.getKey();
                    int sales = entry.getValue();

                    Dish dish = dishService.getById(dishId);
                    if (dish != null) {
                        Map<String, Object> dishSales = new HashMap<>();
                        dishSales.put("name", dish.getName());
                        dishSales.put("sales", sales);
                        dishSales.put("revenue", dish.getPrice().multiply(new BigDecimal(sales)).doubleValue());
                        dishSalesRank.add(dishSales);
                    }
                }

                // 按销量排序
                dishSalesRank.sort((a, b) -> Integer.compare((Integer) b.get("sales"), (Integer) a.get("sales")));

                // 只取前5名
                if (dishSalesRank.size() > 5) {
                    dishSalesRank = dishSalesRank.subList(0, 5);
                }
            }

            response.put("dishSalesRank", dishSalesRank);

            return ResponseResult.success(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("500", "获取统计数据失败");
        }
    }
}
