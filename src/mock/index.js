import Mock from 'mockjs'

const Random = Mock.Random

const users = [
  { id: 1, username: 'admin', phone: '13800000000', password: 'Admin123', role: 'admin', creditScore: 100 },
  { id: 2, username: '张小明', phone: '13800000001', password: '123456', role: 'requester', creditScore: 98 },
  { id: 3, username: '李小红', phone: '13800000002', password: '123456', role: 'helper', creditScore: 95 },
  { id: 4, username: '王小华', phone: '13800000003', password: '123456', role: 'requester', creditScore: 100 },
  { id: 5, username: '赵小强', phone: '13800000004', password: '123456', role: 'helper', creditScore: 92 }
]

const tasks = [
  {
    id: 1,
    title: '帮忙取快递',
    description: '帮我去菜鸟驿站取一个快递，地址是西区菜鸟驿站，快递单号是SF1234567890',
    category: 'delivery',
    reward: 15.00,
    serviceFee: 0.75,
    status: 'pending',
    deadline: '2026-05-20 18:00:00',
    location: '西区菜鸟驿站',
    contactInfo: '13800000002',
    viewCount: 15,
    requesterId: 2,
    helperId: null,
    createTime: '2026-05-17 10:30:00'
  },
  {
    id: 2,
    title: '打印资料',
    description: '需要打印50页复习资料，双面打印，装订好送到教学楼A栋302室',
    category: 'print',
    reward: 20.00,
    serviceFee: 1.00,
    status: 'ongoing',
    deadline: '2026-05-17 16:00:00',
    location: '教学楼A栋302',
    contactInfo: '13800000003',
    viewCount: 8,
    requesterId: 4,
    helperId: 3,
    createTime: '2026-05-17 09:00:00'
  },
  {
    id: 3,
    title: '食堂带饭',
    description: '帮我从二食堂带一份麻辣香锅，微辣，加米饭，送到图书馆三楼',
    category: 'food',
    reward: 8.00,
    serviceFee: 0.40,
    status: 'pending_review',
    deadline: '2026-05-17 12:00:00',
    location: '图书馆三楼',
    contactInfo: '13800000001',
    viewCount: 20,
    requesterId: 2,
    helperId: 5,
    createTime: '2026-05-17 10:00:00'
  },
  {
    id: 4,
    title: '代取外卖',
    description: '外卖送到北门了，麻烦帮忙取一下送到宿舍3号楼402',
    category: 'delivery',
    reward: 10.00,
    serviceFee: 0.50,
    status: 'completed',
    deadline: '2026-05-16 20:00:00',
    location: '3号楼402',
    contactInfo: '13800000004',
    viewCount: 12,
    requesterId: 5,
    helperId: 3,
    createTime: '2026-05-16 18:30:00'
  },
  {
    id: 5,
    title: '帮忙占座',
    description: '下午2点的课，麻烦1点半去教学楼B栋201帮我占个座位',
    category: 'other',
    reward: 5.00,
    serviceFee: 0.25,
    status: 'pending',
    deadline: '2026-05-17 14:00:00',
    location: '教学楼B栋201',
    contactInfo: '13800000002',
    viewCount: 6,
    requesterId: 3,
    helperId: null,
    createTime: '2026-05-17 11:00:00'
  },
  {
    id: 6,
    title: '取火车票',
    description: '帮我去火车站自助取票机取一张明天早上8点的火车票',
    category: 'delivery',
    reward: 25.00,
    serviceFee: 1.25,
    status: 'disputed',
    deadline: '2026-05-16 18:00:00',
    location: '火车站',
    contactInfo: '13800000003',
    viewCount: 10,
    requesterId: 4,
    helperId: 5,
    createTime: '2026-05-16 14:00:00'
  }
]

const accounts = [
  { userId: 1, balance: 0.00, frozenBalance: 0.00 },
  { userId: 2, balance: 150.50, frozenBalance: 15.00 },
  { userId: 3, balance: 85.00, frozenBalance: 0.00 },
  { userId: 4, balance: 200.00, frozenBalance: 20.00 },
  { userId: 5, balance: 45.00, frozenBalance: 0.00 }
]

