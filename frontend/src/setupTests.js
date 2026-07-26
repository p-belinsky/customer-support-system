import '@testing-library/jest-dom'

// jsdom doesn't implement ResizeObserver, which Recharts' ResponsiveContainer requires.
global.ResizeObserver = class ResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

// jsdom reports 0x0 element sizes, which makes ResponsiveContainer render nothing.
Object.defineProperty(HTMLElement.prototype, 'offsetWidth', { configurable: true, value: 500 })
Object.defineProperty(HTMLElement.prototype, 'offsetHeight', { configurable: true, value: 300 })
