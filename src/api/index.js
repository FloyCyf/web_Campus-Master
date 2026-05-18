import request from '@/utils/request'

const normalizePage = (data) => ({
  ...data,
  list: data?.list || data?.records || []
})

const flowTypeMap = {
  freeze: '任务冻结',
  unfreeze: '冻结返还',
  income: '任务收入',
  outcome: '任务支出',
  recharge: '账户充值',
  withdraw: '提现'
}

const normalizeAmount = (flow) => {
  const amount = Number(flow.amount || 0)
  return ['freeze', 'outcome', 'withdraw'].includes(flow.type) ? -Math.abs(amount) : Math.abs(amount)
}

const normalizeTransactionPage = (data) => {
  const page = normalizePage(data)
  return {
    ...page,
    list: page.list.map(flow => ({
      ...flow,
      amount: normalizeAmount(flow),
      typeText: flowTypeMap[flow.type] || flow.type,
      remark: flow.description,
      createTime: flow.createTime
    }))
  }
}

export const userApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  getInfo: (userId) => request.get(`/auth/users/${userId}`)
}

export const taskApi = {
  getList: () => request.get('/tasks').then(normalizePage),
  getMyTasks: (...args) => {
    const type = args.length === 1 ? args[0] : args[1]
    return request.get(`/tasks/my/${type}`).then(normalizePage)
  },
  getDetail: (taskId) => request.get(`/tasks/${taskId}`),
  publish: (data) => request.post('/tasks', data),
  accept: (arg) => {
    const taskId = typeof arg === 'object' ? arg.taskId : arg
    return request.post(`/tasks/${taskId}/accept`)
  },
  submit: (taskId, data) => request.post(`/tasks/${taskId}/submit`, data),
  review: (taskId, data) => request.post(`/tasks/${taskId}/complete`, data),
  dispute: (taskId, data) => request.post(`/tasks/${taskId}/dispute`, data),
  cancel: (arg) => {
    const taskId = typeof arg === 'object' ? arg.taskId : arg
    return request.post(`/tasks/${taskId}/cancel`)
  },
  rate: (taskId, data) => request.post(`/tasks/${taskId}/rate`, data)
}

export const accountApi = {
  getInfo: () => request.get('/account'),
  getFundFlow: () => request.get('/account/transactions').then(normalizeTransactionPage),
  recharge: (amount) => request.post('/account/recharge', { amount })
}

export const reviewApi = {
  getReceived: () => request.get('/reviews/received'),
  getSent: () => request.get('/reviews/sent'),
  getTaskReviews: (taskId) => request.get(`/reviews/task/${taskId}`)
}

export const notificationApi = {
  getList: () => request.get('/notifications').then((data) => ({
    ...normalizePage(data),
    unreadCount: data?.unreadCount ?? (data?.records || data?.list || []).filter(item => item.isRead === 0).length
  })),
  markRead: () => request.put('/notifications/read-all')
}

export const adminApi = {
  getTasks: (params = {}) => request.get('/admin/tasks', { params }).then(normalizePage),
  getDisputes: (params = {}) => request.get('/admin/disputes', { params }).then(normalizePage),
  getUsers: (params = {}) => request.get('/admin/users', { params }).then(normalizePage),
  approveTask: (taskId, data = {}) => request.post(`/admin/tasks/${taskId}/approve`, data),
  takeDownTask: (taskId, data) => request.post(`/admin/tasks/${taskId}/take-down`, data),
  freezeUser: (userId) => request.post(`/admin/users/${userId}/freeze`),
  resolveDispute: (disputeId, data) => request.post(`/admin/disputes/${disputeId}/resolve`, data),
  getStatistics: () => request.get('/admin/stats')
}