const fundFlows = [
  { id: 1, flowNo: 'F20260517001', userId: 2, taskId: 1, type: 'freeze', amount: -15.00, balanceAfter: 150.50, remark: '发布任务冻结', createTime: '2026-05-17 10:30:00' },
  { id: 2, flowNo: 'F20260517002', userId: 3, taskId: 4, type: 'income', amount: 10.00, balanceAfter: 85.00, remark: '完成任务收入', createTime: '2026-05-16 21:00:00' },
  { id: 3, flowNo: 'F20260517003', userId: 4, taskId: 2, type: 'freeze', amount: -20.00, balanceAfter: 200.00, remark: '发布任务冻结', createTime: '2026-05-17 09:00:00' },
  { id: 4, flowNo: 'F20260517004', userId: 5, taskId: 3, type: 'income', amount: 8.00, balanceAfter: 45.00, remark: '完成任务收入', createTime: '2026-05-17 12:30:00' }
]

const notifications = [
  { id: 1, userId: 2, type: 'task_accepted', title: '任务被接单', content: '您发布的"帮忙取快递"任务已被接单人接单', taskId: 1, isRead: 0, createTime: '2026-05-17 11:00:00' },
  { id: 2, userId: 3, type: 'task_submitted', title: '任务已提交', content: '您承接的"打印资料"任务已提交验收', taskId: 2, isRead: 0, createTime: '2026-05-17 15:00:00' },
  { id: 3, userId: 2, type: 'system', title: '系统通知', content: '您的信用分已更新为98分', taskId: null, isRead: 1, createTime: '2026-05-17 08:00:00' },
  { id: 4, userId: 5, type: 'dispute_created', title: '争议提醒', content: '您承接的"取火车票"任务被发起争议', taskId: 6, isRead: 0, createTime: '2026-05-16 17:30:00' }
]

const evaluations = [
  { id: 1, taskId: 4, fromUserId: 5, toUserId: 3, rating: 5, comment: '服务很及时，非常满意', tags: '及时,满意', createTime: '2026-05-16 21:30:00' },
  { id: 2, taskId: 3, fromUserId: 2, toUserId: 5, rating: 4, comment: '还不错，速度很快', tags: '快速', createTime: '2026-05-17 13:00:00' }
]

const adminLogs = [
  { id: 1, adminId: 1, operationType: 'task_approve', targetType: 'task', targetId: 1, detail: '审核通过任务"帮忙取快递"', ipAddress: '192.168.1.100', createTime: '2026-05-17 10:35:00' },
  { id: 2, adminId: 1, operationType: 'dispute_resolve', targetType: 'dispute', targetId: 1, detail: '处理争议任务"取火车票"', ipAddress: '192.168.1.100', createTime: '2026-05-16 18:00:00' }
]

Mock.mock('/api/auth/login', 'post', (req) => {
  const { phone, password } = JSON.parse(req.body)
  const user = users.find(u => u.phone === phone && u.password === password)
  if (user) {
    return {
      code: 200,
      message: '登录成功',
      data: {
        userId: user.id,
        username: user.username,
        phone: user.phone,
        role: user.role,
        creditScore: user.creditScore,
        token: `token_${user.id}_${Date.now()}`
      }
    }
  }
  return { code: 400, message: '手机号或密码错误' }
})

Mock.mock('/api/auth/register', 'post', (req) => {
  const { username, phone, password, role } = JSON.parse(req.body)
  const exists = users.find(u => u.phone === phone)
  if (exists) {
    return { code: 400, message: '手机号已注册' }
  }
  const newUser = {
    id: users.length + 1,
    username,
    phone,
    password,
    role: role || 'helper',
    creditScore: 100
  }
  users.push(newUser)
  return {
    code: 200,
    message: '注册成功',
    data: { userId: newUser.id, username, phone, role: newUser.role }
  }
})

Mock.mock('/api/auth/users/', 'get', (req) => {
  const userId = parseInt(req.url.match(/users\/(\d+)/)[1])
  const user = users.find(u => u.id === userId)
  if (user) {
    const account = accounts.find(a => a.userId === userId)
    return {
      code: 200,
      message: 'success',
      data: {
        userId: user.id,
        username: user.username,
        phone: user.phone,
        role: user.role,
        creditScore: user.creditScore,
        balance: account?.balance || 0,
        frozenBalance: account?.frozenBalance || 0
      }
    }
  }
  return { code: 400, message: '用户不存在' }
})

