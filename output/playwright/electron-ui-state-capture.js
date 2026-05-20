const fs = require('fs');
const path = require('path');
const http = require('http');
const { chromium } = require('playwright');

const APP_DIR = '/Users/nickxiao/JasEatsChoice/JasEatsChoiceFront';
const OUTPUT_DIR = '/Users/nickxiao/JasEatsChoice/output/playwright';
const SUMMARY_FILE = path.join(OUTPUT_DIR, 'electron-ui-state-summary.json');
const API_BASE = 'http://127.0.0.1:7777/api';
const RENDERER_DIR = path.join(APP_DIR, 'out', 'renderer');

fs.mkdirSync(OUTPUT_DIR, { recursive: true });

function getContentType(filePath) {
  const ext = path.extname(filePath).toLowerCase();
  const types = {
    '.html': 'text/html; charset=utf-8',
    '.js': 'text/javascript; charset=utf-8',
    '.css': 'text/css; charset=utf-8',
    '.json': 'application/json; charset=utf-8',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
    '.svg': 'image/svg+xml',
    '.woff': 'font/woff',
    '.woff2': 'font/woff2'
  };
  return types[ext] || 'application/octet-stream';
}

function startStaticServer(rootDir) {
  return new Promise((resolve, reject) => {
    const server = http.createServer((req, res) => {
      try {
        const requestPath = decodeURIComponent((req.url || '/').split('?')[0]);
        const relativePath = requestPath === '/' ? '/index.html' : requestPath;
        const normalized = path.normalize(relativePath).replace(/^(\.\.[/\\])+/, '');
        let filePath = path.join(rootDir, normalized);
        const shouldFallbackToIndex = !path.extname(filePath);
        const assetsIndex = normalized.indexOf('/assets/');
        if (assetsIndex > 0) {
          filePath = path.join(rootDir, normalized.slice(assetsIndex + 1));
        }

        if (!filePath.startsWith(rootDir)) {
          res.writeHead(403);
          res.end('Forbidden');
          return;
        }

        fs.readFile(filePath, (error, data) => {
          if (error) {
            if (shouldFallbackToIndex) {
              filePath = path.join(rootDir, 'index.html');
              fs.readFile(filePath, (indexError, indexData) => {
                if (indexError) {
                  res.writeHead(404);
                  res.end('Not Found');
                  return;
                }
                res.writeHead(200, { 'Content-Type': getContentType(filePath) });
                res.end(indexData);
              });
              return;
            }
            res.writeHead(404);
            res.end('Not Found');
            return;
          }
          res.writeHead(200, { 'Content-Type': getContentType(filePath) });
          res.end(data);
        });
      } catch (error) {
        res.writeHead(500);
        res.end('Server Error');
      }
    });

    server.listen(0, '127.0.0.1', () => {
      const address = server.address();
      resolve({
        server,
        baseUrl: `http://127.0.0.1:${address.port}`
      });
    });
    server.on('error', reject);
  });
}

async function fetchJson(url, options = {}) {
  const response = await fetch(url, options);
  const text = await response.text();
  try {
    return JSON.parse(text);
  } catch (error) {
    throw new Error(`接口返回无法解析: ${url}\n${text.slice(0, 300)}`);
  }
}

async function getFixtures() {
  const captcha = await fetchJson(`${API_BASE}/v1/captcha/checkCode`);
  const userLogin = await fetchJson(`${API_BASE}/v1/users/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      phone: '17322222222',
      password: 'asdasd',
      captcha: '8888',
      checkCodeKey: captcha.data.checkCodeKey
    })
  });
  const userToken = userLogin.data.token;
  const userInfo = userLogin.data.userInfo || userLogin.data.user;
  const userHeaders = { Authorization: `Bearer ${userToken}` };
  const merchantInfo = (
    await fetchJson(`${API_BASE}/v1/merchant/${userInfo.merchantId}`, { headers: userHeaders })
  ).data;
  const userOrders = (await fetchJson(`${API_BASE}/v1/orders/user/${userInfo.userId}`, { headers: userHeaders })).data || [];

  const adminLogin = await fetchJson(`${API_BASE}/admin/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: 'admin123' })
  });

  return {
    user: {
      token: userToken,
      userInfo,
      merchantInfo,
      firstOrderId: userOrders[0]?.id || 'JD1768312307843'
    },
    merchant: {
      token: userToken,
      userInfo,
      merchantInfo
    },
    admin: {
      token: adminLogin.token,
      adminInfo: adminLogin.admin
    }
  };
}

