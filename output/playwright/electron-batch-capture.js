const fs = require('fs');
const path = require('path');
const { _electron: electron } = require('playwright');

const APP_DIR = '/Users/nickxiao/JasEatsChoice/JasEatsChoiceFront';
const OUTPUT_DIR = '/Users/nickxiao/JasEatsChoice/output/playwright';
const API_BASE_URL = 'http://127.0.0.1:7777/api';

const USER_CREDENTIALS = {
  phone: '17322222222',
  password: 'asdasd'
};

const ADMIN_CREDENTIALS = {
  username: 'admin',
  password: 'admin123'
};

async function fetchJson(url, options = {}) {
  const response = await fetch(url, options);
  const text = await response.text();

  try {
    return JSON.parse(text);
  } catch (error) {
    throw new Error(`解析接口响应失败: ${url}\n${text.slice(0, 300)}`);
  }
}

async function getFixtures() {
  const captcha = await fetchJson(`${API_BASE_URL}/v1/captcha/checkCode`);
  const userLogin = await fetchJson(`${API_BASE_URL}/v1/users/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      phone: USER_CREDENTIALS.phone,
      password: USER_CREDENTIALS.password,
      captcha: '8888',
      checkCodeKey: captcha.data.checkCodeKey
    })
  });

  if (!userLogin.success || !userLogin.data?.token) {
    throw new Error(`用户登录失败: ${JSON.stringify(userLogin)}`);
  }

  const userToken = userLogin.data.token;
  const userInfo = userLogin.data.userInfo || userLogin.data.user;
  const userHeaders = { Authorization: `Bearer ${userToken}` };

  const merchantInfoResponse = await fetchJson(
    `${API_BASE_URL}/v1/merchant/${userInfo.merchantId}`,
    { headers: userHeaders }
  );
  const userOrdersResponse = await fetchJson(
    `${API_BASE_URL}/v1/orders/user/${userInfo.userId}`,
    { headers: userHeaders }
  );
  const dishesResponse = await fetchJson(`${API_BASE_URL}/v1/dishes`, { headers: userHeaders });
  const tutorialMyResponse = await fetchJson(
    `${API_BASE_URL}/v1/tutorial/user/my?pageNum=1&pageSize=5`,
    { headers: userHeaders }
  );
  const hotTopicResponse = await fetchJson(`${API_BASE_URL}/v1/home/hot-topic`, {
    headers: userHeaders
  });

  const adminLogin = await fetchJson(`${API_BASE_URL}/admin/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(ADMIN_CREDENTIALS)
  });

  if (!adminLogin.success || !adminLogin.token) {
    throw new Error(`管理端登录失败: ${JSON.stringify(adminLogin)}`);
  }

  const adminToken = adminLogin.token;
  const adminHeaders = { Authorization: `Bearer ${adminToken}` };
  const adminOrdersResponse = await fetchJson(
    `${API_BASE_URL}/admin/orders?pageNum=1&pageSize=5`,
    { headers: adminHeaders }
  );
  const adminTopicsResponse = await fetchJson(
    `${API_BASE_URL}/v1/admin/hot-topic?pageNum=1&pageSize=5`,
    { headers: adminHeaders }
  );

  const userOrders = userOrdersResponse.data || [];
  const dishes = dishesResponse.data || [];
  const tutorials = tutorialMyResponse.records || tutorialMyResponse.data?.records || [];
  const adminOrders = adminOrdersResponse.records || adminOrdersResponse.data?.records || [];
  const adminTopics = adminTopicsResponse.records || adminTopicsResponse.data?.records || [];

  return {
    user: {
      token: userToken,
      userInfo,
      merchantInfo: merchantInfoResponse.data,
      orderId: userOrders[0]?.id || 'O1775982925211',
      orderIdForEvaluation:
        userOrders.find((item) => [3, 4, 5].includes(item.status))?.id || userOrders[0]?.id,
      dishId: String(dishes[0]?.id || dishes[0]?.dishId || '1'),
      tutorialId: String(tutorials[0]?.id || '2022502534052163585'),
      hotTopic: hotTopicResponse.data || { content: '今日热点', sourceType: 'MANUAL' },
      selectedMerchant: merchantInfoResponse.data
    },
    merchant: {
      token: userToken,
      userInfo,
      merchantInfo: merchantInfoResponse.data,
      orderId: adminOrders[0]?.id || userOrders[0]?.id || 'O1775982925211',
      tutorialId: String(tutorials.find((item) => item.merchantSource)?.id || tutorials[0]?.id || '2022500857018429442')
    },
    admin: {
      token: adminToken,
      adminInfo: adminLogin.admin,
      orderId: adminOrders[0]?.id || 'O1775982925211',
      topicId: String(adminTopics[0]?.id || '1')
    }
  };
}

