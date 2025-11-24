import { app, shell, BrowserWindow, ipcMain } from 'electron'
import { join } from 'path'
import icon from '../../resources/icon.png?asset'

// Check if we're in development mode
const isDev = process.env.NODE_ENV === 'development' || process.env.ELECTRON_IS_DEV
import Store from 'electron-store'
import WebSocket from 'ws'

// Initialize electron-store later when app is ready
let store

let mainWindow

function createWindow() {
  // Create the browser window.
  mainWindow = new BrowserWindow({
    width: 1200, // Updated to match desktop design
    height: 800, // Updated to match desktop design
    show: false,
    autoHideMenuBar: true,
    ...(process.platform === 'linux' ? { icon } : {}),
    webPreferences: {
      preload: join(__dirname, '../preload/index.js'),
      sandbox: false
    }
  })

  mainWindow.on('ready-to-show', () => {
    mainWindow.show()
    // Open DevTools to check for renderer errors
    mainWindow.webContents.openDevTools()
  })

  mainWindow.webContents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url)
    return { action: 'deny' }
  })

  // HMR for renderer base on electron-vite cli.
  // Load the remote URL for development or the local html file for production.
  if (isDev && process.env['ELECTRON_RENDERER_URL']) {
    mainWindow.loadURL(process.env['ELECTRON_RENDERER_URL'])
  } else {
    mainWindow.loadFile(join(__dirname, '../renderer/index.html'))
  }
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(() => {
  // Set app user model id for windows
  app.setAppUserModelId('com.electron')

  // Initialize electron-store now that app is ready
  store = new Store({
    projectName: 'jaseatschoice',
    defaults: {
      userPreferences: {
        theme: 'light',
        notification: true,
        defaultFilter: 'all'
      },
      offlineMenus: [],
      historyOrders: []
    }
  })

  // Create window
  createWindow()

  // Electron-store IPC handlers
  ipcMain.handle('store:set', (_, key, value) => {
    store.set(key, value)
  })

  ipcMain.handle('store:get', (_, key) => {
    return store.get(key)
  })

  ipcMain.handle('store:delete', (_, key) => {
    store.delete(key)
  })

  ipcMain.handle('store:clear', () => {
    store.clear()
  })

  app.on('activate', function () {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

// In this file you can include the rest of your app"s specific main process
// code. You can also put them in separate files and require them here.

// WebSocket connection manager
let ws = null

// WebSocket IPC handlers
ipcMain.handle('websocket:connect', async (_, url) => {
  try {
    // Create WebSocket connection
    ws = new WebSocket(url)

    // Connection opened
    ws.on('open', (event) => {
      mainWindow.webContents.send('websocket:open', event)
    })

    // Listen for messages
    ws.on('message', (message) => {
      try {
        const parsedMsg = JSON.parse(message)
        console.log('WebSocket message received:', parsedMsg)

        // Extract message type for classification
        const msgType = parsedMsg.msgType || 'unknown'

        // Handle different message types based on PRD and backend API
        // All message types are forwarded to renderer, with specific handlers if needed

        // For security, we can add message validation here

        // Forward to renderer with specific channel based on message type
        switch (msgType) {
          case 'auth':
          case 'authResponse':
            mainWindow.webContents.send('websocket:auth', parsedMsg)
            break
          case 'orderUpdate':
          case 'orderStatusChange':
            mainWindow.webContents.send('websocket:orderUpdate', parsedMsg)
            break
          case 'chat':
          case 'privateChat':
          case 'groupChat':
          case 'systemMessage':
            mainWindow.webContents.send('websocket:chat', parsedMsg)
            break
          case 'merchantUpdate':
          case 'menuUpdate':
            mainWindow.webContents.send('websocket:merchantUpdate', parsedMsg)
            break
          case 'recommend':
            mainWindow.webContents.send('websocket:recommend', parsedMsg)
            break
          case 'notification':
          case 'systemNotification':
            mainWindow.webContents.send('websocket:notification', parsedMsg)
            break
          default:
            // For all other message types, use the general channel
            mainWindow.webContents.send('websocket:message', parsedMsg)
            break
        }
      } catch (error) {
        console.error('Failed to parse WebSocket message:', error)
        console.error('Raw message:', message)

        // Send error to renderer for debugging
        mainWindow.webContents.send('websocket:error', {
          message: 'Failed to parse WebSocket message',
          error: error.toString(),
          rawMessage: message
        })
      }
    })

    // Connection closed
    ws.on('close', (code, reason) => {
      mainWindow.webContents.send('websocket:close', code, reason)
      ws = null
    })

    // Error occurred
    ws.on('error', (error) => {
      mainWindow.webContents.send('websocket:error', error)
      ws = null
    })

    return true
  } catch (error) {
    console.error('WebSocket connection error:', error)
    mainWindow.webContents.send('websocket:error', error)
    return false
  }
})

ipcMain.handle('websocket:send', async (_, message) => {
  try {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(message))
      return true
    }
    return false
  } catch (error) {
    console.error('Failed to send WebSocket message:', error)
    return false
  }
})

ipcMain.handle('websocket:disconnect', async () => {
  try {
    if (ws) {
      ws.close()
      ws = null
    }
    return true
  } catch (error) {
    console.error('Failed to close WebSocket connection:', error)
    return false
  }
})
