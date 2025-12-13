package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Group;
import com.xx.jaseatschoicejava.mapper.GroupMapper;
import com.xx.jaseatschoicejava.service.GroupService;
import org.springframework.stereotype.Service;

/**
 * 群信息Service实现类
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {
    // 继承ServiceImpl后已经包含了IService接口中定义的所有方法实现
    // 可以在这里添加自定义的业务方法实现
}
