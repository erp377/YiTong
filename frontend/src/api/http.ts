import axios from 'axios'
import { useAuthStore } from '../stores/auth'

export const http = axios.create({
  baseURL: ''
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

