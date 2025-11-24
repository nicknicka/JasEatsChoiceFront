<template>
  <view class="address-container">
    <view class="address-list">
      <view class="address-item" v-for="address in addresses" :key="address.id">
        <view class="address-header">
          <view class="address-name">{{ address.name }}</view>
          <view class="address-phone">{{ address.phone }}</view>
        </view>
        <view class="address-info">
          {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
        </view>
        <view class="address-actions">
          <view class="action-btn edit" @click="editAddress(address)">编辑</view>
          <view class="action-btn delete" @click="deleteAddress(address.id)">删除</view>
          <view class="default-tag" v-if="address.isDefault">默认</view>
        </view>
      </view>
    </view>

    <button class="add-address-btn" @click="addAddress">
      + 添加新地址
    </button>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const addresses = ref([
  {
    id: 1,
    name: '张三',
    phone: '13800138000',
    province: '北京市',
    city: '北京市',
    district: '朝阳区',
    detail: 'XX路XX小区XX号楼XX室',
    isDefault: true
  },
  {
    id: 2,
    name: '张三',
    phone: '13800138000',
    province: '上海市',
    city: '上海市',
    district: '浦东新区',
    detail: 'XX路XX小区XX号楼XX室',
    isDefault: false
  }
])

const addAddress = () => {
  uni.showToast({
    title: '添加地址开发中',
    icon: 'none'
  })
}

const editAddress = (address) => {
  uni.showToast({
    title: '编辑地址开发中',
    icon: 'none'
  })
}

const deleteAddress = (id) => {
  uni.showModal({
    title: '删除地址',
    content: '确定要删除此地址吗？',
    success: (res) => {
      if (res.confirm) {
        addresses.value = addresses.value.filter(item => item.id !== id)
        uni.showToast({
          title: '地址已删除',
          icon: 'success'
        })
      }
    }
  })
}
</script>

<style scoped>
.address-container {
  background-color: #f5f5f5;
  min-height: 100vh;
  padding: 15px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 80px;
}

.address-item {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  position: relative;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.address-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.address-phone {
  font-size: 14px;
  color: #666;
}

.address-info {
  font-size: 14px;
  color: #999;
  line-height: 1.4;
  margin-bottom: 15px;
}

.address-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.action-btn {
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.action-btn.edit {
  background-color: #E3F2FD;
  color: #1E88E5;
}

.action-btn.delete {
  background-color: #FFEBEE;
  color: #E53935;
}

.default-tag {
  margin-left: auto;
  font-size: 12px;
  color: #FF6B35;
  padding: 2px 8px;
  border: 1px solid #FF6B35;
  border-radius: 12px;
}

.add-address-btn {
  position: fixed;
  bottom: 20px;
  left: 20px;
  right: 20px;
  height: 50px;
  background-color: #FF6B35;
  border: none;
  border-radius: 25px;
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}
</style>