const electronPath = require('electron')
const { spawn } = require('child_process')

console.log('Electron path:', electronPath)

// 使用 Electron 可执行文件来测试 API
const child = spawn(electronPath, ['-e', 'console.log("Electron API:", require("electron"))'])

child.stdout.on('data', (data) => {
  console.log('stdout:', data.toString())
})

child.stderr.on('data', (data) => {
  console.error('stderr:', data.toString())
})

child.on('close', (code) => {
  console.log('Child process exited with code:', code)
})
