const fs = require('fs')
const path = require('path')

// 获取当前脚本所在的目录
const scriptDir = __dirname

// 构建 Electron 相关路径
const electronModuleDir = path.resolve(scriptDir, 'node_modules', 'electron')
const electronDistDir = path.resolve(electronModuleDir, 'dist')
const electronAppDir = path.resolve(electronDistDir, 'Electron.app', 'Contents')
const resourcesDir = path.resolve(electronAppDir, 'Resources')
const electronAsar = path.resolve(resourcesDir, 'electron.asar')

console.log('=== 项目结构 ===')
console.log('当前脚本目录:', scriptDir)
console.log('Electron 模块目录:', electronModuleDir)
console.log('Electron 分发目录:', electronDistDir)
console.log('Electron 应用目录:', electronAppDir)
console.log('Electron 资源目录:', resourcesDir)
console.log('electron.asar:', fs.existsSync(electronAsar))

// 检查 electron.asar 是否可以被读取
if (fs.existsSync(electronAsar)) {
  console.log('electron.asar 大小:', fs.statSync(electronAsar).size, '字节')
}

// 列出 Resources 目录内容
console.log('\n=== Resources 目录内容 ===')
if (fs.existsSync(resourcesDir)) {
  console.log(fs.readdirSync(resourcesDir))
}

console.log('\n=== 尝试直接加载 Electron ===')
try {
  const electron = require('electron')
  console.log('类型:', typeof electron)
  console.log('值:', electron)
  if (typeof electron === 'string') {
    console.log('这是路径字符串')

    // 检查该路径是否存在
    if (fs.existsSync(electron)) {
      console.log('该路径指向的文件存在')
      const stat = fs.statSync(electron)
      console.log('是文件:', stat.isFile())
      console.log('是目录:', stat.isDirectory())
      console.log('大小:', stat.size, '字节')
    }
  } else {
    console.log('这是对象/函数')
    console.log('属性:', Object.keys(electron))
  }
} catch (error) {
  console.error('加载 Electron 失败:', error)
}

console.log('\n=== process 对象信息 ===')
console.log('process.type:', process.type)
console.log('process.versions.electron:', process.versions.electron)
console.log('process.versions.node:', process.versions.node)
console.log('process.platform:', process.platform)
console.log('process.arch:', process.arch)
