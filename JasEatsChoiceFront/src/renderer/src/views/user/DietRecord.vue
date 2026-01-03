<template>
  <div class="diet-record-container">
    <!-- 顶部日历选择区域 -->
    <div class="calendar-section">
      <div class="section-header">
        <div class="section-title">
          <el-icon class="title-icon"><CalendarIcon /></el-icon>
          <span>饮食记录</span>
        </div>
        <div class="calendar-control">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期"
            format="YYYY年MM月DD日"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
            size="large"
            class="calendar-picker"
            popper-class="custom-date-picker"
          >
            <template #suffix-icon>
              <el-icon class="calendar-suffix-icon"><CalendarIcon /></el-icon>
            </template>
          </el-date-picker>
          <el-button type="primary" size="small" class="add-btn" @click="openAddRecordDialog">
            <el-icon><Plus /></el-icon>
            添加记录
          </el-button>
        </div>

        <!-- 添加记录弹窗 -->
        <el-dialog
          title="添加饮食记录"
          v-model="addRecordDialogVisible"
          width="720px"
          top="8%"
          transition="dialog-fade"
          class="add-diet-dialog"
        >
          <el-form ref="addRecordFormRef" :model="addRecordForm" label-width="120px">
            <!-- 餐次选择 -->
            <el-row justify="center">
              <el-col :xs="24" :sm="20">
                <el-form-item label="餐次" required>
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><ListIcon /></el-icon>
                      <span>餐&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;次</span>
                    </div>
                  </template>
                  <el-select
                    v-model="addRecordForm.mealType"
                    placeholder="请选择餐次"
                    size="large"
                  >
                    <el-option
                      v-for="mealOption in mealTypeOptions"
                      :key="mealOption.value"
                      :label="mealOption.label"
                      :value="mealOption.value"
                    >
                      <template #default>
                        <div class="select-option">
                          <el-icon v-if="mealOption.value === 'breakfast'"><Sunrise /></el-icon>
                          <el-icon v-else-if="mealOption.value === 'lunch'"><Sunny /></el-icon>
                          <el-icon v-else-if="mealOption.value === 'dinner'"><Moon /></el-icon>
                          <el-icon v-else-if="mealOption.value === 'snack'"><Coffee /></el-icon>
                          <span>{{ mealOption.label }}</span>
                        </div>
                      </template>
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 时间选择 -->
            <el-row justify="center">
              <el-col :xs="24" :sm="20">
                <el-form-item label="时间" required>
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><ClockIcon /></el-icon>
                      <span>时&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;间</span>
                    </div>
                  </template>
                  <el-time-picker
                    v-model="addRecordForm.time"
                    type="time"
                    placeholder="选择时间"
                    format="HH:mm"
                    value-format="HH:mm"
                    style="width: 100%"
                    size="large"
                  >
                  </el-time-picker>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 食物名称 -->
            <el-row justify="center">
              <el-col :xs="24" :sm="20">
                <el-form-item label="食物名称" required>
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><ForkSpoonIcon /></el-icon>
                      <span>食物名称</span>
                    </div>
                  </template>
                  <el-input
                    v-model="addRecordForm.foodName"
                    placeholder="请输入食物名称"
                    size="large"
                    @input="handleFoodNameInput"
                  >
                    <template #prefix-icon>
                      <el-icon class="input-prefix-icon"><ForkSpoonIcon /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 营养成分输入区 -->
            <el-row justify="center" :gutter="20">
              <el-col :xs="24" :sm="10">
                <!-- 卡路里输入 -->
                <el-form-item label="卡路里" required>
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><ScaleIcon /></el-icon>
                      <span>卡&nbsp;路&nbsp;里</span>
                    </div>
                  </template>
                  <el-input-number
                    v-model="addRecordForm.calories"
                    :min="0"
                    :step="10"
                    placeholder="请输入卡路里"
                    size="large"
                    style="width: 100%"
                  >
                    <template #increase-icon>
                      <el-icon class="input-number-icon"><Plus /></el-icon>
                    </template>
                    <template #decrease-icon>
                      <el-icon class="input-number-icon"><Minus /></el-icon>
                    </template>
                  </el-input-number>
                </el-form-item>
              </el-col>

              <el-col :xs="24" :sm="10">
                <!-- 蛋白质输入 -->
                <el-form-item label="蛋白质(g)">
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><MilkTea /></el-icon>
                      <span>蛋&nbsp;白&nbsp;质</span>
                    </div>
                  </template>
                  <el-input-number
                    v-model="addRecordForm.protein"
                    :min="0"
                    :step="0.1"
                    placeholder="请输入蛋白质含量"
                    size="large"
                    style="width: 100%"
                  >
                    <template #increase-icon>
                      <el-icon class="input-number-icon"><Plus /></el-icon>
                    </template>
                    <template #decrease-icon>
                      <el-icon class="input-number-icon"><Minus /></el-icon>
                    </template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row justify="center" :gutter="20" style="margin-top: 20px;">
              <el-col :xs="24" :sm="10">
                <!-- 脂肪输入 -->
                <el-form-item label="脂肪(g)">
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><Timer /></el-icon>
                      <span>脂&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;肪</span>
                    </div>
                  </template>
                  <el-input-number
                    v-model="addRecordForm.fat"
                    :min="0"
                    :step="0.1"
                    placeholder="请输入脂肪含量"
                    size="large"
                    style="width: 100%"
                  >
                    <template #increase-icon>
                      <el-icon class="input-number-icon"><Plus /></el-icon>
                    </template>
                    <template #decrease-icon>
                      <el-icon class="input-number-icon"><Minus /></el-icon>
                    </template>
                  </el-input-number>
                </el-form-item>
              </el-col>

              <el-col :xs="24" :sm="10">
                <!-- 碳水化合物输入 -->
                <el-form-item label="碳水化合物(g)">
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><Food /></el-icon>
                      <span>碳&nbsp;水&nbsp;化&nbsp;合&nbsp;物</span>
                    </div>
                  </template>
                  <el-input-number
                    v-model="addRecordForm.carbohydrate"
                    :min="0"
                    :step="0.1"
                    placeholder="请输入碳水化合物含量"
                    size="large"
                    style="width: 100%"
                  >
                    <template #increase-icon>
                      <el-icon class="input-number-icon"><Plus /></el-icon>
                    </template>
                    <template #decrease-icon>
                      <el-icon class="input-number-icon"><Minus /></el-icon>
                    </template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 描述 -->
            <el-row justify="center">
              <el-col :xs="24" :sm="20">
                <el-form-item label="描述">
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><DocumentIcon /></el-icon>
                      <span>描&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;述</span>
                    </div>
                  </template>
                  <el-input
                    v-model="addRecordForm.description"
                    type="textarea"
                    placeholder="请输入描述（如：份量、做法等）"
                    :rows="4"
                    size="large"
                  >
                    <template #prefix-icon>
                      <el-icon class="input-prefix-icon"><DocumentIcon /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <template #footer>
            <div class="dialog-footer">
              <el-button size="large" @click="closeAddRecordDialog">取消</el-button>
              <el-button
                type="primary"
                size="large"
                @click="submitAddRecordForm"
                :loading="submitLoading"
              >
                <el-icon><Check /></el-icon>
                确认添加
              </el-button>
            </div>
          </template>
        </el-dialog>

        <!-- 编辑记录弹窗 -->
        <el-dialog
          title="编辑饮食记录"
          v-model="editRecordDialogVisible"
          width="720px"
          top="8%"
          transition="dialog-fade"
          class="add-diet-dialog"
        >
          <el-form ref="editRecordFormRef" :model="editRecordForm" label-width="120px">
            <!-- 餐次选择 -->
            <el-row justify="center">
              <el-col :xs="24" :sm="20">
                <el-form-item label="餐次" required>
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><ListIcon /></el-icon>
                      <span>餐&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;次</span>
                    </div>
                  </template>
                  <el-select v-model="editRecordForm.mealType" placeholder="请选择餐次" size="large">
                    <el-option
                      v-for="mealOption in mealTypeOptions"
                      :key="mealOption.value"
                      :label="mealOption.label"
                      :value="mealOption.value"
                    >
                      <template #default>
                        <div class="select-option">
                          <el-icon v-if="mealOption.value === 'breakfast'"><Sunrise /></el-icon>
                          <el-icon v-else-if="mealOption.value === 'lunch'"><Sunny /></el-icon>
                          <el-icon v-else-if="mealOption.value === 'dinner'"><Moon /></el-icon>
                          <el-icon v-else-if="mealOption.value === 'snack'"><Coffee /></el-icon>
                          <span>{{ mealOption.label }}</span>
                        </div>
                      </template>
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 时间选择 -->
            <el-row justify="center">
              <el-col :xs="24" :sm="20">
                <el-form-item label="时间" required>
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><ClockIcon /></el-icon>
                      <span>时&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;间</span>
                    </div>
                  </template>
                  <el-time-picker
                    v-model="editRecordForm.time"
                    type="time"
                    placeholder="选择时间"
                    format="HH:mm"
                    value-format="HH:mm"
                    style="width: 100%"
                    size="large"
                  >
                  </el-time-picker>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 食物名称 -->
            <el-row justify="center">
              <el-col :xs="24" :sm="20">
                <el-form-item label="食物名称" required>
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><ForkSpoonIcon /></el-icon>
                      <span>食物名称</span>
                    </div>
                  </template>
                  <el-input
                    v-model="editRecordForm.foodName"
                    placeholder="请输入食物名称"
                    size="large"
                  >
                    <template #prefix-icon>
                      <el-icon class="input-prefix-icon"><ForkSpoonIcon /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 营养成分输入区 -->
            <el-row justify="center" :gutter="20">
              <el-col :xs="24" :sm="10">
                <el-form-item label="卡路里" required>
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><ScaleIcon /></el-icon>
                      <span>卡&nbsp;路&nbsp;里</span>
                    </div>
                  </template>
                  <el-input-number
                    v-model="editRecordForm.calories"
                    :min="0"
                    :step="10"
                    placeholder="请输入卡路里"
                    size="large"
                    style="width: 100%"
                  >
                    <template #increase-icon>
                      <el-icon class="input-number-icon"><Plus /></el-icon>
                    </template>
                    <template #decrease-icon>
                      <el-icon class="input-number-icon"><Minus /></el-icon>
                    </template>
                  </el-input-number>
                </el-form-item>
              </el-col>

              <el-col :xs="24" :sm="10">
                <el-form-item label="蛋白质(g)">
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><MilkTea /></el-icon>
                      <span>蛋&nbsp;白&nbsp;质</span>
                    </div>
                  </template>
                  <el-input-number
                    v-model="editRecordForm.protein"
                    :min="0"
                    :step="0.1"
                    placeholder="请输入蛋白质含量"
                    size="large"
                    style="width: 100%"
                  >
                    <template #increase-icon>
                      <el-icon class="input-number-icon"><Plus /></el-icon>
                    </template>
                    <template #decrease-icon>
                      <el-icon class="input-number-icon"><Minus /></el-icon>
                    </template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row justify="center" :gutter="20" style="margin-top: 20px;">
              <el-col :xs="24" :sm="10">
                <el-form-item label="脂肪(g)">
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><Timer /></el-icon>
                      <span>脂&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;肪</span>
                    </div>
                  </template>
                  <el-input-number
                    v-model="editRecordForm.fat"
                    :min="0"
                    :step="0.1"
                    placeholder="请输入脂肪含量"
                    size="large"
                    style="width: 100%"
                  >
                    <template #increase-icon>
                      <el-icon class="input-number-icon"><Plus /></el-icon>
                    </template>
                    <template #decrease-icon>
                      <el-icon class="input-number-icon"><Minus /></el-icon>
                    </template>
                  </el-input-number>
                </el-form-item>
              </el-col>

              <el-col :xs="24" :sm="10">
                <el-form-item label="碳水化合物(g)">
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><Food /></el-icon>
                      <span>碳&nbsp;水&nbsp;化&nbsp;合&nbsp;物</span>
                    </div>
                  </template>
                  <el-input-number
                    v-model="editRecordForm.carbohydrate"
                    :min="0"
                    :step="0.1"
                    placeholder="请输入碳水化合物含量"
                    size="large"
                    style="width: 100%"
                  >
                    <template #increase-icon>
                      <el-icon class="input-number-icon"><Plus /></el-icon>
                    </template>
                    <template #decrease-icon>
                      <el-icon class="input-number-icon"><Minus /></el-icon>
                    </template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 描述 -->
            <el-row justify="center">
              <el-col :xs="24" :sm="20">
                <el-form-item label="描述">
                  <template #label>
                    <div class="form-item-label">
                      <el-icon class="label-icon"><DocumentIcon /></el-icon>
                      <span>描&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;述</span>
                    </div>
                  </template>
                  <el-input
                    v-model="editRecordForm.description"
                    type="textarea"
                    placeholder="请输入描述（如：份量、做法等）"
                    :rows="4"
                    size="large"
                  >
                    <template #prefix-icon>
                      <el-icon class="input-prefix-icon"><DocumentIcon /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <template #footer>
            <div class="dialog-footer">
              <el-button size="large" @click="closeEditRecordDialog">取消</el-button>
              <el-button
                type="primary"
                size="large"
                @click="submitEditRecordForm"
                :loading="submitLoading"
              >
                <el-icon><Check /></el-icon>
                保存修改
              </el-button>
            </div>
          </template>
        </el-dialog>

        <!-- 删除确认弹窗 -->
        <el-dialog title="删除确认" v-model="deleteConfirmVisible" width="360px" transition="dialog-fade">
          <div>您确定要删除这条饮食记录吗？</div>
          <template #footer>
            <div class="dialog-footer">
              <el-button @click="deleteConfirmVisible = false">取消</el-button>
              <el-button type="danger" @click="submitDeleteRecord">确定删除</el-button>
            </div>
          </template>
        </el-dialog>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-card">
      <div class="stat-item">
        <div class="stat-number">{{ totalCalories }}</div>
        <div class="stat-label">总卡路里</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-number">{{ dietRecords.length }}</div>
        <div class="stat-label">记录条数</div>
      </div>
    </div>

    <!-- 饮食记录显示区域 -->
    <div class="records-section">
      <div class="meal-sections">
        <!-- 早餐 -->
        <div v-if="getMealsByType('breakfast').length > 0" class="meal-section">
          <div
            class="meal-section-header breakfast"
            @click="toggleSection('breakfast')"
            style="cursor: pointer"
          >
            <el-icon class="meal-icon"><Sunrise /> </el-icon>
            <span class="meal-section-title">早餐</span>
            <el-tag size="small" type="success" class="meal-count">
              {{ getMealsByType('breakfast').length }}
            </el-tag>
            <el-icon :class="{ 'rotate-180': !expandedSections.breakfast }" class="arrow-icon">
              <ArrowDown />
            </el-icon>
          </div>
          <Transition name="collapse">
            <div v-show="expandedSections.breakfast" class="meal-records">
              <DietRecordCard
                v-for="record in getMealsByType('breakfast')"
                :key="record.id"
                :record="record"
                @edit="openEditRecordDialog"
                @delete="openDeleteConfirm"
              />
            </div>
          </Transition>
        </div>

        <!-- 午餐 -->
        <div v-if="getMealsByType('lunch').length > 0" class="meal-section">
          <div
            class="meal-section-header lunch"
            @click="toggleSection('lunch')"
            style="cursor: pointer"
          >
            <el-icon class="meal-icon"><Sunny /> </el-icon>
            <span class="meal-section-title">午餐</span>
            <el-tag size="small" type="warning" class="meal-count">
              {{ getMealsByType('lunch').length }}
            </el-tag>
            <el-icon :class="{ 'rotate-180': !expandedSections.lunch }" class="arrow-icon">
              <ArrowDown />
            </el-icon>
          </div>
          <Transition name="collapse">
            <div v-show="expandedSections.lunch" class="meal-records">
              <DietRecordCard
                v-for="record in getMealsByType('lunch')"
                :key="record.id"
                :record="record"
                @edit="openEditRecordDialog"
                @delete="openDeleteConfirm"
              />
            </div>
          </Transition>
        </div>

        <!-- 晚餐 -->
        <div v-if="getMealsByType('dinner').length > 0" class="meal-section">
          <div
            class="meal-section-header dinner"
            @click="toggleSection('dinner')"
            style="cursor: pointer"
          >
            <el-icon class="meal-icon"><Moon /> </el-icon>
            <span class="meal-section-title">晚餐</span>
            <el-tag size="small" type="danger" class="meal-count">
              {{ getMealsByType('dinner').length }}
            </el-tag>
            <el-icon :class="{ 'rotate-180': !expandedSections.dinner }" class="arrow-icon">
              <ArrowDown />
            </el-icon>
          </div>
          <Transition name="collapse">
            <div v-show="expandedSections.dinner" class="meal-records">
              <DietRecordCard
                v-for="record in getMealsByType('dinner')"
                :key="record.id"
                :record="record"
                @edit="openEditRecordDialog"
                @delete="openDeleteConfirm"
              />
            </div>
          </Transition>
        </div>

        <!-- 加餐 -->
        <div v-if="getMealsByType('snack').length > 0" class="meal-section">
          <div
            class="meal-section-header snack"
            @click="toggleSection('snack')"
            style="cursor: pointer"
          >
            <el-icon class="meal-icon"><Coffee /> </el-icon>
            <span class="meal-section-title">加餐</span>
            <el-tag size="small" type="info" class="meal-count">
              {{ getMealsByType('snack').length }}
            </el-tag>
            <el-icon :class="{ 'rotate-180': !expandedSections.snack }" class="arrow-icon">
              <ArrowDown />
            </el-icon>
          </div>
          <Transition name="collapse">
            <div v-show="expandedSections.snack" class="meal-records">
              <DietRecordCard
                v-for="record in getMealsByType('snack')"
                :key="record.id"
                :record="record"
                @edit="openEditRecordDialog"
                @delete="openDeleteConfirm"
              />
            </div>
          </Transition>
        </div>
      </div>

      <!-- 空数据提示 -->
      <div v-if="dietRecords.length === 0" class="empty-records">
        <el-empty
          image="https://cdn-icons-png.flaticon.com/128/4385/4385277.png"
          description="暂无饮食记录，点击右上角添加按钮开始记录吧"
        >
          <template #bottom>
            <el-button type="primary" class="add-empty-btn">
              <el-icon><Plus /></el-icon>
              添加第一条记录
            </el-button>
          </template>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  Calendar as CalendarIcon,
  Plus,
  Minus,
  Check,
  Sunrise,
  Sunny,
  Moon,
  Coffee,
  ArrowDown,
  List as ListIcon,
  Clock as ClockIcon,
  ForkSpoon as ForkSpoonIcon,
  DataAnalysis as ScaleIcon,
  Document as DocumentIcon,
  MilkTea,
  Timer,
  Food
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
// 导入新组件和工具函数
import DietRecordCard from '../../components/DietRecordCard.vue'
import { mealTypeToChinese, mealTypeToEnglish, mealTypeOptions } from '../../utils/mealTypeUtils.js'

