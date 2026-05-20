const fs = require('fs');
const path = require('path');
const { _electron: electron } = require('playwright');

const APP_DIR = '/Users/nickxiao/JasEatsChoice/JasEatsChoiceFront';
const OUTPUT_DIR = '/Users/nickxiao/JasEatsChoice/output/playwright';
const LOG_FILE = path.join(OUTPUT_DIR, 'electron-retry-missing.log');
const API = 'http://127.0.0.1:7777/api';

function log(message) {
  fs.appendFileSync(LOG_FILE, `${new Date().toISOString()} ${message}\n`);
}

async function fetchJson(url, options = {}) {
  const response = await fetch(url, options);
  const text = await response.text();
  return JSON.parse(text);
}

async function main() {
  fs.writeFileSync(LOG_FILE, '');
  const captcha = await fetchJson(`${API}/v1/captcha/checkCode`);
  const login = await fetchJson(`${API}/v1/users/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      phone: '17322222222',
      password: 'asdasd',
      captcha: '8888',
      checkCodeKey: captcha.data.checkCodeKey
    })
  });
  const token = login.data.token;
  const userInfo = login.data.userInfo || login.data.user;
  const merchant = (
    await fetchJson(`${API}/v1/merchant/${userInfo.merchantId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
  ).data;

  const electronPath = require(path.join(APP_DIR, 'node_modules', 'electron'));
  log('before electron.launch');
  const app = await electron.launch({ executablePath: electronPath, args: ['.'] });
  log('after electron.launch');
  let page = null;
  for (let attempt = 0; attempt < 20; attempt += 1) {
    log(`poll windows attempt=${attempt}`);
    const windows = app.windows();
    if (windows.length > 0) {
      page = windows[0];
      log(`window found count=${windows.length}`);
      break;
    }
    await new Promise((resolve) => setTimeout(resolve, 500));
  }
  if (!page) {
    throw new Error('启动后未检测到 Electron 窗口');
  }
  log(`first window ${page.url()}`);

  async function prep(hash, extraSession = {}) {
    log(`prep ${hash}`);
    await page.evaluate(
      ({ tokenValue, user, merchantInfo, nextHash, sessionData }) => {
        const write = (storage, key, value) => {
          storage.setItem(key, typeof value === 'string' ? value : JSON.stringify(value));
        };

        localStorage.clear();
        sessionStorage.clear();
        write(localStorage, 'auth_token', tokenValue);
        write(localStorage, 'auth_userId', user.userId);
        write(localStorage, 'auth_phone', user.phone);
        write(localStorage, 'auth_merchantId', user.merchantId || '');
        write(localStorage, 'auth_currentRole', 'user');
        write(localStorage, 'token', tokenValue);
        write(localStorage, 'userId', user.userId);
        write(localStorage, 'phone', user.phone);
        write(localStorage, 'userInfo', user);
        write(localStorage, 'merchantInfo', merchantInfo);
        Object.entries(sessionData).forEach(([key, value]) => write(sessionStorage, key, value));
        window.location.hash = nextHash;
      },
      {
        tokenValue: token,
        user: userInfo,
        merchantInfo: merchant,
        nextHash: hash,
        sessionData: extraSession
      }
    );
    log(`reload start ${hash}`);
    await page.reload({ waitUntil: 'domcontentloaded' });
    await page.waitForTimeout(3500);
    log(`reload done ${hash} -> ${page.url()}`);
  }

  const pendingOrder = {
    merchant: {
      merchantId: merchant.merchantId,
      id: merchant.merchantId,
      name: merchant.name,
      rating: merchant.rating || 4.8,
      deliveryTime: '约30分钟',
      deliveryFee: 5
    },
    groupName: '测试订单群',
    userName: userInfo.nickname,
    creator: userInfo.nickname,
    members: [userInfo.nickname],
    cartItems: [
      {
        dishId: '1',
        name: '宫保鸡丁',
        dishName: '宫保鸡丁',
        price: 38,
        quantity: 1,
        totalPrice: 38,
        selectedOptionalIngredients: [],
        note: ''
      }
    ],
    totalAmount: 38,
    fromChat: false,
    orderId: 'test-order-1'
  };

  await prep('#/user/home/today-recipe');
  await page.screenshot({
    path: path.join(OUTPUT_DIR, 'electron-user-today-recipe.png'),
    fullPage: true
  });
  log('saved today recipe');

  await prep('#/user/home/calorie');
  await page.screenshot({
    path: path.join(OUTPUT_DIR, 'electron-user-calorie.png'),
    fullPage: true
  });
  log('saved calorie');

  await prep('#/user/home/order-confirmation', { pendingOrder });
  await page.screenshot({
    path: path.join(OUTPUT_DIR, 'electron-user-order-confirmation-page.png'),
    fullPage: true
  });
  log('saved order confirmation');

  await app.close();
}

main().catch((error) => {
  log(`error ${error.stack || error.message}`);
  process.exit(1);
});
