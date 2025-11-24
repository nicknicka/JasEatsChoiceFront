<template>
  <div class="address-container">
    <h2>管理地址</h2>

    <el-button type="primary" style="margin-bottom: 20px;" @click="showAddDialog = true">
      <el-icon><Plus /></el-icon>
      新增地址
    </el-button>

    <div class="address-list">
      <el-card
        v-for="address in addresses"
        :key="address.id"
        class="address-card"
        :class="{ 'default-address': address.isDefault }"
      >
        <div class="address-header">
          <div class="address-name">{{ address.name }}</div>
          <div class="address-phone">{{ address.phone }}</div>
        </div>

        <div class="address-details">
          <div class="address-full">{{ address.province }} {{ address.city }} {{ address.district }} {{ address.street }}</div>
          <div class="address-tag" v-if="address.tag">{{ address.tag }}</div>
        </div>

        <div class="address-actions">
          <el-button type="text" size="small" @click="editAddress(address)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="text" size="small" danger @click="deleteAddress(address)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
          <el-button
            v-if="!address.isDefault"
            type="text" size="small"
            @click="setDefault(address)"
          >
            设置默认
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 新增地址对话框 -->
    <el-dialog title="新增地址" v-model="showAddDialog" width="50%">
      <el-form ref="addressFormRef" :model="newAddress" label-width="80px" rules="addressRules">
        <el-form-item label="收件人" prop="name">
          <el-input v-model="newAddress.name" placeholder="请输入收件人姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="newAddress.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="省" prop="province">
          <el-input v-model="newAddress.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="市" prop="city">
          <el-input v-model="newAddress.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区" prop="district">
          <el-input v-model="newAddress.district" placeholder="请输入区/县" />
        </el-form-item>
        <el-form-item label="街道" prop="street">
          <el-input v-model="newAddress.street" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="newAddress.tag" placeholder="选择地址标签">
            <el-option label="家" value="家" />
            <el-option label="公司" value="公司" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="addAddress">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑地址对话框 -->
    <el-dialog title="编辑地址" v-model="showEditDialog" width="50%">
      <el-form ref="editFormRef" :model="editingAddress" label-width="80px" rules="addressRules">
        <el-form-item label="收件人" prop="name">
          <el-input v-model="editingAddress.name" placeholder="请输入收件人姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="editingAddress.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="省" prop="province">
          <el-input v-model="editingAddress.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="市" prop="city">
          <el-input v-model="editingAddress.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区" prop="district">
          <el-input v-model="editingAddress.district" placeholder="请输入区/县" />
        </el-form-item>
        <el-form-item label="街道" prop="street">
          <el-input v-model="editingAddress.street" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="editingAddress.tag" placeholder="选择地址标签">
            <el-option label="家" value="家" />
            <el-option label="公司" value="公司" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="updateAddress">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { Edit, Delete, Plus } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

// 地址列表
const addresses = ref([]);

// 新增地址对话框
const showAddDialog = ref(false);

// 编辑地址对话框
const showEditDialog = ref(false);

// 新增地址表单数据
const newAddress = ref({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  street: '',
  tag: '',
  isDefault: false
});

// 编辑地址表单数据
const editingAddress = ref({});

// 地址验证规则
const addressRules = ref({
  name: [{ required: true, message: '请输入收件人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区/县', trigger: 'blur' }],
  street: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
});

// 从本地存储加载地址数据
onMounted(() => {
  const savedAddresses = localStorage.getItem('userAddresses');
  if (savedAddresses) {
    addresses.value = JSON.parse(savedAddresses);
  } else {
    // 默认地址数据
    addresses.value = [
      {
        id: 1,
        name: '张三',
        phone: '138xxxx8888',
        province: '北京',
        city: '北京',
        district: '朝阳区',
        street: '建国路88号',
        tag: '公司',
        isDefault: true
      },
      {
        id: 2,
        name: '张三',
        phone: '138xxxx8888',
        province: '北京',
        city: '北京',
        district: '海淀区',
        street: '中关村大街1号',
        tag: '家',
        isDefault: false
      }
    ];
    // 保存到本地存储
    localStorage.setItem('userAddresses', JSON.stringify(addresses.value));
  }
});

// 保存地址数据到本地存储
const saveAddresses = () => {
  localStorage.setItem('userAddresses', JSON.stringify(addresses.value));
};

// 新增地址
const addAddress = () => {
  // 表单验证
  // 生成新地址ID
  const newId = addresses.value.length > 0 ? Math.max(...addresses.value.map(item => item.id)) + 1 : 1;
  // 创建新地址对象
  const addressToAdd = {
    ...newAddress.value,
    id: newId
  };

  // 保存到地址列表
  addresses.value.push(addressToAdd);
  // 保存到本地存储
  saveAddresses();

  // 重置表单
  newAddress.value = {
    name: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    street: '',
    tag: '',
    isDefault: false
  };

  // 关闭对话框
  showAddDialog.value = false;

  // 提示成功
  ElMessage.success('地址已新增');
};

// 编辑地址
const editAddress = (address) => {
  // 复制地址信息到编辑表单
  editingAddress.value = JSON.parse(JSON.stringify(address));
  // 显示编辑对话框
  showEditDialog.value = true;
};

// 更新地址
const updateAddress = () => {
  // 找到要更新的地址
  const index = addresses.value.findIndex(item => item.id === editingAddress.value.id);
  if (index !== -1) {
    // 更新地址信息
    addresses.value[index] = editingAddress.value;
    // 保存到本地存储
    saveAddresses();
    // 关闭对话框
    showEditDialog.value = false;
    // 提示成功
    ElMessage.success('地址已更新');
  }
};

// 删除地址
const deleteAddress = (address) => {
  // 确认删除
  // 如果是默认地址，不能删除（或取消默认后再删除）
  if (address.isDefault) {
    ElMessage.warning('默认地址不能删除');
    return;
  }

  // 找到要删除的地址索引
  const index = addresses.value.findIndex(item => item.id === address.id);
  if (index !== -1) {
    // 删除地址
    addresses.value.splice(index, 1);
    // 保存到本地存储
    saveAddresses();
    // 提示成功
    ElMessage.success('地址已删除');
  }
};

// 设置默认地址
const setDefault = (address) => {
  // 将所有地址的isDefault设为false
  addresses.value.forEach(item => {
    item.isDefault = false;
  });
  // 将当前地址设为默认
  address.isDefault = true;
  // 保存到本地存储
  saveAddresses();
  // 提示成功
  ElMessage.success('默认地址已设置');
};
</script>

<style scoped lang="less">
.address-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 24px;
    margin: 0 0 20px 0;
  }

  .address-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .address-card {
    position: relative;
    padding: 20px;

    &.default-address {
      border-left: 4px solid #409eff;

      &::before {
        content: "默认地址";
        position: absolute;
        top: 20px;
        right: 20px;
        color: #409eff;
        font-size: 14px;
      }
    }

    .address-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;

      .address-name {
        font-size: 16px;
        font-weight: bold;
      }

      .address-phone {
        color: #606266;
      }
    }

    .address-details {
      margin-bottom: 15px;

      .address-full {
        margin-bottom: 5px;
      }

      .address-tag {
        display: inline-block;
        color: #409eff;
        background-color: rgba(64, 158, 255, 0.1);
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 12px;
      }
    }

    .address-actions {
      text-align: right;
    }
  }
}
</style>
