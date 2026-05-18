import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/publish',
    name: 'Publish',
    component: () => import('@/views/Publish.vue'),
    meta: { requiresAuth: true, roles: ['requester', 'admin'] }
  },
  {
    path: '/my-tasks',
    name: 'MyTasks',
    component: () => import('@/views/MyTasks.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/task/:id',
    name: 'TaskDetail',
    component: () => import('@/views/TaskDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/submit/:id',
    name: 'SubmitTask',
    component: () => import('@/views/SubmitTask.vue'),
    meta: { requiresAuth: true, roles: ['helper'] }
  },
  {
    path: '/review/:id',
    name: 'ReviewTask',
    component: () => import('@/views/ReviewTask.vue'),
    meta: { requiresAuth: true, roles: ['requester'] }
  },
  {
    path: '/rate/:id',
    name: 'RateTask',
    component: () => import('@/views/RateTask.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/Admin.vue'),
    meta: { requiresAuth: true, roles: ['admin'] }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userInfo = localStorage.getItem('userInfo')
  const user = userInfo ? JSON.parse(userInfo) : null

  if (to.meta.requiresAuth && !user) {
    next('/login')
    return
  }

  if (to.meta.roles && user) {
    if (!to.meta.roles.includes(user.role)) {
      next('/home')
      return
    }
  }

  next()
})

export default router
