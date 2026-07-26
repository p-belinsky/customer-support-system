import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getTicket, sendReply, updateStatus } from '../api/tickets'
import StatusBadge, { TICKET_STATUSES } from '../components/StatusBadge'
import { formatDateTime } from '../utils/format'

function TicketDetail() {
  const { ticketId } = useParams()
  const [ticket, setTicket] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [replyBody, setReplyBody] = useState('')
  const [replyError, setReplyError] = useState(null)
  const [sending, setSending] = useState(false)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    getTicket(ticketId)
      .then((data) => {
        if (!cancelled) setTicket(data)
      })
      .catch(() => {
        if (!cancelled) setError('Failed to load ticket')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [ticketId])

  async function handleStatusChange(e) {
    const newStatus = e.target.value
    const previousStatus = ticket.status
    setTicket((t) => ({ ...t, status: newStatus }))
    try {
      await updateStatus(ticketId, newStatus)
    } catch {
      setTicket((t) => ({ ...t, status: previousStatus }))
    }
  }

  async function handleSendReply(e) {
    e.preventDefault()
    if (!replyBody.trim()) return

    setSending(true)
    setReplyError(null)
    try {
      const message = await sendReply(ticketId, replyBody)
      setTicket((t) => ({ ...t, messages: [...t.messages, message] }))
      setReplyBody('')
    } catch {
      setReplyError('Failed to send reply. Please try again.')
    } finally {
      setSending(false)
    }
  }

  if (loading) return <p className="text-sm text-gray-600">Loading…</p>
  if (error) return <p className="text-sm text-red-600">{error}</p>
  if (!ticket) return null

  return (
    <div>
      <Link to="/dashboard/tickets" className="text-sm text-gray-500 hover:underline">
        ← Back to tickets
      </Link>

      <div className="mt-3 flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">{ticket.subject}</h1>
          <p className="mt-1 text-sm text-gray-600">
            {ticket.customerName ? `${ticket.customerName} · ` : ''}
            {ticket.customerEmail}
          </p>
        </div>

        <div className="flex items-center gap-2">
          <StatusBadge status={ticket.status} />
          <select
            value={ticket.status}
            onChange={handleStatusChange}
            className="rounded border border-gray-300 px-2 py-1.5 text-sm"
          >
            {TICKET_STATUSES.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="mt-6 flex flex-col gap-3">
        {ticket.messages.map((message) => (
          <div
            key={message.id}
            className={`max-w-2xl rounded-lg border p-3 text-sm ${
              message.direction === 'OUTBOUND'
                ? 'ml-auto border-gray-900 bg-gray-900 text-white'
                : 'border-gray-200 bg-white text-gray-900'
            }`}
          >
            <p className="whitespace-pre-wrap">{message.body}</p>
            <p className={`mt-2 text-xs ${message.direction === 'OUTBOUND' ? 'text-gray-300' : 'text-gray-500'}`}>
              {message.senderEmail} · {formatDateTime(message.createdAt)}
            </p>
          </div>
        ))}
      </div>

      <form onSubmit={handleSendReply} className="mt-6 rounded-lg border border-gray-200 bg-white p-4">
        <label htmlFor="reply" className="block text-sm font-medium text-gray-900">
          Reply to customer
        </label>
        <textarea
          id="reply"
          value={replyBody}
          onChange={(e) => setReplyBody(e.target.value)}
          rows={4}
          className="mt-2 w-full rounded border border-gray-300 px-3 py-2 text-sm"
          disabled={sending}
        />
        {replyError && <p className="mt-2 text-sm text-red-600">{replyError}</p>}
        <button
          type="submit"
          disabled={sending || !replyBody.trim()}
          className="mt-3 rounded bg-gray-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {sending ? 'Sending…' : 'Send reply'}
        </button>
      </form>
    </div>
  )
}

export default TicketDetail
