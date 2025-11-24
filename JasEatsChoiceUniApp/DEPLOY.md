# 佳食宜选小程序端 - 部署指南

## 项目概述
本项目是基于uni-app开发的佳食宜选餐饮下单系统小程序端，已经完成了核心页面的开发，包括：

- 首页
- 今日食谱
- 我的食谱
- AI聊天助手
- 设置

## 部署方式

### 方式一：使用HBuilderX部署（推荐）

#### 1. 下载并安装HBuilderX
- 访问 [HBuilderX官方下载](https://www.dcloud.io/hbuilderx.html)
- 下载并安装对应系统版本

#### 2. 导入项目
1. 打开HBuilderX
2. 选择 `文件` -> `导入` -> `从本地目录导入`
3. 选择项目目录 `JasEatsChoiceUniApp`
4. 选择 `uni-app项目` 类型导入

#### 3. 配置微信小程序
1. 点击左侧导航栏的 `manifest.json`
2. 选择 `微信小程序配置`
3. 填写您的微信小程序 `AppID`
4. 保存配置

#### 4. 运行到微信小程序
1. 确保已安装微信开发者工具
2. 点击HBuilderX工具栏的 `运行` -> `运行到小程序模拟器` -> `微信开发者工具`
3. HBuilderX会自动编译并在微信开发者工具中打开项目

#### 5. 发布
1. 点击HBuilderX工具栏的 `发行` -> `微信小程序`
2. 填写版本信息和备注
3. 点击 `发行`，等待编译完成
4. 在微信开发者工具中点击 `上传` 即可提交审核

### 方式二：使用命令行部署

#### 1. 安装依赖
```bash
npm install -g @dcloudio/cli
```

#### 2. 配置
1. 修改 `manifest.json` 中的微信小程序AppID
2. 确保微信开发者工具已开启服务端口

#### 3. 开发运行
```bash
npm run dev:mp-weixin
```

#### 4. 构建发布
```bash
npm run build:mp-weixin
```

## 项目结构
```
├── src/
│   ├── pages/                # 页面目录
│   │   ├── index/index.vue   # 首页
│   │   ├── recipe/today.vue  # 今日食谱
│   │   ├── recipe/my.vue     # 我的食谱
│   │   ├── ai/chat.vue       # AI聊天助手
│   │   └── settings/index.vue # 设置
│   ├── utils/                # 工具函数
│   │   └── request.js        # 网络请求封装
│   ├── static/               # 静态资源
│   ├── main.js               # 入口文件
│   ├── manifest.json         # 应用配置
│   └── pages.json            # 页面配置
└── package.json              # 项目配置
```

## 核心功能
1. **首页** - 个性化菜品推荐、搜索功能、快速导航
2. **今日食谱** - 根据日期和用户偏好推荐每日三餐
3. **我的食谱** - 管理个人收藏和创建的食谱
4. **AI聊天助手** - 智能问答、食谱推荐、健康咨询
5. **设置** - 个人信息管理、系统设置

## 技术栈
- **框架**：uni-app + Vue 3
- **UI**：uni-app原生组件 + 自定义样式
- **网络请求**：封装的request.js
- **构建工具**：Vite

## 后端API
项目已经配置了基础的API请求封装，默认后端地址为 `http://localhost:8080`
可以在 `src/utils/request.js` 中修改baseURL

## 注意事项
1. 确保微信小程序AppID正确配置
2. 微信开发者工具需要开启"服务端口"
3. 首次运行可能需要在微信开发者工具中授权
4. 建议使用HBuilderX进行开发和调试

## 开发文档
- [uni-app官方文档](https://uniapp.dcloud.net.cn/)
- [微信小程序开发文档](https://developers.weixin.qq.com/miniprogram/dev/)
