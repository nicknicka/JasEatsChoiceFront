package com.xx.jaseatschoicejava.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.MenuDish;

import java.util.List;
import java.util.Map;

/**
 * 菜单菜品关联Mapper接口
 */


@Mapper
public interface MenuDishMapper extends BaseMapper<MenuDish> {

    /**
     * 根据菜品ID查询关联的所有菜单信息
     * @param dishId 菜品ID
     * @return 菜单信息列表，包含菜单ID、名称、类型、状态以及菜品在该菜单中的状态
     */
    @Select("SELECT m.id, m.name, m.type, m.status as menu_status, md.status as dish_status " +
            "FROM t_menu m " +
            "INNER JOIN t_menu_dish md ON m.id = md.menu_id " +
            "WHERE md.dish_id = #{dishId}")
    List<Map<String, Object>> selectMenusByDishId(@Param("dishId") String dishId);

    /**
     * 根据菜单ID查询菜品数量
     * @param menuId 菜单ID
     * @return 菜品数量
     */
    @Select("SELECT COUNT(*) FROM t_menu_dish WHERE menu_id = #{menuId}")
    Integer selectDishCountByMenuId(@Param("menuId") String menuId);
}
