const { app, BrowserWindow } = require('electron');

console.log('Electron app module:', app);

app.whenReady().then(() => {
  console.log('App is ready');
  const win = new BrowserWindow({ width: 800, height: 600 });
  win.loadURL('https://www.electronjs.org');

  win.on('closed', () => {
    app.quit();
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});
