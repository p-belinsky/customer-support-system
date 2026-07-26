import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const navLinkClass = ({ isActive }) =>
  `rounded px-3 py-2 text-sm font-medium ${
    isActive ? 'bg-gray-900 text-white' : 'text-gray-600 hover:bg-gray-100'
  }`

function DashboardLayout() {
  const { logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="flex items-center justify-between border-b border-gray-200 bg-white px-6 py-3">
        <nav className="flex gap-2">
          <div className='pr-6 pt-2'>
            <h1 className='font-medium'>Support Admin</h1>
          </div>
          <NavLink to="/dashboard/home" className={navLinkClass}>
            Home
          </NavLink>
          <NavLink to="/dashboard/tickets" className={navLinkClass}>
            Tickets
          </NavLink>
        </nav>
        <button
          onClick={handleLogout}
          className="rounded px-3 py-2 text-sm font-medium text-gray-600 hover:bg-gray-100"
        >
          Log out
        </button>
      </header>

      <main className="p-6">
        <Outlet />
      </main>
    </div>
  )
}

export default DashboardLayout
