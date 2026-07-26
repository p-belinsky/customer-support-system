// Palette intentionally avoids blue/purple/amber/red/yellow/green/gray — those are used by StatusBadge.
const CATEGORY_STYLES = [
  'bg-teal-100 text-teal-800',
  'bg-pink-100 text-pink-800',
  'bg-cyan-100 text-cyan-800',
  'bg-orange-100 text-orange-800',
  'bg-lime-100 text-lime-800',
  'bg-fuchsia-100 text-fuchsia-800',
  'bg-rose-100 text-rose-800',
  'bg-indigo-100 text-indigo-800',
]

function hashString(value) {
  let hash = 0
  for (let i = 0; i < value.length; i++) {
    hash = (hash * 31 + value.charCodeAt(i)) >>> 0
  }
  return hash
}

function CategoryBadge({ category }) {
  if (!category) {
    return <span className="text-gray-400">—</span>
  }

  const className = category.toLowerCase() === 'general'
    ? 'bg-gray-200 text-black'
    : CATEGORY_STYLES[hashString(category) % CATEGORY_STYLES.length]

  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${className}`}>
      {category}
    </span>
  )
}

export default CategoryBadge
