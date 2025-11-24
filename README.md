# JasEatsChoiceFront - Electron Desktop Application

JasEatsChoice是一个基于Electron框架开发的桌面端餐饮下单系统。

## 项目结构

```
JasEatsChoiceFront/
├── main.js              # Electron主进程入口文件
├── preload.js           # 预加载脚本，用于渲染进程与主进程通信
├── index.html           # 渲染进程界面
└── package.json         # 项目配置文件
```

## 功能实现

### main.js
- 创建Electron窗口
- 配置窗口大小和Web偏好设置
- 实现窗口生命周期管理

### index.html
- 实现了用户端首页的基本UI界面
- 参考了PRD文档和页面设计
- 左侧菜单栏包含所有功能入口
- 右侧内容区展示天气、推荐菜品、热点和教程

## 技术栈

- Electron - 桌面应用框架
- HTML/CSS/JavaScript - 前端页面开发

## 启动方式

```bash
npm run start
```

## 开发状态

目前已完成基础框架搭建和首页UI实现。