function buildInitScript({ mode, auth, sessionData = {}, localData = {} }) {
  return ({ modeValue, authValue, sessionValue, localValue }) => {
    const write = (storage, key, value) => {
      storage.setItem(key, typeof value === 'string' ? value : JSON.stringify(value));
    };

    window.api = {
      window: {
        resizeToMain: async () => {},
        resizeToLogin: async () => {},
        resizeToRegister: async () => {},
        resizeToAdminLogin: async () => {}
      },
      openExternal: () => {}
    };

    localStorage.clear();
    sessionStorage.clear();

    if (modeValue === 'user' || modeValue === 'merchant') {
      const userInfo = authValue.userInfo;
      write(localStorage, 'auth_token', authValue.token);
      write(localStorage, 'auth_userId', userInfo.userId);
      write(localStorage, 'auth_phone', userInfo.phone);
      write(localStorage, 'auth_merchantId', userInfo.merchantId || '');
      write(localStorage, 'auth_currentRole', modeValue);
      write(localStorage, 'token', authValue.token);
      write(localStorage, 'userId', userInfo.userId);
      write(localStorage, 'phone', userInfo.phone);
      write(localStorage, 'userInfo', userInfo);
      write(localStorage, 'merchantInfo', authValue.merchantInfo);
    }

    if (modeValue === 'admin') {
      write(localStorage, 'admin_token', authValue.token);
      write(localStorage, 'admin_info', authValue.adminInfo);
    }

    Object.entries(localValue).forEach(([key, value]) => write(localStorage, key, value));
    Object.entries(sessionValue).forEach(([key, value]) => write(sessionStorage, key, value));
  };
}

