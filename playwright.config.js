// @ts-check
const { defineConfig, devices } = require('@playwright/test');

/**
 * Playwright 配置文件
 * 用于健身房会员管理系统的全链路端到端测试
 */
module.exports = defineConfig({
  testDir: './e2e',
  /* 并行运行测试的最大工作进程数 */
  fullyParallel: true,
  /* 如果测试失败，是否禁止重试 */
  forbidOnly: !!process.env.CI,
  /* 在 CI 环境中重试失败的测试 */
  retries: process.env.CI ? 2 : 0,
  /* 并行运行的工作进程数 */
  workers: process.env.CI ? 1 : 1,
  /* 测试报告配置 */
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['list'],
    ['json', { outputFile: 'playwright-report/results.json' }]
  ],
  /* 共享测试配置 */
  use: {
    /* 基础 URL */
    baseURL: 'http://localhost:5173',
    /* 收集失败时的跟踪信息 */
    trace: 'on-first-retry',
    /* 截图配置 */
    screenshot: 'only-on-failure',
    /* 视频录制 */
    video: 'retain-on-failure',
    /* 操作超时时间 */
    actionTimeout: 10000,
    /* 导航超时时间 */
    navigationTimeout: 30000,
  },

  /* 配置测试项目 */
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    // 可以添加更多浏览器
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },
    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] },
    // },
  ],

  /* 运行本地开发服务器 */
  webServer: [
    {
      command: 'cd frontend && python3 -m http.server 5173',
      port: 5173,
      reuseExistingServer: !process.env.CI,
      timeout: 120 * 1000,
      stdout: 'pipe',
      stderr: 'pipe',
    },
    // 注意：后端服务需要手动启动，或者使用 Maven 命令
    // 这里不自动启动后端，因为需要数据库连接等复杂配置
  ],
});

