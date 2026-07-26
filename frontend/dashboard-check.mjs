import { chromium } from 'playwright'

const browser = await chromium.launch({ args: ['--no-sandbox'] })
const page = await (await browser.newContext()).newPage()

await page.goto('http://localhost:5174/login')
await page.getByLabel('Username').fill('admin')
await page.getByLabel('Password').fill('admin123')
await page.getByRole('button', { name: 'Sign in' }).click()

await page.waitForSelector('h1:has-text("Dashboard")', { timeout: 15000 })
await page.waitForSelector('text=Total Tickets', { timeout: 15000 })
await page.waitForTimeout(1000)

const info = await page.evaluate(() => {
  const cards = [...document.querySelectorAll('h2')].map((h) => h.textContent)
  const categoryHeading = [...document.querySelectorAll('h2')].find((h) => h.textContent.includes('Category'))
  const chartCard = categoryHeading.parentElement
  const svg = chartCard.querySelector('svg:has(.recharts-pie)')
  const rect = svg.getBoundingClientRect()
  const texts = [...svg.querySelectorAll('text')].map((t) => ({ text: t.textContent, x: t.getAttribute('x'), y: t.getAttribute('y') }))
  return { cards, svgRect: { w: rect.width, h: rect.height }, viewBox: svg.getAttribute('viewBox'), texts, outerHTMLSnippet: svg.outerHTML.slice(0, 500) }
})
console.log(JSON.stringify(info, null, 2))

await browser.close()
