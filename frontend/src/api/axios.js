import axios from 'axios'

export const UNAUTHORIZED_EVENT = 'auth:unauthorized'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  withCredentials: true,
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      window.dispatchEvent(new Event(UNAUTHORIZED_EVENT))
    } else if (!error.response) {
      console.error('Network error calling API:', error.message)
    } else {
      console.error(`API error ${error.response.status} on ${error.config?.url}:`, error.response.data)
    }
    return Promise.reject(error)
  },
)

export default api
