function ChartCard({ title, children }) {
  return (
    <div className="rounded-lg border border-gray-200 bg-white p-6">
      <h2 className="text-sm font-medium text-gray-900">{title}</h2>
      <div className="mt-4 h-64">{children}</div>
    </div>
  )
}

export default ChartCard
