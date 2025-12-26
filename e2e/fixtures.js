// 测试工具函数和常量

// API 基础 URL
const API_BASE_URL = 'http://localhost:8080';

// 测试用户凭据
const TEST_USERS = {
  admin: {
    username: 'admin',
    password: 'admin123',
  },
};

// 等待函数
async function waitForAPI(page, timeout = 30000) {
  // 等待页面加载完成
  await page.waitForLoadState('networkidle', { timeout });
}

// 登录辅助函数
async function login(page, username = TEST_USERS.admin.username, password = TEST_USERS.admin.password) {
  // 访问登录页面
  await page.goto('/index.html');
  await waitForAPI(page);

  // 处理干扰：如果出现"系统接口需要 Token"的提示弹窗，先点击"稍后"或"关闭"
  try {
    // 等待页面稳定
    await page.waitForTimeout(500);
    
    // 检查是否有 Token 相关的提示（可能在对话框内或独立的消息框）
    const tokenAlert = page.locator('.el-alert:has-text("Token"), .el-message:has-text("Token"), .el-notification:has-text("Token"), text=/系统接口需要.*Token/, text=/需要.*Token/').first();
    if (await tokenAlert.count() > 0) {
      // 尝试关闭提示（查找关闭按钮）
      const closeBtn = page.locator('.el-alert__closebtn, .el-message__closeBtn, .el-notification__closeBtn').first();
      if (await closeBtn.count() > 0) {
        await closeBtn.click({ force: true });
        await page.waitForTimeout(300);
      }
    }
    
    // 如果登录对话框中有"稍后"按钮，先点击它关闭对话框，然后重新打开
    const laterButton = page.locator('button:has-text("稍后")').first();
    if (await laterButton.count() > 0 && await laterButton.isVisible()) {
      await laterButton.click({ force: true });
      await page.waitForTimeout(500);
      // 重新打开登录对话框（如果需要）
      const loginBtn = page.locator('button:has-text("登录")').first();
      if (await loginBtn.count() > 0 && !(await page.locator('.el-dialog:has-text("登录")').isVisible())) {
        await loginBtn.click({ force: true });
        await page.waitForTimeout(500);
      }
    }
  } catch (e) {
    // 如果没有找到弹窗，继续执行
    console.log('未发现 Token 提示弹窗，继续登录流程');
  }

  // 等待登录表单出现
  await page.waitForSelector('input[placeholder*="用户名"], input[type="text"]', { timeout: 10000 });

  // 查找并填写用户名
  const usernameInput = page.locator('input[placeholder*="用户名"], input[type="text"]').first();
  await usernameInput.fill(username);

  // 查找并填写密码
  const passwordInput = page.locator('input[type="password"]').first();
  await passwordInput.fill(password);

  // 定位登录按钮 - 使用更精确的选择器，确保在对话框内
  const loginButton = page.locator('.el-dialog button:has-text("登录"), .el-dialog__footer button.el-button--primary:has-text("登录")').first();
  
  // 等待按钮可见且可点击（不是 loading 状态）
  await expect(loginButton).toBeVisible({ timeout: 5000 });
  await expect(loginButton).toBeEnabled({ timeout: 5000 });
  
  // 等待按钮不在 loading 状态（使用标准的 DOM API，不能用 :has-text()）
  await page.waitForFunction(() => {
    // 查找对话框内的所有按钮
    const dialog = document.querySelector('.el-dialog');
    if (!dialog) return false;
    
    // 在对话框内查找包含"登录"文字的按钮
    const buttons = dialog.querySelectorAll('button');
    let loginBtn = null;
    for (const btn of buttons) {
      if (btn.textContent && btn.textContent.trim().includes('登录')) {
        loginBtn = btn;
        break;
      }
    }
    
    if (!loginBtn) return false;
    
    // 检查是否有 loading 类或 disabled 属性
    return !loginBtn.classList.contains('is-loading') && !loginBtn.disabled;
  }, { timeout: 5000 });

  // 设置网络请求监听（监听所有请求，不仅仅是响应）
  let requestSent = false;
  let responseReceived = false;
  
  page.on('request', request => {
    if (request.url().includes('/api/auth/login') && request.method() === 'POST') {
      requestSent = true;
      console.log('[网络请求] 登录请求已发送:', request.url());
    }
  });

  // 设置响应监听（必须在点击之前设置）
  const responsePromise = page.waitForResponse(
    response => {
      const url = response.url();
      const status = response.status();
      const matches = url.includes('/api/auth/login') && (status === 200 || status === 201);
      if (matches) {
        responseReceived = true;
        console.log('[网络响应] 登录 API 响应成功:', url, status);
      }
      return matches;
    },
    { timeout: 20000 }
  ).catch(e => {
    console.warn('[网络响应] 等待登录响应超时:', e.message);
    return null;
  });

  // 先点击登录按钮（这是关键：先执行点击动作）
  console.log('[测试] 点击登录按钮...');
  await loginButton.click({ force: true });
  
  // 等待一小段时间，确保请求已发送
  await page.waitForTimeout(500);
  
  // 检查请求是否已发送
  if (!requestSent) {
    console.warn('[测试] 警告：登录请求可能未发送，等待更长时间...');
    await page.waitForTimeout(1000);
  }
  
  // 然后等待 API 响应
  console.log('[测试] 等待登录 API 响应...');
  try {
    const response = await responsePromise;
    if (response) {
      const responseData = await response.json();
      console.log('[测试] 登录 API 响应数据:', responseData);
    } else {
      console.warn('[测试] 登录响应未收到，但继续执行...');
    }
  } catch (e) {
    console.warn('[测试] 等待登录响应时出错:', e.message);
    // 不抛出错误，继续执行，让后续的 token 检查来判断是否成功
  }
  
  // 等待 token 设置（轮询方式，更可靠）
  let token = null;
  for (let i = 0; i < 30; i++) {
    token = await page.evaluate(() => localStorage.getItem('token'));
    if (token) {
      break;
    }
    await page.waitForTimeout(500);
  }
  
  if (!token) {
    throw new Error('登录失败：未找到 token，可能登录未成功');
  }
  
  // 等待登录成功后的 UI 标志：检测"退出"按钮是否出现
  // 这比检测弹窗关闭更可靠，因为"退出"按钮的出现明确表示登录成功
  try {
    const logoutButton = page.locator('button:has-text("退出")').first();
    await logoutButton.waitFor({ state: 'visible', timeout: 10000 });
    console.log('[测试] 登录成功：检测到"退出"按钮');
  } catch (e) {
    // 如果"退出"按钮没出现，尝试检测用户标签（已登录状态）
    try {
      const userTag = page.locator('.el-tag').filter({ hasText: /已登录|admin|系统管理员/ }).first();
      await userTag.waitFor({ state: 'visible', timeout: 5000 });
      console.log('[测试] 登录成功：检测到用户标签');
    } catch (e2) {
      // 最后尝试：检测数据表格是否加载（不显示"未登录"错误）
      try {
        const table = page.locator('.el-table').first();
        await table.waitFor({ state: 'visible', timeout: 5000 });
        const errorAlert = page.locator('.el-alert:has-text("未登录：请先登录后再加载数据")').first();
        const hasError = await errorAlert.count();
        if (hasError === 0) {
          console.log('[测试] 登录成功：检测到数据表格且无错误提示');
        } else {
          throw new Error('仍有未登录错误提示');
        }
      } catch (e3) {
        console.warn('[测试] 等待登录成功标志超时，但 token 已存在，继续执行...');
      }
    }
  }
  
  // 等待页面稳定
  await page.waitForTimeout(1000);

  return token;
}

// 等待元素可见
async function waitForElement(page, selector, timeout = 10000) {
  await page.waitForSelector(selector, { state: 'visible', timeout });
}

// 等待 API 请求完成
async function waitForAPIResponse(page, urlPattern, timeout = 30000) {
  await page.waitForResponse(
    (response) => response.url().includes(urlPattern) && response.status() === 200,
    { timeout }
  );
}

// 导出所有函数和常量
module.exports = {
  API_BASE_URL,
  TEST_USERS,
  waitForAPI,
  login,
  waitForElement,
  waitForAPIResponse,
};