async function settlePage(page, extraDelay = 1800) {
  await page.waitForLoadState('domcontentloaded').catch(() => {});
  await page.waitForTimeout(extraDelay);
  await page
    .locator('.el-loading-mask')
    .first()
    .waitFor({ state: 'hidden', timeout: 3000 })
    .catch(() => {});
  await page.waitForTimeout(700);
}

async function seedUserSession(page, fixtures, role = 'user') {
  await page.evaluate(
    ({ token, userInfo, merchantInfo, roleName }) => {
      const write = (storage, key, value) => {
        storage.setItem(key, typeof value === 'string' ? value : JSON.stringify(value));
      };

      localStorage.clear();
      sessionStorage.clear();

      write(localStorage, 'auth_token', token);
      write(localStorage, 'auth_userId', userInfo.userId);
      write(localStorage, 'auth_phone', userInfo.phone);
      write(localStorage, 'auth_merchantId', userInfo.merchantId || '');
      write(localStorage, 'auth_currentRole', roleName);

      write(localStorage, 'token', token);
      write(localStorage, 'userId', userInfo.userId);
      write(localStorage, 'phone', userInfo.phone);
      write(localStorage, 'userInfo', userInfo);
      write(localStorage, 'merchantInfo', merchantInfo);
    },
    {
      token: fixtures.token,
      userInfo: fixtures.userInfo,
      merchantInfo: fixtures.merchantInfo,
      roleName: role
    }
  );
}

async function seedAdminSession(page, fixtures) {
  await page.evaluate(
    ({ token, adminInfo }) => {
      const write = (storage, key, value) => {
        storage.setItem(key, typeof value === 'string' ? value : JSON.stringify(value));
      };

      localStorage.clear();
      sessionStorage.clear();

      write(localStorage, 'admin_token', token);
      write(localStorage, 'admin_info', adminInfo);
    },
    fixtures
  );
}

async function prepareUserApp(page, fixtures, hash, role = 'user', session = {}) {
  await seedUserSession(page, fixtures, role);
  await page.evaluate(
    ({ sessionData, targetHash }) => {
      const write = (storage, key, value) => {
        storage.setItem(key, typeof value === 'string' ? value : JSON.stringify(value));
      };

      Object.entries(sessionData || {}).forEach(([key, value]) => {
        write(sessionStorage, key, value);
      });
      window.location.hash = targetHash;
    },
    { sessionData: session, targetHash: hash }
  );
  await page.reload({ waitUntil: 'domcontentloaded' });
  await settlePage(page);
}

async function prepareAdminApp(page, fixtures, hash) {
  await seedAdminSession(page, fixtures);
  await page.evaluate((targetHash) => {
    window.location.hash = targetHash;
  }, hash);
  await page.reload({ waitUntil: 'domcontentloaded' });
  await settlePage(page);
}

async function capture(page, filename) {
  const screenshotPath = path.join(OUTPUT_DIR, filename);
  await page.screenshot({ path: screenshotPath, fullPage: true });
  return screenshotPath;
}

