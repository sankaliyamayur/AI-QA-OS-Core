import { test, expect } from '@playwright/test';

test('AC-003: Verify Login Failure with Invalid Password', async ({ page }) => {
  await page.goto('http://localhost:3000/login');
  await page.locator('input[name="username"], #username').fill('admin');
  await page.locator('input[name="password"], #password').fill('wrongpassword');
  await page.locator('button[type="submit"], button:has-text("Login")').click();
  // Wait for the alert banner which will not appear (causing failure and taking screenshot)
  await page.locator('.alert-danger').waitFor({ timeout: 5000 });
});