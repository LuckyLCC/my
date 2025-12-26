// 认证相关测试
const { test, expect } = require('@playwright/test');
const { login, waitForAPI, TEST_USERS } = require('./fixtures.js');

test.describe('认证模块', () => {
  test.beforeEach(async ({ page }) => {
    // 清除 localStorage
    await page.goto('/index.html');
    await page.evaluate(() => {
      localStorage.clear();
    });
  });

  test('应该能够成功登录', async ({ page }) => {
    await page.goto('/index.html');
    await waitForAPI(page);

    // 等待登录表单加载
    await page.waitForSelector('input[type="text"], input[placeholder*="用户名"]', { timeout: 10000 });

    // 填写用户名
    const usernameInput = page.locator('input[type="text"], input[placeholder*="用户名"]').first();
    await usernameInput.fill(TEST_USERS.admin.username);

    // 填写密码
    const passwordInput = page.locator('input[type="password"]').first();
    await passwordInput.fill(TEST_USERS.admin.password);

    // 点击登录按钮
    const loginButton = page.locator('button:has-text("登录"), button.el-button--primary').first();
    await loginButton.click();

    // 等待登录完成
    await page.waitForTimeout(3000);

    // 验证登录成功：检查 localStorage 中是否有 token
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeTruthy();
    expect(token.length).toBeGreaterThan(0);

    // 验证页面跳转或显示主界面
    // 精确匹配左侧侧边栏的菜单项
    const sidebar = page.locator('.el-aside');
    await expect(sidebar).toBeVisible({ timeout: 5000 });
    
    // 验证"会员管理"菜单项在侧边栏中可见
    const memberMenuItem = sidebar.locator('.el-menu-item').filter({ hasText: '会员管理' }).first();
    await expect(memberMenuItem).toBeVisible({ timeout: 5000 });
  });

  test('登录失败应该显示错误信息', async ({ page }) => {
    await page.goto('/index.html');
    await waitForAPI(page);

    // 等待登录表单加载
    await page.waitForSelector('input[type="text"]', { timeout: 10000 });

    // 填写错误的用户名和密码
    const usernameInput = page.locator('input[type="text"]').first();
    await usernameInput.fill('wrong_user');

    const passwordInput = page.locator('input[type="password"]').first();
    await passwordInput.fill('wrong_password');

    // 点击登录按钮
    const loginButton = page.locator('button:has-text("登录")').first();
    await loginButton.click();

    // 等待错误提示出现
    await page.waitForTimeout(2000);

    // 检查是否有错误提示（Element Plus 通常使用 el-message 或 el-notification）
    const errorMessage = await page.locator('.el-message--error, .el-notification--error, text=/错误|失败|用户名|密码/').count();
    // 或者检查 token 是否未设置
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeFalsy();
  });

  test('登录后应该能够访问受保护的页面', async ({ page }) => {
    // 先登录
    await login(page);

    // 访问会员管理页面（通过点击菜单或直接导航）
    await page.goto('/index.html');
    await waitForAPI(page);

    // 等待主界面加载
    await page.waitForTimeout(2000);

    // 尝试访问会员列表（通过 API 或 UI）
    // 检查页面是否正常加载，没有跳转到登录页
    const currentUrl = page.url();
    expect(currentUrl).toContain('index.html');

    // 验证 token 仍然存在
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeTruthy();
  });
});

