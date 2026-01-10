const fs = require('fs')
const path = require('path')

const electronModulePath = path.dirname(require.resolve('electron'))
console.log('Electron module path:', electronModulePath)

console.log('Files in electron module directory:', fs.readdirSync(electronModulePath))

// Check dist directory
const distPath = path.join(electronModulePath, 'dist')
if (fs.existsSync(distPath)) {
  console.log('Files in dist directory:', fs.readdirSync(distPath))

  const electronAppPath = path.join(distPath, 'Electron.app')
  if (fs.existsSync(electronAppPath)) {
    console.log('Electron.app directory exists')
    const contentsPath = path.join(electronAppPath, 'Contents')
    if (fs.existsSync(contentsPath)) {
      console.log('Contents directory:', fs.readdirSync(contentsPath))

      const resourcesPath = path.join(contentsPath, 'Resources')
      if (fs.existsSync(resourcesPath)) {
        console.log('Resources directory:', fs.readdirSync(resourcesPath))
      }
    }
  }
}

// Check if we have electron.d.ts file for TypeScript
const typesPath = path.join(electronModulePath, 'electron.d.ts')
if (fs.existsSync(typesPath)) {
  console.log('TypeScript definitions file exists (electron.d.ts)')
}

// Try to require electron
console.log('=== Attempting to require electron ===')
try {
  const electron = require('electron')
  console.log('typeof electron:', typeof electron)
  console.log('electron:', electron)

  if (typeof electron === 'string') {
    console.log('WARNING: electron module returns string path')
  } else {
    console.log('Electron API object properties:', Object.keys(electron))
  }
} catch (error) {
  console.error('Error requiring electron:', error)
}