async function openPage(browser, baseUrl, options) {
  const context = await browser.newContext({
    viewport: { width: 1440, height: 1100 }
  });
  const rewritePrefixes = [
    'https://api.yourdomain.com',
    'http://api.yourdomain.com',
    'https://localhost:7777',
    'https://127.0.0.1:7777'
  ];
  await context.route('**/*', async (route) => {
    const url = route.request().url();
    const matchedPrefix = rewritePrefixes.find((prefix) => url.startsWith(prefix));
    if (matchedPrefix) {
      const response = await route.fetch({
        url: `${API_BASE}${url.slice(matchedPrefix.length)}`
      });
      await route.fulfill({ response });
      return;
    }
    await route.continue();
  });
  await context.addInitScript(buildInitScript(options), {
    modeValue: options.mode,
    authValue: options.auth,
    sessionValue: options.sessionData || {},
    localValue: options.localData || {}
  });
  const page = await context.newPage();
  await page.goto(`${baseUrl}${options.routePath}`, { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(options.delay || 3500);
  if (options.waitSelector) {
    await page.locator(options.waitSelector).first().waitFor({ timeout: 15000 });
  }
  return { context, page };
}

async function screenshot(page, filename) {
  await page.screenshot({
    path: path.join(OUTPUT_DIR, filename),
    fullPage: true
  });
}

async function clickButton(page, text) {
  const button = page.getByRole('button', { name: text }).first();
  await button.waitFor({ timeout: 10000 });
  await button.click();
}

async function captureUserGroupOrderStates(browser, baseUrl, fixtures, results) {
  const { context, page } = await openPage(browser, baseUrl, {
    mode: 'user',
    auth: fixtures.user,
    routePath: '/user/home/chat',
    waitSelector: '.chat-container',
    delay: 5000
  });

  try {
    const groupConversation = page.locator('.conversation-item').filter({
      has: page.locator('.group-tag')
    }).first();
    await groupConversation.waitFor({ timeout: 15000 });
    await groupConversation.click();
    await page.waitForTimeout(3000);

    const floatingOrderButton = page.locator('.floating-order-btn');
    const quickCreateButton = page.locator('.group-quick-actions .el-button').first();

    if (!(await floatingOrderButton.isVisible().catch(() => false))) {
      if (await quickCreateButton.isVisible().catch(() => false)) {
        await quickCreateButton.click();
        await page.waitForTimeout(4000);
      }
    }

    await floatingOrderButton.click();
    await page.locator('.group-order-drawer').waitFor({ timeout: 10000 });
    await screenshot(page, 'electron-user-group-order-drawer-state.png');
    results.push({ file: 'electron-user-group-order-drawer-state.png', success: true });

    const chooseButton = page.getByText(/选择商家|选择菜品/, { exact: false }).last();
    await chooseButton.click();
    await page.locator('.merchant-select-dialog').waitFor({ timeout: 10000 });
    await screenshot(page, 'electron-user-merchant-select-dialog.png');
    results.push({ file: 'electron-user-merchant-select-dialog.png', success: true });

    const firstMerchantCard = page.locator('.merchant-select-dialog .merchant-card').first();
    await firstMerchantCard.click();
    await page.locator('.product-select-dialog').waitFor({ timeout: 10000 });
    await screenshot(page, 'electron-user-product-select-dialog.png');
    results.push({ file: 'electron-user-product-select-dialog.png', success: true });

    const firstProduct = page.locator('.product-select-dialog .product-item').first();
    const customizeButton = firstProduct.getByRole('button', { name: '定制' }).first();
    if (await customizeButton.isVisible().catch(() => false)) {
      await customizeButton.click();
    } else {
      await firstProduct.click();
    }
    await page.locator('.product-customize-dialog').waitFor({ timeout: 10000 });
    await screenshot(page, 'electron-user-product-customize-dialog.png');
    results.push({ file: 'electron-user-product-customize-dialog.png', success: true });
  } catch (error) {
    results.push({ group: 'user-group-order', success: false, error: error.message });
  } finally {
    await context.close();
  }
}

async function captureUserPaymentConfirm(browser, baseUrl, fixtures, results) {
  const pendingOrder = {
    merchant: {
      merchantId: fixtures.user.merchantInfo.merchantId,
      id: fixtures.user.merchantInfo.merchantId,
      name: fixtures.user.merchantInfo.name,
      rating: fixtures.user.merchantInfo.rating || 4.8,
      deliveryTime: '约30分钟',
      deliveryFee: 5
    },
    groupName: '测试订单群',
    userName: fixtures.user.userInfo.nickname,
    creator: fixtures.user.userInfo.nickname,
    members: [fixtures.user.userInfo.nickname],
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
    orderId: 'state-capture-order'
  };

  const { context, page } = await openPage(browser, baseUrl, {
    mode: 'user',
    auth: fixtures.user,
    routePath: '/user/home/order-confirmation',
    sessionData: { pendingOrder },
    waitSelector: '.order-confirmation-container',
    delay: 5000
  });

  try {
    await clickButton(page, '提交订单');
    await page.locator('.el-message-box').waitFor({ timeout: 10000 });
    await screenshot(page, 'electron-user-payment-confirm-dialog.png');
    results.push({ file: 'electron-user-payment-confirm-dialog.png', success: true });
  } catch (error) {
    results.push({ file: 'electron-user-payment-confirm-dialog.png', success: false, error: error.message });
  } finally {
    await context.close();
  }
}

async function captureUserConsumeHistoryDetail(browser, baseUrl, fixtures, results) {
  const { context, page } = await openPage(browser, baseUrl, {
    mode: 'user',
    auth: fixtures.user,
    routePath: '/user/home/consume-history',
    waitSelector: '.transactions-list',
    delay: 5000
  });

  try {
    const firstItem = page.locator('.transaction-item').first();
    await firstItem.waitFor({ timeout: 10000 });
    await firstItem.click();
    await page.locator('.el-dialog').filter({ hasText: '交易详情' }).first().waitFor({ timeout: 10000 });
    await screenshot(page, 'electron-user-consume-history-detail-dialog.png');
    results.push({ file: 'electron-user-consume-history-detail-dialog.png', success: true });
  } catch (error) {
    results.push({ file: 'electron-user-consume-history-detail-dialog.png', success: false, error: error.message });
  } finally {
    await context.close();
  }
}

async function captureMerchantReplyDialog(browser, baseUrl, fixtures, results) {
  const { context, page } = await openPage(browser, baseUrl, {
    mode: 'merchant',
    auth: fixtures.merchant,
    routePath: '/merchant/home/comments',
    waitSelector: '.comments-page, .comments-container, .comments-list, .merchant-comments-page',
    delay: 6000
  });

  try {
    const replyButton = page.getByRole('button', { name: /回复评价|追加回复/ }).first();
    await replyButton.waitFor({ timeout: 12000 });
    await replyButton.click();
    await page.locator('.el-dialog').filter({ hasText: /回复评价|追加回复/ }).first().waitFor({ timeout: 10000 });
    await screenshot(page, 'electron-merchant-comment-reply-dialog.png');
    results.push({ file: 'electron-merchant-comment-reply-dialog.png', success: true });
  } catch (error) {
    results.push({ file: 'electron-merchant-comment-reply-dialog.png', success: false, error: error.message });
  } finally {
    await context.close();
  }
}

async function captureAdminDishDialogs(browser, baseUrl, fixtures, results) {
  const { context, page } = await openPage(browser, baseUrl, {
    mode: 'admin',
    auth: fixtures.admin,
    routePath: '/admin/dishes/audit',
    waitSelector: '.el-table',
    delay: 6000
  });

  try {
    await clickButton(page, '详情');
    await page.locator('.el-dialog').filter({ hasText: '菜品详情' }).first().waitFor({ timeout: 10000 });
    await screenshot(page, 'electron-admin-dish-audit-detail-dialog.png');
    results.push({ file: 'electron-admin-dish-audit-detail-dialog.png', success: true });

    await clickButton(page, '去审核');
    await page.locator('.el-dialog').filter({ hasText: '菜品审核' }).first().waitFor({ timeout: 10000 });
    await screenshot(page, 'electron-admin-dish-audit-review-dialog.png');
    results.push({ file: 'electron-admin-dish-audit-review-dialog.png', success: true });
  } catch (error) {
    results.push({ group: 'admin-dish-dialogs', success: false, error: error.message });
  } finally {
    await context.close();
  }
}

async function captureAdminRechargeDetail(browser, baseUrl, fixtures, results) {
  const { context, page } = await openPage(browser, baseUrl, {
    mode: 'admin',
    auth: fixtures.admin,
    routePath: '/admin/finance/recharges',
    waitSelector: '.el-table',
    delay: 6000
  });

  try {
    await clickButton(page, '详情');
    await page.locator('.el-dialog').filter({ hasText: '充值详情' }).first().waitFor({ timeout: 10000 });
    await screenshot(page, 'electron-admin-recharge-detail-dialog.png');
    results.push({ file: 'electron-admin-recharge-detail-dialog.png', success: true });
  } catch (error) {
    results.push({ file: 'electron-admin-recharge-detail-dialog.png', success: false, error: error.message });
  } finally {
    await context.close();
  }
}

async function captureAdminAnnouncementDialogs(browser, baseUrl, fixtures, results) {
  const { context, page } = await openPage(browser, baseUrl, {
    mode: 'admin',
    auth: fixtures.admin,
    routePath: '/admin/announcements',
    waitSelector: '.el-table',
    delay: 6000
  });

  try {
    await clickButton(page, '查看');
    await page.locator('.el-dialog').filter({ hasText: '公告详情' }).first().waitFor({ timeout: 10000 });
    await screenshot(page, 'electron-admin-announcement-detail-dialog.png');
    results.push({ file: 'electron-admin-announcement-detail-dialog.png', success: true });

    await clickButton(page, '编辑');
    await page.locator('.el-dialog').filter({ hasText: /编辑公告|发布公告/ }).first().waitFor({ timeout: 10000 });
    await screenshot(page, 'electron-admin-announcement-edit-dialog.png');
    results.push({ file: 'electron-admin-announcement-edit-dialog.png', success: true });
  } catch (error) {
    results.push({ group: 'admin-announcement-dialogs', success: false, error: error.message });
  } finally {
    await context.close();
  }
}

async function captureAdminPermissionDialog(browser, baseUrl, fixtures, results) {
  const { context, page } = await openPage(browser, baseUrl, {
    mode: 'admin',
    auth: fixtures.admin,
    routePath: '/admin/settings/permissions',
    waitSelector: '.el-table',
    delay: 6000
  });

  try {
    await clickButton(page, '编辑');
    await page.locator('.el-dialog').filter({ hasText: /编辑权限|新建权限|添加子权限/ }).first().waitFor({ timeout: 10000 });
    await screenshot(page, 'electron-admin-permission-edit-dialog.png');
    results.push({ file: 'electron-admin-permission-edit-dialog.png', success: true });
  } catch (error) {
    results.push({ file: 'electron-admin-permission-edit-dialog.png', success: false, error: error.message });
  } finally {
    await context.close();
  }
}

async function main() {
  const fixtures = await getFixtures();
  const { server, baseUrl } = await startStaticServer(RENDERER_DIR);
  const browser = await chromium.launch({ headless: true });
  const results = [];

  try {
    await captureUserGroupOrderStates(browser, baseUrl, fixtures, results);
    await captureUserPaymentConfirm(browser, baseUrl, fixtures, results);
    await captureUserConsumeHistoryDetail(browser, baseUrl, fixtures, results);
    await captureMerchantReplyDialog(browser, baseUrl, fixtures, results);
    await captureAdminDishDialogs(browser, baseUrl, fixtures, results);
    await captureAdminRechargeDetail(browser, baseUrl, fixtures, results);
    await captureAdminAnnouncementDialogs(browser, baseUrl, fixtures, results);
    await captureAdminPermissionDialog(browser, baseUrl, fixtures, results);
  } finally {
    await browser.close();
    await new Promise((resolve) => server.close(resolve));
  }

  fs.writeFileSync(SUMMARY_FILE, JSON.stringify(results, null, 2));
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