async function main() {
  fs.mkdirSync(OUTPUT_DIR, { recursive: true });
  process.chdir(APP_DIR);

  const fixtures = await getFixtures();
  const electronPath =
    process.env.ELECTRON_EXECUTABLE_PATH ||
    require(path.join(APP_DIR, 'node_modules', 'electron'));

  const app = await electron.launch({
    executablePath: electronPath,
    args: ['.']
  });

  const page = await app.firstWindow();
  const results = [];

  const userRoutes = [
    { filename: 'electron-user-login.png', hash: '#/login', type: 'guest', session: {} },
    { filename: 'electron-user-home.png', hash: '#/user/home' },
    { filename: 'electron-user-recommend.png', hash: '#/user/home/recommend' },
    { filename: 'electron-user-merchants.png', hash: '#/user/home/merchants' },
    {
      filename: 'electron-user-merchant-detail.png',
      hash: '#/user/home/merchant-detail?viewMode=order',
      session: { selectedMerchant: fixtures.user.selectedMerchant }
    },
    { filename: 'electron-user-order-confirmation-page.png', hash: '#/user/home/order-confirmation' },
    { filename: 'electron-user-today-recipe.png', hash: '#/user/home/today-recipe' },
    { filename: 'electron-user-calorie.png', hash: '#/user/home/calorie' },
    { filename: 'electron-user-my-recipe.png', hash: '#/user/home/my-recipe' },
    { filename: 'electron-user-diet-record.png', hash: '#/user/home/diet-record' },
    { filename: 'electron-user-orders.png', hash: `#/user/home/orders` },
    { filename: 'electron-user-order-detail.png', hash: `#/user/home/order-detail/${fixtures.user.orderId}` },
    { filename: 'electron-user-evaluate-order.png', hash: `#/user/home/evaluate-order/${fixtures.user.orderIdForEvaluation}` },
    { filename: 'electron-user-consume-history.png', hash: '#/user/home/consume-history' },
    { filename: 'electron-user-message-center.png', hash: '#/user/home/message-center' },
    { filename: 'electron-user-system-notification.png', hash: '#/user/home/system-notification' },
    { filename: 'electron-user-chat.png', hash: '#/user/home/chat' },
    { filename: 'electron-user-contacts.png', hash: '#/user/home/contacts' },
    { filename: 'electron-user-ai.png', hash: '#/user/home/ai' },
    { filename: 'electron-user-settings.png', hash: '#/user/home/settings' },
    { filename: 'electron-user-profile.png', hash: '#/user/home/profile' },
    { filename: 'electron-user-wallet-management.png', hash: '#/user/home/wallet-management' },
    { filename: 'electron-user-wallet-transactions.png', hash: '#/user/home/wallet-transactions' },
    { filename: 'electron-user-payment-password-setup.png', hash: '#/user/home/payment-password-setup' },
    { filename: 'electron-user-wallet-security.png', hash: '#/user/home/wallet-security' },
    { filename: 'electron-user-address.png', hash: '#/user/home/address' },
    { filename: 'electron-user-contact.png', hash: '#/user/home/contact' },
    { filename: 'electron-user-dish-detail.png', hash: `#/user/home/dish-detail/${fixtures.user.dishId}` },
    { filename: 'electron-user-my-collection.png', hash: '#/user/home/my-collection' },
    { filename: 'electron-user-tutorials.png', hash: '#/user/home/tutorials' },
    { filename: 'electron-user-tutorial-detail.png', hash: `#/user/home/tutorials/${fixtures.user.tutorialId}` },
    { filename: 'electron-user-publish-tutorial.png', hash: '#/user/home/tutorials/publish' },
    { filename: 'electron-user-my-tutorials.png', hash: '#/user/home/tutorials/my' },
    {
      filename: 'electron-user-hot-topic.png',
      hash: '#/user/home/hot-topic',
      session: {},
      localExtra: { currentHotTopic: fixtures.user.hotTopic }
    }
  ];

  const merchantRoutes = [
    { filename: 'electron-merchant-home.png', hash: '#/merchant/home' },
    { filename: 'electron-merchant-orders.png', hash: '#/merchant/home/orders' },
    { filename: 'electron-merchant-today-orders.png', hash: '#/merchant/home/today-orders' },
    { filename: 'electron-merchant-menu.png', hash: '#/merchant/home/menu' },
    { filename: 'electron-merchant-messages.png', hash: '#/merchant/home/messages' },
    { filename: 'electron-merchant-menu-edit.png', hash: '#/merchant/home/menu-edit' },
    { filename: 'electron-merchant-dish-management.png', hash: '#/merchant/home/dish-management' },
    { filename: 'electron-merchant-dish-edit.png', hash: '#/merchant/home/dish-edit' },
    { filename: 'electron-merchant-chat.png', hash: '#/merchant/home/chat' },
    { filename: 'electron-merchant-statistics.png', hash: '#/merchant/home/statistics' },
    { filename: 'electron-merchant-order-detail.png', hash: `#/merchant/home/order-detail/${fixtures.merchant.orderId}` },
    { filename: 'electron-merchant-comments.png', hash: '#/merchant/home/comments' },
    { filename: 'electron-merchant-settings.png', hash: '#/merchant/home/settings' },
    { filename: 'electron-merchant-tutorials.png', hash: '#/merchant/home/tutorials' },
    { filename: 'electron-merchant-wish-list-audit.png', hash: '#/merchant/home/wish-list-audit' },
    { filename: 'electron-merchant-ai.png', hash: '#/merchant/home/ai' }
  ];

  const adminRoutes = [
    { filename: 'electron-admin-login.png', hash: '#/admin/login', type: 'guest' },
    { filename: 'electron-admin-dashboard.png', hash: '#/admin/dashboard' },
    { filename: 'electron-admin-users.png', hash: '#/admin/users' },
    { filename: 'electron-admin-merchants.png', hash: '#/admin/merchants' },
    { filename: 'electron-admin-merchants-audit.png', hash: '#/admin/merchants/audit' },
    { filename: 'electron-admin-orders.png', hash: '#/admin/orders' },
    { filename: 'electron-admin-dishes.png', hash: '#/admin/dishes' },
    { filename: 'electron-admin-dishes-audit.png', hash: '#/admin/dishes/audit' },
    { filename: 'electron-admin-tutorial-manage.png', hash: '#/admin/tutorials/manage' },
    { filename: 'electron-admin-tutorial-review.png', hash: '#/admin/tutorials/review' },
    { filename: 'electron-admin-topics.png', hash: '#/admin/topics' },
    { filename: 'electron-admin-announcements.png', hash: '#/admin/announcements' },
    { filename: 'electron-admin-withdrawals.png', hash: '#/admin/finance/withdrawals' },
    { filename: 'electron-admin-recharges.png', hash: '#/admin/finance/recharges' },
    { filename: 'electron-admin-refunds.png', hash: '#/admin/finance/refunds' },
    { filename: 'electron-admin-settings.png', hash: '#/admin/settings' },
    { filename: 'electron-admin-profile.png', hash: '#/admin/settings/profile' },
    { filename: 'electron-admin-password.png', hash: '#/admin/settings/password' },
    { filename: 'electron-admin-roles.png', hash: '#/admin/settings/roles' },
    { filename: 'electron-admin-permissions.png', hash: '#/admin/settings/permissions' },
    { filename: 'electron-admin-logs.png', hash: '#/admin/settings/logs' },
    { filename: 'electron-admin-statistics.png', hash: '#/admin/statistics' }
  ];

  async function captureGuest(hash, filename) {
    await page.evaluate((targetHash) => {
      localStorage.clear();
      sessionStorage.clear();
      window.location.hash = targetHash;
    }, hash);
    await page.reload({ waitUntil: 'domcontentloaded' });
    await settlePage(page);
    await capture(page, filename);
  }

  async function captureUserRoute(route, role) {
    await prepareUserApp(page, role === 'merchant' ? fixtures.merchant : fixtures.user, route.hash, role, route.session || {});
    if (route.localExtra) {
      await page.evaluate((localExtra) => {
        const write = (storage, key, value) => {
          storage.setItem(key, typeof value === 'string' ? value : JSON.stringify(value));
        };

        Object.entries(localExtra).forEach(([key, value]) => {
          write(localStorage, key, value);
        });
      }, route.localExtra);
      await page.reload({ waitUntil: 'domcontentloaded' });
      await settlePage(page);
    }
    await capture(page, route.filename);
  }

  async function captureAdminRoute(route) {
    await prepareAdminApp(page, fixtures.admin, route.hash);
    await capture(page, route.filename);
  }

  for (const route of userRoutes) {
    try {
      if (route.type === 'guest') {
        await captureGuest(route.hash, route.filename);
      } else {
        await captureUserRoute(route, 'user');
      }
      results.push({ route: route.hash, file: route.filename, success: true });
    } catch (error) {
      results.push({ route: route.hash, file: route.filename, success: false, error: error.message });
    }
  }

  for (const route of merchantRoutes) {
    try {
      await captureUserRoute(route, 'merchant');
      results.push({ route: route.hash, file: route.filename, success: true });
    } catch (error) {
      results.push({ route: route.hash, file: route.filename, success: false, error: error.message });
    }
  }

  for (const route of adminRoutes) {
    try {
      if (route.type === 'guest') {
        await captureGuest(route.hash, route.filename);
      } else {
        await captureAdminRoute(route);
      }
      results.push({ route: route.hash, file: route.filename, success: true });
    } catch (error) {
      results.push({ route: route.hash, file: route.filename, success: false, error: error.message });
    }
  }

  fs.writeFileSync(
    path.join(OUTPUT_DIR, 'electron-batch-summary.json'),
    JSON.stringify(results, null, 2)
  );

  await app.close();
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