Mock.mock('/api/task/list', 'get', () => {
  const pendingTasks = tasks.filter(t => t.status === 'pending')
  return {
    code: 200,
    message: 'success',
    data: {
      list: pendingTasks.map(t => ({
        ...t,
        statusText: getStatusText(t.status),
        categoryText: getCategoryText(t.category)
      })),
      total: pendingTasks.length
    }
  }
})

Mock.mock('/api/task/my', 'get', (req) => {
  const userId = parseInt(req.url.match(/userId=(\d+)/)[1])
  const type = req.url.match(/type=(\w+)/)[1]
  let myTasks = []
  if (type === 'published') {
    myTasks = tasks.filter(t => t.requesterId === userId)
  } else {
    myTasks = tasks.filter(t => t.helperId === userId)
  }
  return {
    code: 200,
    message: 'success',
    data: {
      list: myTasks.map(t => ({
        ...t,
        statusText: getStatusText(t.status),
        categoryText: getCategoryText(t.category)
      })),
      total: myTasks.length
    }
  }
})

Mock.mock('/api/task/detail', 'get', (req) => {
  const taskId = parseInt(req.url.match(/taskId=(\d+)/)[1])
  const task = tasks.find(t => t.id === taskId)
  if (task) {
    const requester = users.find(u => u.id === task.requesterId)
    const helper = task.helperId ? users.find(u => u.id === task.helperId) : null
    return {
      code: 200,
      message: 'success',
      data: {
        ...task,
        statusText: getStatusText(task.status),
        categoryText: getCategoryText(task.category),
        requester: requester ? { username: requester.username, phone: requester.phone } : null,
        helper: helper ? { username: helper.username, phone: helper.phone } : null
      }
    }
  }
  return { code: 400, message: '任务不存在' }
})

Mock.mock('/api/task/publish', 'post', (req) => {
  const { title, description, category, reward, deadline, location, contactInfo, requesterId } = JSON.parse(req.body)
  const newTask = {
    id: tasks.length + 1,
    title,
    description,
    category,
    reward: parseFloat(reward),
    serviceFee: parseFloat(reward) * 0.05,
    status: 'pending',
    deadline,
    location,
    contactInfo,
    viewCount: 0,
    requesterId: parseInt(requesterId),
    helperId: null,
    createTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
  }
  tasks.push(newTask)
  return { code: 200, message: '发布成功', data: { taskId: newTask.id } }
})

Mock.mock('/api/task/accept', 'post', (req) => {
  const { taskId, userId } = JSON.parse(req.body)
  const task = tasks.find(t => t.id === taskId)
  if (!task) return { code: 400, message: '任务不存在' }
  if (task.status !== 'pending') return { code: 409, message: '任务状态不允许接单' }
  if (task.helperId) return { code: 409, message: '任务已被接单' }
  task.helperId = parseInt(userId)
  task.status = 'ongoing'
  return { code: 200, message: '接单成功', data: { taskId } }
})

Mock.mock('/api/task/submit', 'post', (req) => {
  const { taskId, evidence } = JSON.parse(req.body)
  const task = tasks.find(t => t.id === taskId)
  if (!task) return { code: 400, message: '任务不存在' }
  if (task.status !== 'ongoing') return { code: 409, message: '任务状态不允许提交' }
  task.status = 'pending_review'
  return { code: 200, message: '提交成功', data: { taskId } }
})

Mock.mock('/api/task/review', 'post', (req) => {
  const { taskId, approve } = JSON.parse(req.body)
  const task = tasks.find(t => t.id === taskId)
  if (!task) return { code: 400, message: '任务不存在' }
  if (task.status !== 'pending_review') return { code: 409, message: '任务状态不允许验收' }
  task.status = approve ? 'completed' : 'disputed'
  return { code: 200, message: approve ? '验收通过' : '已发起争议', data: { taskId } }
})

Mock.mock('/api/task/cancel', 'post', (req) => {
  const { taskId } = JSON.parse(req.body)
  const task = tasks.find(t => t.id === taskId)
  if (!task) return { code: 400, message: '任务不存在' }
  if (task.status !== 'pending') return { code: 409, message: '任务状态不允许取消' }
  task.status = 'cancelled'
  return { code: 200, message: '取消成功', data: { taskId } }
})

Mock.mock('/api/task/rate', 'post', (req) => {
  const { taskId, rating, comment, fromUserId, toUserId } = JSON.parse(req.body)
  const newEval = {
    id: evaluations.length + 1,
    taskId: parseInt(taskId),
    fromUserId: parseInt(fromUserId),
    toUserId: parseInt(toUserId),
    rating: parseInt(rating),
    comment,
    tags: '',
    createTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
  }
  evaluations.push(newEval)
  return { code: 200, message: '评价成功', data: { evalId: newEval.id } }
})

