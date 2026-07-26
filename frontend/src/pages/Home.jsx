import { useEffect, useState } from 'react'
import { Bar, BarChart, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { getDashboardMetrics } from '../api/dashboard'
import StatTile from '../components/StatTile'
import ChartCard from '../components/ChartCard'

const STATUS_DISPLAY = {
  NEW: { label: 'Open', color: '#3b82f6' },
  AI_RESPONDED: { label: 'AI Responded', color: '#a855f7' },
  NEEDS_REVIEW: { label: 'In Progress', color: '#f59e0b' },
  ESCALATED: { label: 'Pending Human', color: '#ef4444' },
  PENDING_CUSTOMER: { label: 'Pending Customer', color: '#eab308' },
  RESOLVED: { label: 'Resolved', color: '#22c55e' },
  CLOSED: { label: 'Closed', color: '#6b7280' },
}

// Same palette family as CategoryBadge.jsx, so a category's chart color matches its badge color elsewhere in the app.
const CATEGORY_COLORS = ['#14b8a6', '#ec4899', '#06b6d4', '#f97316', '#84cc16', '#d946ef', '#f43f5e', '#6366f1']

function hashString(value) {
  let hash = 0
  for (let i = 0; i < value.length; i++) {
    hash = (hash * 31 + value.charCodeAt(i)) >>> 0
  }
  return hash
}

function Home() {
  const [metrics, setMetrics] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    getDashboardMetrics()
      .then((data) => {
        if (cancelled) return
        setMetrics(data)
      })
      .catch((err) => {
        if (cancelled) return
        setError(err.response?.data?.error || 'Failed to load dashboard')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  return (
    <div>
      <h1 className="text-2xl font-semibold text-gray-900">Dashboard</h1>

      {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

      {loading ? (
        <p className="mt-4 text-sm text-gray-600">Loading…</p>
      ) : metrics && metrics.totalTickets === 0 ? (
        <p className="mt-4 text-sm text-gray-600">No tickets yet — metrics will appear once tickets start coming in.</p>
      ) : metrics ? (
        <>
          <div className="mt-4 grid grid-cols-2 gap-4 lg:grid-cols-4">
            <StatTile label="Total Tickets" value={metrics.totalTickets} />
            <StatTile
              label="Resolution Rate"
              value={metrics.resolutionRatePercent == null ? null : `${Math.round(metrics.resolutionRatePercent)}%`}
            />
            <StatTile label="AI Handled" value={metrics.aiHandledCount} hint="AI Responded" />
            <StatTile
              label="Human Handled"
              value={metrics.humanHandledCount}
              hint="Reviewed, escalated, pending, resolved, or closed"
            />
          </div>

          <div className="mt-4 grid grid-cols-1 gap-4 lg:grid-cols-2">
            <ChartCard title="Tickets by Status">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={metrics.byStatus
                    .filter((s) => s.count > 0)
                    .map((s) => ({ ...STATUS_DISPLAY[s.status], count: s.count }))}
                  margin={{ bottom: 32 }}
                >
                  <XAxis
                    dataKey="label"
                    tick={{ fontSize: 11 }}
                    interval={0}
                    angle={-30}
                    textAnchor="end"
                    height={50}
                  />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="count">
                    {metrics.byStatus
                      .filter((s) => s.count > 0)
                      .map((s) => (
                        <Cell key={s.status} fill={STATUS_DISPLAY[s.status].color} />
                      ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </ChartCard>

            <ChartCard title="Tickets by Category">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={metrics.byCategory.map((c) => ({ name: c.category, value: c.count }))}
                    dataKey="value"
                    nameKey="name"
                    label={({ name, percent }) => `${name} ${Math.round(percent * 100)}%`}
                    labelLine={false}
                  >
                    {metrics.byCategory.map((c) => (
                      <Cell
                        key={c.category}
                        fill={
                          c.category?.toLowerCase() === 'general'
                            ? '#e5e7eb'
                            : CATEGORY_COLORS[hashString(c.category) % CATEGORY_COLORS.length]
                        }
                      />
                    ))}
                  </Pie>
                  <Legend />
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </ChartCard>
          </div>
        </>
      ) : null}
    </div>
  )
}

export default Home
