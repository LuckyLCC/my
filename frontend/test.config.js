// 前端测试配置
module.exports = {
  // 测试环境配置
  testEnvironment: 'node',
  // 测试文件匹配模式
  testMatch: [
    '**/test/**/*.test.js',
    '**/test/**/*.spec.js',
    '**/__tests__/**/*.js'
  ],
  // 预设处理程序
  transform: {
    '^.+\\.js$': 'babel-jest'
  },
  // 模块文件扩展名
  moduleFileExtensions: ['js', 'json'],
  // 测试超时时间
  testTimeout: 30000,
  // 报告器
  reporters: ['default', 'jest-junit'],
  // 测试结果输出目录
  testResultsProcessor: 'jest-junit'
};