// 展开/折叠状态
const expandedSections = ref({
  breakfast: true,
  lunch: true,
  dinner: true,
  snack: true
})

// 切换展开/折叠
const toggleSection = (section) => {
  expandedSections.value[section] = !expandedSections.value[section]
}

// 日历选择的日期，默认是今天
const selectedDate = ref(new Date().toISOString().split('T')[0])

// 饮食记录数据
const dietRecords = ref([])

// 记录悬停状态已移至DietRecordCard组件

// 计算总卡路里
const totalCalories = ref(0)

// 根据餐食类型筛选记录
const getMealsByType = (mealType) => {
  return dietRecords.value.filter((record) => record.mealType === mealType)
}

// 更新总卡路里
const updateTotalCalories = () => {
  totalCalories.value = dietRecords.value.reduce(
    (total, record) => total + (record.calories || 0),
    0
  )
}

// 从后端获取饮食记录
const fetchDietRecords = async (date) => {
  try {
    // 这里需要获取当前登录用户的ID，假设已经存储在localStorage中
    const userInfo = JSON.parse(localStorage.getItem('userInfo'))
    if (!userInfo || !userInfo.userId) {
      // 如果没有用户信息，显示空数据状态
      dietRecords.value = []
      totalCalories.value = 0
      return
    }

    // 格式化日期为YYYY-MM-DD格式
    const formattedDate = date

    // 调用后端API
    const apiUrl = API_CONFIG.diet.date.replace('{userId}', userInfo.userId) + formattedDate
    const response = await api.get(apiUrl)

    // 将后端返回的数据转换为前端需要的格式
    if (response && response.data) {
      const backendRecords = response.data

      // 转换数据格式
      dietRecords.value = backendRecords.map((record) => ({
        id: record.id,
        mealType: mealTypeToEnglish(record.mealTime),
        mealTypeName: record.mealTime,
        time: record.recordTime ? record.recordTime.split('T')[1].substring(0, 5) : '',
        foodName: record.foodName || '暂未定义食物名称', // 使用直接存储的食物名称
        calories: record.calorie,
        protein: record.protein || 0,
        fat: record.fat || 0,
        carbohydrate: record.carbohydrate || 0,
        description: record.description || '' // 使用直接存储的食物描述
      }))
    }

    // console.log('获取的饮食记录数据:', dietRecords.value)
    // 更新总卡路里
    updateTotalCalories()
  } catch (error) {
    console.error('获取饮食记录失败:', error)
    ElMessage.error('获取饮食记录失败，请稍后重试')
    // 失败时清空数据，显示空数据状态
    dietRecords.value = []
    totalCalories.value = 0
  }
}

