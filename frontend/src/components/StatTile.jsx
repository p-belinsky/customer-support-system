function StatTile({ label, value, hint }) {
  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4">
      <p className="text-sm text-gray-500">{label}</p>
      <p className="text-2xl font-semibold text-gray-900">{value ?? '—'}</p>
      {hint && <p className="text-xs text-gray-400">{hint}</p>}
    </div>
  )
}

export default StatTile
