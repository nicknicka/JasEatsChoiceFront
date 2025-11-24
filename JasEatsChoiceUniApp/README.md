# 佳食宜选小程序端

## 项目介绍
基于uni-app开发的佳食宜选餐饮下单系统小程序端，提供用户点单、食谱推荐、AI助手等功能。

## 技术栈
- uni-app
- Vue 3
- Vite

## 项目结构
```
├── src/
│   ├── pages/
│   │   ├── index/              # 首页
│   │   ├── recipe/             # 食谱相关
│   │   │   ├── today.vue       # 今日食谱
│   │   │   └── my.vue          # 我的食谱
│   │   ├── ai/                 # AI助手
│   │   │   └── chat.vue        # AI聊天
│   │   └── settings/           # 设置
│   │       └── index.vue       # 设置首页
│   ├── components/             # 组件
│   ├── utils/                  # 工具
│   ├── api/                    # API
│   ├── static/                 # 静态资源
│   ├── store/                  # 状态管理
│   ├── main.js                 # 入口文件
│   ├── manifest.json           # 应用配置
│   └── pages.json              # 页面配置
└── package.json
```

## 安装与运行
### 安装依赖
```bash
npm install
```

### 微信小程序开发
```bash
npm run dev:mp-weixin
```

### 微信小程序打包
```bash
npm run build:mp-weixin
```

## 主要功能
1. 首页 - 个性化推荐、快速导航
2. 今日食谱 - 每日食谱推荐
3. 我的食谱 - 个人食谱管理
4. AI聊天助手 - 智能问答
5. 设置 - 个人设置、系统设置

## 开发注意事项
1. 确保已安装HBuilderX或微信开发者工具
2. 配置正确的后端API地址
3. 微信小程序需要在开发者工具中配置appid
4. 遵守uni-app开发规范

## 文档
- [uni-app官方文档](https://uniapp.dcloud.net.cn/)
- [微信小程序开发文档](https://developers.weixin.qq.com/miniprogram/dev/)
