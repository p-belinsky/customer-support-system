import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AuthProvider, useAuth } from './AuthContext'
import * as authApi from '../api/auth'

vi.mock('../api/auth')

function Consumer() {
  const { user, logout } = useAuth()
  return (
    <div>
      <span data-testid="user">{user ? user.username : 'none'}</span>
      <button onClick={() => logout().catch(() => {})}>logout</button>
    </div>
  )
}

function LoginTrigger({ onError }) {
  const { login } = useAuth()
  return (
    <button onClick={() => login('admin', 'wrong').catch(onError)}>login</button>
  )
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('AuthProvider', () => {
  it('defaults to unauthenticated when the initial session check fails', async () => {
    authApi.me.mockRejectedValue(new Error('401'))

    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>,
    )

    await waitFor(() => expect(screen.getByTestId('user')).toHaveTextContent('none'))
  })

  it('sets the user on successful login', async () => {
    authApi.me.mockRejectedValue(new Error('401'))
    authApi.login.mockResolvedValue({ username: 'admin' })

    function LoginButton() {
      const { login } = useAuth()
      return <button onClick={() => login('admin', 'admin123')}>login</button>
    }

    render(
      <AuthProvider>
        <Consumer />
        <LoginButton />
      </AuthProvider>,
    )
    await waitFor(() => expect(screen.getByTestId('user')).toHaveTextContent('none'))

    await userEvent.click(screen.getByText('login'))

    await waitFor(() => expect(screen.getByTestId('user')).toHaveTextContent('admin'))
  })

  it('throws a readable error on failed login and keeps the user unset', async () => {
    authApi.me.mockRejectedValue(new Error('401'))
    authApi.login.mockRejectedValue({ response: { data: { error: 'Invalid username or password' } } })
    const onError = vi.fn()

    render(
      <AuthProvider>
        <Consumer />
        <LoginTrigger onError={onError} />
      </AuthProvider>,
    )
    await waitFor(() => expect(screen.getByTestId('user')).toHaveTextContent('none'))

    await userEvent.click(screen.getByText('login'))

    await waitFor(() => expect(onError).toHaveBeenCalled())
    expect(onError.mock.calls[0][0].message).toBe('Invalid username or password')
    expect(screen.getByTestId('user')).toHaveTextContent('none')
  })

  it('always clears the user on logout, even if the logout call fails', async () => {
    authApi.me.mockResolvedValue({ username: 'admin' })
    authApi.logout.mockRejectedValue(new Error('network error'))

    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>,
    )
    await waitFor(() => expect(screen.getByTestId('user')).toHaveTextContent('admin'))

    await userEvent.click(screen.getByText('logout'))

    await waitFor(() => expect(screen.getByTestId('user')).toHaveTextContent('none'))
  })
})
