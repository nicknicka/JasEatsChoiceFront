
// 测试直接访问 Electron 内部模块
const fs = require('fs');
const path = require('path');

// Electron 的真实路径
const electronPath = require('electron');
console.log('Electron path:', electronPath);

// Electron.app/Contents/Resources 目录通常包含内置模块
const resourcesPath = path.join(electronPath, '..', '..', 'Resources');
console.log('Resources path:', resourcesPath);

// 检查是否有 electron.asar 或 app 目录
if (fs.existsSync(resourcesPath)) {
    const files = fs.readdirSync(resourcesPath);
    console.log('Resources directory contents:', files);
}

// 检查 process 对象的属性
console.log('Process type:', process.type); // 在 Electron 主进程中应该是 "browser"
console.log('Process versions:', process.versions);

// 尝试访问 electron 内部模块
try {
    const { app, BrowserWindow } = require('electron');
    console.log('Successfully imported Electron API');
    console.log('App:', app);
} catch (error) {
    console.error('Error importing Electron API:', error);
}
