import api from './axios'

export function getDashboardMetrics() {
  return api.get('/tickets/metrics').then((res) => res.data)
}
