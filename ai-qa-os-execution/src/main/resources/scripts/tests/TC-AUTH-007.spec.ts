import { test, expect } from '@playwright/test';

test('AC-007: Login fails with invalid Password', async ({ page }) => {
  await page.setViewportSize({ width: 1280, height: 720 });

  // 1. Navigate to OnePurpos Openings page
  await page.goto('https://onepurpos.in/openings', { waitUntil: 'domcontentloaded', timeout: 30000 });
  await page.waitForTimeout(3000);

  // 2. Click Login dropdown / button
  const loginBtn = page.getByRole('button', { name: /login/i }).or(page.getByRole('link', { name: /login/i })).first();
  if (await loginBtn.isVisible()) {
    await loginBtn.click();
    await page.waitForTimeout(1500);
  }

  // 3. Fill form if input fields are present
  const emailField = page.locator('input[type="email"], input[name="email"], input[name="username"]').first();
  if (await emailField.isVisible()) {
    await emailField.fill('shivam@yopamail.com');
  }

  const passField = page.locator('input[type="password"], input[name="password"]').first();
  if (await passField.isVisible()) {
    await passField.fill('WrongPassword123!');
  }

  await page.waitForTimeout(1500);

  // 4. Submit
  const submitBtn = page.locator('button[type="submit"], button:has-text("Log in"), button:has-text("Login")').first();
  if (await submitBtn.isVisible()) {
    await submitBtn.click();
  }

  await page.waitForTimeout(3000);

  // 5. Assert intentional test failure (AC-007)
  await expect(page.locator('div.error-message-non-existent')).toBeVisible({ timeout: 5000 });
});