// 处理日期变化
const handleDateChange = (date) => {
  selectedDate.value = date
  // 根据选择的日期加载对应的饮食记录数据
  fetchDietRecords(date)
}

// 添加记录弹窗可见性
const addRecordDialogVisible = ref(false)

// 添加记录表单引用
const addRecordFormRef = ref(null)

// 提交加载状态
const submitLoading = ref(false)

// 添加记录表单数据
const addRecordForm = ref({
  mealType: 'breakfast',
  time: '',
  foodName: '',
  calories: 0,
  protein: 0,
  fat: 0,
  carbohydrate: 0,
  description: ''
})

// 食物名称输入处理（示例：可以添加自动补全逻辑）
const handleFoodNameInput = (value) => {
  // 这里可以添加食物名称自动补全逻辑
}

// 编辑记录弹窗可见性
const editRecordDialogVisible = ref(false)

// 编辑记录表单引用
const editRecordFormRef = ref(null)

// 编辑记录表单数据
const editRecordForm = ref({
  id: '',
  mealType: 'breakfast',
  time: '',
  foodName: '',
  calories: 0,
  protein: 0,
  fat: 0,
  carbohydrate: 0,
  description: ''
})

// 删除确认弹窗可见性
const deleteConfirmVisible = ref(false)

// 当前要删除的记录ID
const currentDeleteId = ref('')


