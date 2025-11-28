const { app, BrowserWindow } = require('electron')

console.log('App:', app)
console.log('BrowserWindow:', BrowserWindow)

app.on('ready', () => {
  console.log('App is ready!')
  app.quit()
})

app.on('window-all-closed', () => {
  app.quit()
})
