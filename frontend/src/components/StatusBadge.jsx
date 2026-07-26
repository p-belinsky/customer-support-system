const STATUS_STYLES = {
  NEW: { label: 'New', className: 'bg-blue-100 text-blue-800' },
  AI_RESPONDED: { label: 'AI Responded', className: 'bg-purple-100 text-purple-800' },
  NEEDS_REVIEW: { label: 'Needs Review', className: 'bg-amber-100 text-amber-800' },
  ESCALATED: { label: 'Escalated', className: 'bg-red-100 text-red-800' },
  PENDING_CUSTOMER: { label: 'Pending Customer', className: 'bg-yellow-100 text-yellow-800' },
  RESOLVED: { label: 'Resolved', className: 'bg-green-100 text-green-800' },
  CLOSED: { label: 'Closed', className: 'bg-gray-200 text-gray-700' },
}

const FALLBACK_STYLE = { label: 'Unknown', className: 'bg-gray-100 text-gray-600' }

export const TICKET_STATUSES = Object.keys(STATUS_STYLES)

function StatusBadge({ status }) {
  const { label, className } = STATUS_STYLES[status] ?? FALLBACK_STYLE

  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${className}`}>
      {label}
    </span>
  )
}

export default StatusBadge
