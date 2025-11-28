const { app, BrowserWindow } = require('electron')

app.on('ready', () => {
  const win = new BrowserWindow({
    width: 800,
    height: 600
  })

  win.loadURL('https://www.google.com')

  console.log('Electron app started successfully!')
})

app.on('window-all-closed', () => {
  app.quit()
})
