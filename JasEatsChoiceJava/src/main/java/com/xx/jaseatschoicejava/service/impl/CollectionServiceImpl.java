package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.entity.UserCollection;
import com.xx.jaseatschoicejava.mapper.CollectionMapper;
import com.xx.jaseatschoicejava.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收藏Service实现类
 */
@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionMapper collectionMapper;

    @Override
    public List<UserCollection> getCollectionsByUserId(Long userId) {
        LambdaQueryWrapper<UserCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCollection::getUserId, userId)
                .orderByDesc(UserCollection::getCreateTime);
        return collectionMapper.selectList(queryWrapper);
    }

    @Override
    public List<UserCollection> getCollectionsByUserIdAndType(Long userId, String type) {
        LambdaQueryWrapper<UserCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCollection::getUserId, userId)
                .eq(UserCollection::getCollectableType, type)
                .orderByDesc(UserCollection::getCreateTime);
        return collectionMapper.selectList(queryWrapper);
    }

    @Override
    public Long addCollection(UserCollection collection) {
        collection.setCreateTime(LocalDateTime.now());
        collectionMapper.insert(collection);
        return collection.getId();
    }

    @Override
    public boolean removeCollection(Long userId, String collectableType, Long collectableId) {
        LambdaQueryWrapper<UserCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCollection::getUserId, userId)
                .eq(UserCollection::getCollectableType, collectableType)
                .eq(UserCollection::getCollectableId, collectableId);
        return collectionMapper.delete(queryWrapper) > 0;
    }

    @Override
    public boolean isCollected(Long userId, String collectableType, Long collectableId) {
        LambdaQueryWrapper<UserCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCollection::getUserId, userId)
                .eq(UserCollection::getCollectableType, collectableType)
                .eq(UserCollection::getCollectableId, collectableId);
        return collectionMapper.selectCount(queryWrapper) > 0;
    }
}
