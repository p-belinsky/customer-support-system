import api from './axios'

export function login(username, password) {
  return api
    .post('/auth/login', new URLSearchParams({ username, password }), {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    })
    .then((res) => res.data)
}

export function logout() {
  return api.post('/auth/logout').then((res) => res.data)
}

export function me() {
  return api.get('/auth/me').then((res) => res.data)
}
