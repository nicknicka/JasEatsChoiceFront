package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Menu;
import com.xx.jaseatschoicejava.entity.MenuDish;
import com.xx.jaseatschoicejava.service.MenuService;
import com.xx.jaseatschoicejava.service.MenuDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * 菜单控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuDishService menuDishService;

    /**
     * 根据商家ID获取菜单列表
     */
    @GetMapping("/merchants/{merchantId}/menu")
    public ResponseResult<?> getMenusByMerchantId(@PathVariable Long merchantId) {
        LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Menu::getMerchantId, merchantId.toString());
        // 获取所有菜单，不区分状态
        List<Menu> menus = menuService.list(queryWrapper);

        // 将后端的状态转换为前端需要的格式
        menus.forEach(menu -> {
            if ("active".equals(menu.getStatus())) {
                menu.setStatus("online");
            } else {
                // 将inactive统一转换为offline（前端没有inactive状态）
                menu.setStatus("offline");
            }
        });

        return ResponseResult.success(menus);
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/{menuId}")
    public ResponseResult<?> getMenuDetail(@PathVariable String menuId) {
        Menu menu = menuService.getById(menuId);
        if (menu != null) {
            // 将后端的状态转换为前端需要的格式
            if ("active".equals(menu.getStatus())) {
                menu.setStatus("online");
            } else {
                menu.setStatus("offline");
            }
            return ResponseResult.success(menu);
        }
        return ResponseResult.fail("404", "菜单不存在");
    }

    /**
     * 获取菜单详情（根据商家ID和菜单ID）
     */
    @GetMapping("/merchants/{merchantId}/menu/{menuId}")
    public ResponseResult<?> getMenuDetailByMerchant(@PathVariable Long merchantId, @PathVariable String menuId) {
        Menu menu = menuService.getById(menuId);
        // 验证菜单是否属于该商家
        if (menu != null && merchantId.toString().equals(menu.getMerchantId())) {
            // 将后端的状态转换为前端需要的格式
            if ("active".equals(menu.getStatus())) {
                menu.setStatus("online");
            } else {
                menu.setStatus("offline");
            }
            return ResponseResult.success(menu);
        }
        return ResponseResult.fail("404", "菜单不存在");
    }

    /**
     * 获取菜单关联的菜品列表
     */
    @GetMapping("/merchants/{merchantId}/menu/{menuId}/dishes")
    public ResponseResult<?> getMenuDishes(@PathVariable Long merchantId, @PathVariable String menuId) {
        // 首先验证菜单是否属于该商家
        Menu menu = menuService.getById(menuId);
        if (menu == null || !merchantId.toString().equals(menu.getMerchantId())) {
            return ResponseResult.fail("404", "菜单不存在");
        }

        // 查询该菜单下的所有菜品关联
        LambdaQueryWrapper<MenuDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MenuDish::getMenuId, menuId);
        List<MenuDish> menuDishes = menuDishService.list(queryWrapper);

        return ResponseResult.success(menuDishes);
    }

    /**
     * 设置菜单自动上下架时间
     */
    @PutMapping("/merchants/{merchantId}/menu/{menuId}/schedule")
    public ResponseResult<?> setMenuSchedule(@PathVariable Long merchantId, @PathVariable String menuId, @RequestBody Map<String, Object> params) {
        try {
            LocalDateTime autoStartTime = LocalDateTime.parse((String) params.get("autoStartTime"));
            LocalDateTime autoEndTime = LocalDateTime.parse((String) params.get("autoEndTime"));
            boolean success = menuService.setMenuSchedule(menuId, autoStartTime, autoEndTime);
            if (success) {
                return ResponseResult.success("自动上下架时间设置成功");
            }
            return ResponseResult.fail("500", "自动上下架时间设置失败");
        } catch (DateTimeParseException e) {
            return ResponseResult.fail("400", "时间格式错误");
        }
    }

    /**
     * 菜单批量操作
     */
    @PostMapping("/merchants/{merchantId}/menu/batch")
    public ResponseResult<?> batchOperateMenus(@PathVariable Long merchantId, @RequestBody Map<String, Object> params) {
        // 将菜单ID转换为String类型
        List<String> menuIds = ((List<?>) params.get("menuIds")).stream()
            .map(Object::toString)
            .collect(java.util.stream.Collectors.toList());
        String action = (String) params.get("action");
        boolean success = menuService.batchOperateMenus(menuIds, action);
        if (success) {
            return ResponseResult.success("批量操作成功");
        }
        return ResponseResult.fail("500", "批量操作失败");
    }

    /**
     * 添加新菜单
     */
    @PostMapping("/merchants/{merchantId}/menu")
    public ResponseResult<?> addMenu(@PathVariable Long merchantId, @RequestBody Menu menu) {
        try {
            // 设置商家ID
            menu.setMerchantId(merchantId.toString());

            // 转换前端传来的状态值到后端统一格式
            String frontStatus = menu.getStatus();
            if ("online".equals(frontStatus) || "active".equals(frontStatus)) {
                menu.setStatus("active");
            } else { // draft或offline都转为inactive
                menu.setStatus("inactive");
            }

            boolean success = menuService.save(menu);
            if (success) {
                // 返回前端需要的状态格式
                Menu responseMenu = menuService.getById(menu.getId());
                if (responseMenu != null) {
                    // 将后端的active/inactive转换为前端的online/offline
                    if ("active".equals(responseMenu.getStatus())) {
                        responseMenu.setStatus("online");
                    } else {
                        responseMenu.setStatus("offline");
                    }
                    return ResponseResult.success(responseMenu);
                }
                return ResponseResult.fail("500", "保存菜单失败");
            }
            return ResponseResult.fail("500", "保存菜单失败");
        } catch (DateTimeParseException e) {
            // 处理时间格式解析异常
            return ResponseResult.fail("400", "时间格式错误，请使用正确的日期时间格式");
        } catch (Exception e) {
            return ResponseResult.fail("500", "保存菜单失败: " + e.getMessage());
        }
    }

    /**
     * 更新菜单
     */
    @PutMapping("/merchants/{merchantId}/menu/{menuId}")
    public ResponseResult<?> updateMenu(@PathVariable Long merchantId, @PathVariable String menuId, @RequestBody Map<String, Object> requestData) {
        log.info("Received update menu request: {}", requestData);
        try {
            // 验证菜单是否属于该商家
            Menu existingMenu = menuService.getById(menuId);
            if (existingMenu == null || !merchantId.toString().equals(existingMenu.getMerchantId())) {
                return ResponseResult.fail("404", "菜单不存在");
            }

            // 从请求数据中获取菜单基本信息
            Menu menu = new Menu();
            menu.setId(menuId);
            menu.setMerchantId(merchantId.toString());
            menu.setName((String) requestData.get("name"));
            menu.setType((String) requestData.get("category")); // 前端使用category，后端存储为type
            menu.setDescription((String) requestData.get("description")); // 菜单描述

            // 转换前端传来的状态值到后端统一格式
            String frontStatus = (String) requestData.get("status");
            if ("online".equals(frontStatus) || "active".equals(frontStatus)) {
                menu.setStatus("active");
            } else { // draft或offline都转为inactive
                menu.setStatus("inactive");
            }

            // 处理自动时间
            if (requestData.get("autoOnline") != null && !requestData.get("autoOnline").toString().isEmpty()) {
                // 处理时间格式，假设前端传来的是 HH:mm:ss 格式
                String autoOnlineStr = (String) requestData.get("autoOnline");
                // 如果是 HH:mm:ss 格式，需要拼接日期
                if (autoOnlineStr.length() == 8 && autoOnlineStr.contains(":")) {
                    LocalDateTime now = LocalDateTime.now();
                    String[] timeParts = autoOnlineStr.split(":");
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);
                    int second = Integer.parseInt(timeParts[2]);
                    menu.setAutoStartTime(now.withHour(hour).withMinute(minute).withSecond(second));
                }
            }

            if (requestData.get("autoOffline") != null && !requestData.get("autoOffline").toString().isEmpty()) {
                String autoOfflineStr = (String) requestData.get("autoOffline");
                if (autoOfflineStr.length() == 8 && autoOfflineStr.contains(":")) {
                    LocalDateTime now = LocalDateTime.now();
                    String[] timeParts = autoOfflineStr.split(":");
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);
                    int second = Integer.parseInt(timeParts[2]);
                    menu.setAutoEndTime(now.withHour(hour).withMinute(minute).withSecond(second));
                }
            }

            // 更新菜单基本信息
            boolean success = menuService.updateById(menu);

            // 处理菜品关联
            if (success && requestData.containsKey("dishes")) {
                // 删除旧的菜单菜品关联
                LambdaQueryWrapper<MenuDish> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(MenuDish::getMenuId, menuId);
                menuDishService.remove(deleteWrapper);

                // 添加新的菜单菜品关联
                List<?> dishIds = (List<?>) requestData.get("dishes");
                for (int i = 0; i < dishIds.size(); i++) {
                    Object dishIdObj = dishIds.get(i);
                    String dishId = null;

                    if (dishIdObj instanceof Number) {
                        dishId = String.valueOf(dishIdObj);
                    } else if (dishIdObj instanceof String) {
                        dishId = (String) dishIdObj;
                    }

                    if (dishId != null && !dishId.trim().isEmpty()) {
                        MenuDish menuDish = new MenuDish();
                        menuDish.setMenuId(menuId);
                        menuDish.setDishId(dishId);
                        menuDish.setSort(i); // 设置排序
                        menuDishService.save(menuDish);
                    }
                }
            }

            if (success) {
                // 返回前端需要的状态格式
                Menu responseMenu = menuService.getById(menuId);
                if (responseMenu != null) {
                    // 将后端的active/inactive转换为前端的online/offline
                    if ("active".equals(responseMenu.getStatus())) {
                        responseMenu.setStatus("online");
                    } else {
                        responseMenu.setStatus("offline");
                    }
                    return ResponseResult.success(responseMenu);
                }
                return ResponseResult.fail("500", "更新菜单失败");
            }
            return ResponseResult.fail("500", "更新菜单失败");
        } catch (DateTimeParseException e) {
            return ResponseResult.fail("400", "时间格式错误，请使用正确的日期时间格式");
        } catch (Exception e) {
            return ResponseResult.fail("500", "更新菜单失败: " + e.getMessage());
        }
    }
}
