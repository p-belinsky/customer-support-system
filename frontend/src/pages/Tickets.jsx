import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listCategories, listTickets } from '../api/tickets'
import StatusBadge, { TICKET_STATUSES } from '../components/StatusBadge'
import CategoryBadge from '../components/CategoryBadge'
import { formatDate } from '../utils/format'

function Tickets() {
  const [tickets, setTickets] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [statusFilter, setStatusFilter] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('')
  const [categories, setCategories] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  useEffect(() => {
    listCategories()
      .then(setCategories)
      .catch(() => setCategories([]))
  }, [])

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    listTickets({ status: statusFilter || undefined, category: categoryFilter || undefined, page })
      .then((data) => {
        if (cancelled) return
        setTickets(data.content)
        setTotalPages(data.totalPages)
      })
      .catch((err) => {
        if (cancelled) return
        setError(err.response?.data?.error || 'Failed to load tickets')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [statusFilter, categoryFilter, page])

  function handleStatusChange(e) {
    setStatusFilter(e.target.value)
    setPage(0)
  }

  function handleCategoryChange(e) {
    setCategoryFilter(e.target.value)
    setPage(0)
  }

  return (
    <div>
      <div className='items-center gap-2'>
        <h1 className="text-2xl font-semibold text-gray-900 py-1">Tickets</h1>

          <div className="flex items-center gap-2">
            <select
                value={statusFilter}
                onChange={handleStatusChange}
                className="rounded border border-gray-300 px-3 py-2 text-sm"
            >
              <option value="">All statuses</option>
              {TICKET_STATUSES.map((status) => (
                  <option key={status} value={status}>
                    {status}
                  </option>
              ))}
            </select>

            <select
                value={categoryFilter}
                onChange={handleCategoryChange}
                className="rounded border border-gray-300 px-3 py-2 text-sm"
            >
              <option value="">All categories</option>
              {categories.map((category) => (
                  <option key={category} value={category}>
                    {category}
                  </option>
              ))}
            </select>

        </div>

      </div>

      {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

      {loading ? (
        <p className="mt-4 text-sm text-gray-600">Loading…</p>
      ) : tickets.length === 0 ? (
        <p className="mt-4 text-sm text-gray-600">No tickets found.</p>
      ) : (
        <div className="mt-4 overflow-hidden rounded-lg border border-gray-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-gray-50 text-gray-600">
              <tr>
                <th className="px-4 py-2 font-medium">Customer</th>
                <th className="px-4 py-2 font-medium">Subject</th>
                <th className="px-4 py-2 font-medium">Status</th>
                <th className="px-4 py-2 font-medium">Category</th>
                <th className="px-4 py-2 font-medium">Created</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {tickets.map((ticket) => (
                <tr key={ticket.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-gray-600">
                    <p className='font-bold'>{ticket.customerName}</p>
                    <p className="font-light">{ticket.customerEmail}</p>
                  </td>
                  <td className="px-4 py-3">
                    <Link to={`/dashboard/tickets/${ticket.id}`} className="font-light text-gray-900 hover:underline">
                      {ticket.subject}
                    </Link>
                  </td>
                  <td className="px-4 py-3">
                    <StatusBadge status={ticket.status} />
                  </td>
                  <td className="px-4 py-3">
                    <CategoryBadge category={ticket.category} />
                  </td>
                  <td className="px-4 py-3 text-gray-600">{formatDate(ticket.lastMessageAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="mt-4 flex items-center justify-end gap-2">
        <button
          onClick={() => setPage((p) => Math.max(0, p - 1))}
          disabled={page <= 0}
          className="rounded px-3 py-1.5 text-sm font-medium text-gray-600 hover:bg-gray-100 disabled:opacity-40"
        >
          Prev
        </button>
        <span className="text-sm text-gray-500">
          Page {page + 1} of {Math.max(totalPages, 1)}
        </span>
        <button
          onClick={() => setPage((p) => p + 1)}
          disabled={page + 1 >= totalPages}
          className="rounded px-3 py-1.5 text-sm font-medium text-gray-600 hover:bg-gray-100 disabled:opacity-40"
        >
          Next
        </button>
      </div>
    </div>
  )
}

export default Tickets
