package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Menu;
import com.xx.jaseatschoicejava.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * 菜单控制器
 */
@RestController
@RequestMapping("/v1/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

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
    @GetMapping("/menus/{menuId}")
    public ResponseResult<?> getMenuDetail(@PathVariable String menuId) {
        Menu menu = menuService.getById(menuId);
        if (menu != null) {
            return ResponseResult.success(menu);
        }
        return ResponseResult.fail("404", "菜单不存在");
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
}
