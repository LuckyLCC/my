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

  // 等待登录表单出现
  await page.waitForSelector('input[placeholder*="用户名"], input[type="text"]', { timeout: 10000 });

  // 查找并填写用户名
  const usernameInput = page.locator('input[placeholder*="用户名"], input[type="text"]').first();
  await usernameInput.fill(username);

  // 查找并填写密码
  const passwordInput = page.locator('input[type="password"]').first();
  await passwordInput.fill(password);

  // 点击登录按钮
  const loginButton = page.locator('button:has-text("登录"), button.el-button--primary').first();
  await loginButton.click();

  // 等待登录完成（检查是否跳转到主页面或 token 是否存储）
  await page.waitForTimeout(2000);
  
  // 验证登录成功（检查 localStorage 中是否有 token）
  const token = await page.evaluate(() => localStorage.getItem('token'));
  if (!token) {
    throw new Error('登录失败：未找到 token');
  }

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

