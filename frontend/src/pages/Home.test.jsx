import { render, screen, waitFor } from '@testing-library/react'
import Home from './Home'
import * as dashboardApi from '../api/dashboard'

vi.mock('../api/dashboard')

beforeEach(() => {
  vi.clearAllMocks()
})

const FULL_METRICS = {
  totalTickets: 5,
  resolutionRatePercent: 20,
  aiHandledCount: 1,
  humanHandledCount: 2,
  byStatus: [
    { status: 'NEW', count: 1 },
    { status: 'AI_RESPONDED', count: 1 },
    { status: 'NEEDS_REVIEW', count: 1 },
    { status: 'ESCALATED', count: 0 },
    { status: 'PENDING_CUSTOMER', count: 0 },
    { status: 'RESOLVED', count: 1 },
    { status: 'CLOSED', count: 0 },
  ],
  byCategory: [
    { category: 'Billing', count: 2 },
    { category: 'Technical', count: 2 },
    { category: 'General', count: 1 },
  ],
}

describe('Home', () => {
  it('shows a loading state before the metrics resolve', () => {
    dashboardApi.getDashboardMetrics.mockReturnValue(new Promise(() => {}))

    render(<Home />)

    expect(screen.getByText('Loading…')).toBeInTheDocument()
  })

  it('renders stat tiles once metrics resolve', async () => {
    dashboardApi.getDashboardMetrics.mockResolvedValue(FULL_METRICS)

    render(<Home />)

    await waitFor(() => expect(screen.getByText('5')).toBeInTheDocument())
    expect(screen.getByText('20%')).toBeInTheDocument()
    expect(screen.getByText('AI Handled')).toBeInTheDocument()
    expect(screen.getByText('Human Handled')).toBeInTheDocument()
  })

  it('shows the empty state when there are no tickets', async () => {
    dashboardApi.getDashboardMetrics.mockResolvedValue({
      totalTickets: 0,
      resolutionRatePercent: null,
      aiHandledCount: 0,
      humanHandledCount: 0,
      byStatus: [],
      byCategory: [],
    })

    render(<Home />)

    await waitFor(() => expect(screen.getByText(/No tickets yet/)).toBeInTheDocument())
  })

  it('shows an error message when the metrics request fails', async () => {
    dashboardApi.getDashboardMetrics.mockRejectedValue({ response: { data: { error: 'Failed to load dashboard' } } })

    render(<Home />)

    await waitFor(() => expect(screen.getByText('Failed to load dashboard')).toBeInTheDocument())
  })
})
