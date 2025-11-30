
package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Discount;
import com.xx.jaseatschoicejava.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠活动控制器
 */
@RestController
@RequestMapping("/api/v1/merchant")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    /**
     * 获取商家的优惠活动列表
     */
    @GetMapping("/{merchantId}/discounts")
    public ResponseResult<?> getMerchantDiscounts(@PathVariable Long merchantId) {
        // 根据商家ID查询所有优惠活动
        LambdaQueryWrapper<Discount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Discount::getMerchantId, merchantId);

        List<Discount> discounts = discountService.list(queryWrapper);
        return ResponseResult.success(discounts);
    }
}
