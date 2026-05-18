import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['token'] = token
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else {
      const error = new Error(res.message || '请求失败')
      error.response = response
      return Promise.reject(error)
    }
  },
  error => {
    if (error.response && error.response.data) {
      error.message = error.response.data.message || '请求异常'
    }
    return Promise.reject(error)
  }
)

export default request
