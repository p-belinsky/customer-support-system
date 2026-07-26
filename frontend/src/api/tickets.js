import api from './axios'

export function listTickets({ status, category, page = 0, size = 20 } = {}) {
  return api.get('/tickets', { params: { status, category, page, size } }).then((res) => res.data)
}

export function listCategories() {
  return api.get('/tickets/categories').then((res) => res.data)
}

export function getTicket(ticketId) {
  return api.get(`/tickets/${ticketId}`).then((res) => res.data)
}

export function sendReply(ticketId, body) {
  return api.post(`/tickets/${ticketId}/messages`, { body }).then((res) => res.data)
}

export function updateStatus(ticketId, status) {
  return api.patch(`/tickets/${ticketId}/status`, { status }).then((res) => res.data)
}