Mock.mock('/api/account/info', 'get', (req) => {
  const userId = parseInt(req.url.match(/userId=(\d+)/)[1])
  const account = accounts.find(a => a.userId === userId)
  if (account) {
    return {
      code: 200,
      message: 'success',
      data: account
    }
  }
  return { code: 400, message: '账户不存在' }
})

Mock.mock('/api/fund/flow', 'get', (req) => {
  const userId = parseInt(req.url.match(/userId=(\d+)/)[1])
  const userFlows = fundFlows.filter(f => f.userId === userId)
  return {
    code: 200,
    message: 'success',
    data: {
      list: userFlows.map(f => ({
        ...f,
        typeText: getFlowTypeText(f.type)
      })),
      total: userFlows.length
    }
  }
})

Mock.mock('/api/notification/list', 'get', (req) => {
  const userId = parseInt(req.url.match(/userId=(\d+)/)[1])
  const userNotifs = notifications.filter(n => n.userId === userId)
  return {
    code: 200,
    message: 'success',
    data: {
      list: userNotifs,
      total: userNotifs.length,
      unreadCount: userNotifs.filter(n => n.isRead === 0).length
    }
  }
})

Mock.mock('/api/notification/read', 'post', (req) => {
  const { userId } = JSON.parse(req.body)
  notifications.forEach(n => {
    if (n.userId === userId && n.isRead === 0) {
      n.isRead = 1
    }
  })
  return { code: 200, message: '已全部标记为已读' }
})

Mock.mock('/api/admin/tasks', 'get', () => {
  return {
    code: 200,
    message: 'success',
    data: {
      list: tasks.map(t => ({
        ...t,
        statusText: getStatusText(t.status),
        categoryText: getCategoryText(t.category)
      })),
      total: tasks.length
    }
  }
})

Mock.mock('/api/admin/disputes', 'get', () => {
  const disputedTasks = tasks.filter(t => t.status === 'disputed')
  return {
    code: 200,
    message: 'success',
    data: {
      list: disputedTasks.map(t => ({
        ...t,
        statusText: getStatusText(t.status),
        categoryText: getCategoryText(t.category)
      })),
      total: disputedTasks.length
    }
  }
})

Mock.mock('/api/admin/resolve', 'post', (req) => {
  const { taskId, result, remark } = JSON.parse(req.body)
  const task = tasks.find(t => t.id === taskId)
  if (task) {
    task.status = result === 'approve' ? 'completed' : 'cancelled'
    adminLogs.push({
      id: adminLogs.length + 1,
      adminId: 1,
      operationType: 'dispute_resolve',
      targetType: 'task',
      targetId: taskId,
      detail: `处理争议任务，结果: ${result === 'approve' ? '支持接单方' : '支持需求方'}，备注: ${remark}`,
      ipAddress: '192.168.1.100',
      createTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
    })
    return { code: 200, message: '处理成功' }
  }
  return { code: 400, message: '任务不存在' }
})

Mock.mock('/api/admin/statistics', 'get', () => {
  return {
    code: 200,
    message: 'success',
    data: {
      totalUsers: users.length,
      totalTasks: tasks.length,
      pendingTasks: tasks.filter(t => t.status === 'pending').length,
      completedTasks: tasks.filter(t => t.status === 'completed').length,
      disputedTasks: tasks.filter(t => t.status === 'disputed').length,
      totalAmount: tasks.reduce((sum, t) => sum + t.reward, 0)
    }
  }
})

function getStatusText(status) {
  const map = {
    pending: '待接单',
    ongoing: '进行中',
    pending_review: '待验收',
    completed: '已完成',
    disputed: '争议中',
    cancelled: '已取消'
  }
  return map[status] || status
}

function getCategoryText(category) {
  const map = {
    delivery: '跑腿代办',
    food: '美食外卖',
    print: '打印复印',
    other: '其他'
  }
  return map[category] || category
}

function getFlowTypeText(type) {
  const map = {
    freeze: '冻结',
    unfreeze: '解冻',
    income: '收入',
    expense: '支出',
    settle: '结算',
    recharge: '充值',
    withdraw: '提现'
  }
  return map[type] || type
}

export default Mock
