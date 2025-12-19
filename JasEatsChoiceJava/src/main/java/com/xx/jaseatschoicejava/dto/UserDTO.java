package com.xx.jaseatschoicejava.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.xx.jaseatschoicejava.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象，用于向前端传输用户信息，隐藏敏感字段
 */
@Data
public class UserDTO {
    private Long userId;
    private String phone;
    private String nickname;
    private Double height;
    private Double weight;
    private String dietGoal;
    private JsonNode allergies;
    private JsonNode preferTags;
    private String email;
    private Boolean disableWeatherRecommend;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 从User实体转换为UserDTO
     * @param user 用户实体
     * @return UserDTO
     */
    public static UserDTO fromUser(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setPhone(user.getPhone());
        userDTO.setNickname(user.getNickname());
        userDTO.setHeight(user.getHeight());
        userDTO.setWeight(user.getWeight());
        userDTO.setDietGoal(user.getDietGoal());
        userDTO.setAllergies(user.getAllergies());
        userDTO.setPreferTags(user.getPreferTags());
        userDTO.setEmail(user.getEmail());
        userDTO.setDisableWeatherRecommend(user.getDisableWeatherRecommend());
        userDTO.setCreateTime(user.getCreateTime());
        userDTO.setUpdateTime(user.getUpdateTime());
        // 敏感信息如password已自动隐藏，不进行设置
        return userDTO;
    }
}
