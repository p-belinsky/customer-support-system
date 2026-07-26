import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import App from './App'
import * as authApi from './api/auth'
import * as dashboardApi from './api/dashboard'

vi.mock('./api/auth')
vi.mock('./api/dashboard')

beforeEach(() => {
  vi.clearAllMocks()
  window.history.pushState({}, '', '/')
})

describe('App', () => {
  it('redirects unauthenticated visitors to the login page', async () => {
    authApi.me.mockRejectedValue(new Error('401'))

    render(<App />)

    expect(await screen.findByRole('heading', { name: 'Admin Login' })).toBeInTheDocument()
  })

  it('shows an error on failed login and stays on the login page', async () => {
    authApi.me.mockRejectedValue(new Error('401'))
    authApi.login.mockRejectedValue({ response: { data: { error: 'Invalid username or password' } } })

    render(<App />)
    await screen.findByRole('heading', { name: 'Admin Login' })

    await userEvent.type(screen.getByLabelText('Username'), 'admin')
    await userEvent.type(screen.getByLabelText('Password'), 'wrong')
    await userEvent.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByText('Invalid username or password')).toBeInTheDocument()
  })

  it('logs in, navigates dashboard pages, and logs out', async () => {
    authApi.me.mockRejectedValue(new Error('401'))
    authApi.login.mockResolvedValue({ username: 'admin' })
    authApi.logout.mockResolvedValue({})
    dashboardApi.getDashboardMetrics.mockResolvedValue({
      totalTickets: 0,
      resolutionRatePercent: null,
      aiHandledCount: 0,
      humanHandledCount: 0,
      byStatus: [],
      byCategory: [],
    })

    render(<App />)
    await screen.findByRole('heading', { name: 'Admin Login' })

    await userEvent.type(screen.getByLabelText('Username'), 'admin')
    await userEvent.type(screen.getByLabelText('Password'), 'admin123')
    await userEvent.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByRole('heading', { name: 'Dashboard' })).toBeInTheDocument()

    await userEvent.click(screen.getByRole('link', { name: 'Tickets' }))
    expect(await screen.findByRole('heading', { name: 'Tickets' })).toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: 'Log out' }))
    expect(await screen.findByRole('heading', { name: 'Admin Login' })).toBeInTheDocument()
  })
})
