import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/css/styles.less'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import pinia from './store' // 引入 Pinia 实例

const app = createApp(App)

// Register all Element Plus icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用 Pinia
app.use(pinia)

// 禁用所有Element Plus相关的控制台警告，仅保留页面上的表单验证提示
const originalWarn = console.warn;
console.warn = function (warning, ...args) {
  // 检查是否是Element Plus相关的警告
  const isElementPlusWarning =
    // 情况1: ElementPlusError实例 (所有Element Plus错误警告)
    (warning instanceof Error && (warning.name === 'ElementPlusError' || warning.message.includes('ElementPlus'))) ||
    // 情况2: 字符串警告包含Element Plus关键词
    (typeof warning === 'string' && (warning.includes('ElementPlus') || warning.includes('Unexpected mutation of'))) ||
    // 情况3: 对象类型的表单验证警告
    (typeof warning === 'object' && warning !== null && JSON.stringify(warning).match(/(phone|password|captcha): Array/));

  // 仅显示非Element Plus的警告
  if (!isElementPlusWarning) {
    originalWarn.apply(console, arguments);
  }
};

app.use(ElementPlus, {
  size: 'small',
  zIndex: 3000
}).use(router).mount('#app')