// 打开添加记录弹窗
const openAddRecordDialog = () => {
  addRecordDialogVisible.value = true
  // 重置表单并设置时间默认值为当前系统时间
  const currentTime = new Date().toTimeString().slice(0, 5);
  addRecordForm.value = {
    mealType: 'breakfast',
    time: currentTime,
    foodName: '',
    calories: 0,
    protein: 0,
    fat: 0,
    carbohydrate: 0,
    description: ''
  }
}

// 关闭添加记录弹窗
const closeAddRecordDialog = () => {
  addRecordDialogVisible.value = false
}

// 关闭编辑记录弹窗
const closeEditRecordDialog = () => {
  editRecordDialogVisible.value = false
}

// 提交添加记录表单
const submitAddRecordForm = async () => {
  try {
    submitLoading.value = true
    // 表单验证
    if (!addRecordFormRef.value) return
    await addRecordFormRef.value.validate()

    // 这里需要获取当前登录用户的ID，假设已经存储在localStorage中
    const userInfo = JSON.parse(localStorage.getItem('userInfo'))
    if (!userInfo || !userInfo.userId) {
      ElMessage.error('未找到用户信息，请先登录')
      return
    }

    // 构建请求数据
    // 合并日期和时间为时间字符串格式，避免时区转换
    const recordTime = `${selectedDate.value}T${addRecordForm.value.time}:00`

    const requestData = {
      userId: userInfo.userId,
      mealTime: mealTypeToChinese(addRecordForm.value.mealType),
      foodName: addRecordForm.value.foodName,
      calorie: addRecordForm.value.calories, // 注意后端字段是单数形式
      protein: addRecordForm.value.protein,
      fat: addRecordForm.value.fat,
      carbohydrate: addRecordForm.value.carbohydrate,
      description: addRecordForm.value.description,
      recordTime: recordTime
    }

    // 调用后端API添加记录
    await api.post(API_CONFIG.diet.add, requestData)

    // 添加成功后，关闭弹窗并刷新记录
    closeAddRecordDialog()
    fetchDietRecords(selectedDate.value)
  } catch (error) {
    console.error('添加记录失败:', error)
    ElMessage.error('添加记录失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

// 打开编辑记录弹窗并填充数据
const openEditRecordDialog = (record) => {
  editRecordDialogVisible.value = true
  // 填充编辑表单数据
  editRecordForm.value = {
    id: record.id,
    mealType: record.mealType,
    time: record.time,
    foodName: record.foodName,
    calories: record.calories,
    protein: record.protein,
    fat: record.fat,
    carbohydrate: record.carbohydrate,
    description: record.description || ''
  }
}

// 提交编辑记录表单
const submitEditRecordForm = async () => {
  try {
    submitLoading.value = true
    // 表单验证
    if (!editRecordFormRef.value) return
    await editRecordFormRef.value.validate()

    // 这里需要获取当前登录用户的ID，假设已经存储在localStorage中
    const userInfo = JSON.parse(localStorage.getItem('userInfo'))
    if (!userInfo || !userInfo.userId) {
      ElMessage.error('未找到用户信息，请先登录')
      return
    }

    // 构建请求数据
    // 合并日期和时间为时间字符串格式，避免时区转换
    const recordTime = `${selectedDate.value}T${editRecordForm.value.time}:00`

    const requestData = {
      id: editRecordForm.value.id, // 直接作为字符串处理，不需要转换为数字
      userId: Number(userInfo.userId), // 确保userId是数字类型
      mealTime: mealTypeToChinese(editRecordForm.value.mealType),
      foodName: editRecordForm.value.foodName,
      calorie: editRecordForm.value.calories, // 注意后端字段是单数形式
      protein: editRecordForm.value.protein,
      fat: editRecordForm.value.fat,
      carbohydrate: editRecordForm.value.carbohydrate,
      description: editRecordForm.value.description,
      recordTime: recordTime
    }

    // 调用后端API编辑记录
    // console.log('发送的请求数据:', requestData) // 调试
    const response = await api.put(API_CONFIG.diet.update, requestData)
    // console.log('编辑记录响应数据:', response)
    // 编辑成功后，关闭弹窗并刷新记录
    closeEditRecordDialog()
    fetchDietRecords(selectedDate.value)
  } catch (error) {
    console.error('编辑记录失败:', error)
    ElMessage.error('编辑记录失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

// 打开删除确认弹窗
const openDeleteConfirm = (record) => {
  currentDeleteId.value = record.id
  deleteConfirmVisible.value = true
}

// 提交删除记录
const submitDeleteRecord = async () => {
  try {
    if (!currentDeleteId.value) return

    // 这里需要获取当前登录用户的ID，假设已经存储在localStorage中
    const userInfo = JSON.parse(localStorage.getItem('userInfo'))
    if (!userInfo || !userInfo.userId) {
      ElMessage.error('未找到用户信息，请先登录')
      return
    }

    // 调用后端API删除记录
    await api.delete(API_CONFIG.diet.delete.replace('{id}', currentDeleteId.value))

    // 删除成功后，关闭弹窗并刷新记录
    deleteConfirmVisible.value = false
    fetchDietRecords(selectedDate.value)
  } catch (error) {
    console.error('删除记录失败:', error)
    ElMessage.error('删除记录失败，请稍后重试')
  }
}

// 页面加载时初始化数据
onMounted(() => {
  // 加载默认日期的饮食记录数据
  fetchDietRecords(selectedDate.value)
})
</script>

<style scoped>
.diet-record-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 0;
}

/* 顶部日历区域 */
.calendar-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 700;
  color: white;
}

.title-icon {
  font-size: 32px;
}

.calendar-control {
  display: flex;
  align-items: center;
  gap: 12px;
}

.calendar-picker {
  background-color: white;
  border-radius: 8px;
  padding: 4px;
}

.calendar-suffix-icon {
  color: #667eea;
}

.add-btn {
  border-radius: 24px !important;
  padding: 10px 24px !important;
  font-weight: 600 !important;
  background: white;
  color: #667eea;
  border: 2px solid white;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 255, 255, 0.3);
  background: white;
  color: #667eea;
}

/* 统计卡片 */
.stats-card {
  display: flex;
  align-items: center;
  gap: 32px;
  background-color: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #999;
  font-weight: 500;
}

.stat-divider {
  width: 2px;
  height: 48px;
  background-color: #e8e8e8;
}

/* 记录区域 */
.records-section {
  flex: 1;
  background-color: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  overflow-y: auto;
}

.meal-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 餐食分类标题 */
.meal-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meal-section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.meal-section-header.breakfast {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.meal-section-header.lunch {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.meal-section-header.dinner {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.meal-section-header.snack {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.meal-icon {
  font-size: 24px;
}

.meal-count {
  margin-left: auto;
}

/* 箭头图标样式 */
.arrow-icon {
  transition: transform 0.3s ease;
  font-size: 16px;
  margin-left: 8px;
}

/* 折叠动画样式 */
.collapse-enter-active,
.collapse-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.collapse-enter-from,
.collapse-leave-to {
  max-height: 0;
  opacity: 0;
  margin: 0;
}

/* 记录卡片 */
.meal-records {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 8px;
}

.record-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background-color: #f9fafb;
  border-radius: 12px;
  border-left: 4px solid #e5e7eb;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.record-card:hover {
  background-color: white;
  border-left-color: #667eea;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.record-info {
  flex: 1;
  min-width: 0;
}

.record-time {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
  margin-bottom: 4px;
}

.food-name {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.food-description {
  font-size: 14px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-calories {
  text-align: right;
  margin-left: 24px;
}

/* 记录操作按钮 */
.record-actions {
  display: flex;
  gap: 8px;
  margin-left: 24px;
  flex-shrink: 0;
}

/* 弹窗动画 */
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.dialog-fade-enter-from {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

.dialog-fade-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* 弹窗滑入动画 */
.dialog-slide-up-enter-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  transform-origin: center bottom;
}

.dialog-slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  transform-origin: center top;
}

.dialog-slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.dialog-slide-up-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

/* 自定义Dialog样式 */
:deep(.el-dialog__header) {
  border-bottom: 2px solid rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  padding: 24px 28px;
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:deep(.el-dialog__body) {
  padding: 32px 28px;
}

/* 表单字段样式 */
:deep(.el-form-item) {
  margin-bottom: 20px; /* 调整为与其他部分一致的间距 */
  align-items: center; /* 垂直居中对齐 */
}

/* 选择器选项样式 */
.select-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 输入框前缀图标 */
.input-prefix-icon {
  color: #667eea;
  font-size: 18px;
}

/* 数字输入框图标 */
.input-number-icon {
  font-size: 14px;
  font-weight: bold;
}

/* 自定义弹窗样式 */
.add-diet-dialog :deep(.el-dialog__body) {
  padding: 36px 32px;
}

/* 大型按钮样式增强 */
.add-diet-dialog :deep(.el-button--large) {
  padding: 12px 40px;
  font-size: 16px;
}

/* 表单输入框增强 */
.add-diet-dialog :deep(.el-input--large .el-input__wrapper),
.add-diet-dialog :deep(.el-select--large .el-select__wrapper),
.add-diet-dialog :deep(.el-textarea--large .el-textarea__inner),
.add-diet-dialog :deep(.el-time-picker--large .el-time-picker__input-inner),
.add-diet-dialog :deep(.el-input-number--large .el-input-number__input) {
  height: 50px;
  font-size: 16px;
  line-height: 24px;
}

/* 带图标的标签样式 */
.form-item-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label-icon {
  font-size: 18px;
  color: #667eea;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #555;
  font-size: 14px;
}

/* 输入框样式 */
:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__inner),
:deep(.el-time-picker__input-inner),
:deep(.el-input-number__input) {
  border-radius: 8px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-textarea__inner:hover),
:deep(.el-time-picker__input-inner:hover),
:deep(.el-input-number__input:hover) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus),
:deep(.el-textarea__inner.is-focus),
:deep(.el-time-picker__input-inner.is-focus),
:deep(.el-input-number__input.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

/* 按钮样式 */
:deep(.dialog-footer) {
  text-align: center;
  padding: 0 28px 24px;
}

:deep(.dialog-footer .el-button) {
  padding: 10px 28px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.dialog-footer .el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.dialog-footer .el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

:deep(.dialog-footer .el-button--default) {
  border-color: #e5e7eb;
}

:deep(.dialog-footer .el-button--default:hover) {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.calories-text {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #ef4444;
}

.calories-unit {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
}

/* 空数据 */
.empty-records {
  margin: 60px 0;
  text-align: center;
}

.add-empty-btn {
  margin-top: 24px;
  border-radius: 8px;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .calendar-section {
    padding: 20px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .section-title {
    font-size: 24px;
  }

  .stats-card {
    gap: 16px;
    padding: 16px;
  }

  .stat-number {
    font-size: 24px;
  }

  .record-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .record-calories {
    margin-left: 0;
  }
}
</style>
