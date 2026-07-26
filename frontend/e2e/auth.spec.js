import { test, expect } from '@playwright/test'

test('redirects unauthenticated visitors to /login', async ({ page }) => {
  await page.goto('/')

  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('heading', { name: 'Admin Login' })).toBeVisible()
})

test('logs in, persists across reload, navigates dashboard pages, and logs out', async ({ page }) => {
  await page.goto('/login')

  await page.getByLabel('Username').fill('admin')
  await page.getByLabel('Password').fill('wrong')
  await page.getByRole('button', { name: 'Sign in' }).click()
  await expect(page.getByText('Invalid username or password')).toBeVisible()
  await expect(page).toHaveURL(/\/login$/)

  await page.getByLabel('Username').fill('admin')
  await page.getByLabel('Password').fill('admin123')
  await page.getByRole('button', { name: 'Sign in' }).click()

  await expect(page).toHaveURL(/\/dashboard\/home$/)
  await expect(page.getByRole('heading', { name: 'Home' })).toBeVisible()

  // Session must survive a reload (cookie + AuthContext's me() check), not just client state.
  await page.reload()
  await expect(page).toHaveURL(/\/dashboard\/home$/)
  await expect(page.getByRole('heading', { name: 'Home' })).toBeVisible()

  await page.getByRole('link', { name: 'Tickets' }).click()
  await expect(page).toHaveURL(/\/dashboard\/tickets$/)
  await expect(page.getByRole('heading', { name: 'Tickets' })).toBeVisible()

  await page.getByRole('button', { name: 'Log out' }).click()
  await expect(page).toHaveURL(/\/login$/)

  // Session must be invalidated server-side, not just cleared client-side.
  await page.goto('/dashboard')
  await expect(page).toHaveURL(/\/login$/)
})
