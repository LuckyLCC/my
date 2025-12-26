import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('http://localhost:5173/index.html');
  await page.getByLabel('登录').getByRole('button', { name: '登录' }).click();
  await page.getByRole('button', { name: '退出' }).click();
  await page.getByRole('button', { name: '登录' }).click();
  await page.getByLabel('登录').getByRole('button', { name: '登录' }).click();
  await page.getByRole('button', { name: '新增' }).click();
  await page.getByLabel('姓名').click();
  await page.getByLabel('姓名').fill('3333');
  await page.getByLabel('手机号').click();
  await page.getByLabel('手机号').fill('33');
  await page.getByRole('textbox', { name: '身份证号' }).click();
  await page.getByLabel('手机号').click();
  await page.getByLabel('手机号').fill('333');
  await page.getByRole('textbox', { name: '身份证号' }).click();
  await page.getByRole('textbox', { name: '身份证号' }).fill('333');
  await page.getByText('姓名性别男手机号身份证号卡种季卡（¥10800').click();
  await page.getByRole('combobox', { name: '卡种' }).click();
  await page.getByText('半年卡（¥19800）').click();
  await page.getByRole('combobox', { name: '开始日期' }).click();
  await page.getByText('10').nth(3).click();
  await page.getByRole('button', { name: '保存' }).click();
});