<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { notificationApi } from '@/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const showUserMenu = ref(false)
const showNotification = ref(false)
const notifications = ref([])

const roleMeta = computed(() => {
  const map = {
    requester: { label: '需求方', hint: '发布与验收' },
    helper: { label: '接单方', hint: '接单与交付' },
    admin: { label: '管理员', hint: '平台管理' }
  }
  return map[userStore.role] || { label: '访客', hint: '未登录' }
})

const navLinks = computed(() => {
  if (userStore.role === 'admin') {
    return [{ path: '/admin', label: '管理工作台' }]
  }

  const links = [
    { path: '/home', label: userStore.role === 'helper' ? '任务广场' : '任务大厅' },
    { path: '/my-tasks', label: userStore.role === 'helper' ? '我的接单' : '我的任务' }
  ]
  if (userStore.role === 'requester' || userStore.role === 'admin') {
    links.push({ path: '/publish', label: '发布任务' })
  }
  return links
})

const isActive = (path) => {
  if (path === '/home' && (route.path === '/' || route.path === '/home')) return true
  return route.path.startsWith(path)
}

const notificationTone = (type) => {
  if (type === 'task_accepted') return 'bg-emerald-100 text-emerald-600'
  if (type === 'task_submitted') return 'bg-blue-100 text-blue-600'
  if (type === 'review_received') return 'bg-rose-100 text-rose-600'
  if (type === 'dispute_created') return 'bg-amber-100 text-amber-600'
  return 'bg-surface-100 text-surface-500'
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const goToProfile = () => {
  showUserMenu.value = false
  router.push('/profile')
}

const goToAdmin = () => {
  showUserMenu.value = false
  router.push('/admin')
}

const loadNotifications = async () => {
  if (!userStore.userId) return
  try {
    const data = await notificationApi.getList()
    notifications.value = data.list || []
    userStore.setUnreadCount(data.unreadCount || 0)
  } catch (error) {
    console.error('加载通知失败:', error)
  }
}

const markAllRead = async () => {
  if (!userStore.userId) return
  try {
    await notificationApi.markRead()
    userStore.setUnreadCount(0)
    notifications.value.forEach(n => n.isRead = 1)
  } catch (error) {
    console.error('标记通知失败:', error)
  }
}

const toggleNotification = () => {
  showNotification.value = !showNotification.value
  if (showNotification.value && userStore.unreadCount > 0) {
    markAllRead()
  }
}

const goToTaskDetail = (taskId) => {
  showNotification.value = false
  router.push(`/task/${taskId}`)
}

onMounted(loadNotifications)
</script>

<template>
  <nav class="sticky top-0 z-50 border-b border-white/60 bg-white/78 backdrop-blur-xl shadow-[0_10px_30px_rgba(15,23,42,0.04)]">
    <div class="max-w-6xl mx-auto px-5 sm:px-6">
      <div class="flex items-center justify-between h-18 py-3">
        <div class="flex items-center gap-8">
          <router-link to="/home" class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-[var(--role-accent)] flex items-center justify-center shadow-soft">
              <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <div>
              <span class="block text-xl font-semibold text-surface-950 leading-5">万事达</span>
              <span class="hidden sm:block text-xs text-surface-400 mt-1">{{ roleMeta.hint }}</span>
            </div>
          </router-link>

          <div class="hidden md:flex items-center gap-1 rounded-xl bg-white/70 border border-white px-1.5 py-1 shadow-soft">
            <router-link
              v-for="link in navLinks"
              :key="link.path"
              :to="link.path"
              :class="[
                'px-4 py-2 rounded-lg text-sm font-medium transition-all',
                isActive(link.path)
                  ? 'bg-[var(--role-accent)] text-white shadow-soft'
                  : 'text-surface-500 hover:text-surface-800 hover:bg-surface-50'
              ]"
            >
              {{ link.label }}
            </router-link>
          </div>
        </div>

        <div class="flex items-center gap-3">
          <template v-if="userStore.isAuthenticated">
            <span class="hidden lg:inline-flex px-3 py-1 rounded-full text-xs font-medium bg-[var(--role-accent-soft)] text-[var(--role-accent-strong)]">
              {{ roleMeta.label }}
            </span>

            <div class="relative">
              <button class="relative p-2.5 rounded-xl hover:bg-white transition-colors" @click="toggleNotification">
                <svg class="w-5 h-5 text-surface-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.4-1.4A2 2 0 0118 14.2V11a6 6 0 00-4-5.7V5a2 2 0 10-4 0v.3A6 6 0 006 11v3.2c0 .5-.2 1-.6 1.4L4 17h11zm0 0v1a3 3 0 11-6 0v-1" />
                </svg>
                <span v-if="userStore.unreadCount > 0" class="absolute -top-1 -right-1 min-w-5 h-5 px-1 bg-rose-500 text-white text-xs font-semibold rounded-full flex items-center justify-center">
                  {{ userStore.unreadCount > 9 ? '9+' : userStore.unreadCount }}
                </span>
              </button>

              <Transition enter-active-class="transition ease-out duration-100" enter-from-class="opacity-0 scale-95" enter-to-class="opacity-100 scale-100" leave-active-class="transition ease-in duration-75" leave-from-class="opacity-100 scale-100" leave-to-class="opacity-0 scale-95">
                <div v-if="showNotification" class="absolute right-0 mt-3 w-96 max-w-[calc(100vw-2rem)] bg-white rounded-2xl shadow-large border border-surface-100 py-2 z-50">
                  <div class="px-4 py-3 border-b border-surface-100 flex items-center justify-between">
                    <h3 class="font-semibold text-surface-900">消息提醒</h3>
                    <span class="text-xs text-surface-500">{{ notifications.length }} 条</span>
                  </div>
                  <div class="max-h-80 overflow-y-auto">
                    <div
                      v-for="notif in notifications"
                      :key="notif.id"
                      :class="['px-4 py-3 cursor-pointer hover:bg-surface-50 transition-colors', notif.isRead === 0 ? 'bg-[var(--role-accent-soft)]/60' : '']"
                      @click="notif.taskId ? goToTaskDetail(notif.taskId) : showNotification = false"
                    >
                      <div class="flex items-start gap-3">
                        <div :class="['w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0', notificationTone(notif.type)]">
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                          </svg>
                        </div>
                        <div class="flex-1 min-w-0">
                          <p class="font-medium text-surface-900 text-sm">{{ notif.title }}</p>
                          <p class="text-xs text-surface-500 mt-0.5 line-clamp-2">{{ notif.content }}</p>
                          <p class="text-xs text-surface-400 mt-1">{{ notif.createTime }}</p>
                        </div>
                      </div>
                    </div>
                    <div v-if="notifications.length === 0" class="px-4 py-8 text-center text-surface-500">
                      暂无消息
                    </div>
                  </div>
                </div>
              </Transition>
            </div>

            <div class="relative">
              <button class="flex items-center gap-2 px-2.5 py-1.5 rounded-full bg-white border border-surface-100 hover:shadow-soft transition-all" @click="showUserMenu = !showUserMenu">
                <div class="w-8 h-8 rounded-full bg-[var(--role-accent)] flex items-center justify-center">
                  <span class="text-white text-xs font-semibold">{{ userStore.username?.charAt(0) || 'U' }}</span>
                </div>
                <span class="text-sm font-medium text-surface-700 hidden sm:block">{{ userStore.username }}</span>
                <svg class="w-4 h-4 text-surface-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                </svg>
              </button>

              <Transition enter-active-class="transition ease-out duration-100" enter-from-class="opacity-0 scale-95" enter-to-class="opacity-100 scale-100" leave-active-class="transition ease-in duration-75" leave-from-class="opacity-100 scale-100" leave-to-class="opacity-0 scale-95">
                <div v-if="showUserMenu" class="absolute right-0 mt-3 w-48 bg-white rounded-2xl shadow-large border border-surface-100 py-1">
                  <button class="w-full px-4 py-2.5 text-left text-sm text-surface-700 hover:bg-surface-50 flex items-center gap-2" @click="goToProfile">个人中心</button>
                  <button v-if="userStore.role === 'admin'" class="w-full px-4 py-2.5 text-left text-sm text-surface-700 hover:bg-surface-50 flex items-center gap-2" @click="goToAdmin">后台管理</button>
                  <div class="border-t border-surface-100 my-1"></div>
                  <button class="w-full px-4 py-2.5 text-left text-sm text-surface-700 hover:bg-surface-50 flex items-center gap-2" @click="handleLogout">退出登录</button>
                </div>
              </Transition>
            </div>
          </template>

          <template v-else>
            <router-link to="/login" class="px-4 py-2 text-sm font-medium text-surface-600 hover:text-surface-900 transition-colors">登录</router-link>
            <router-link to="/register" class="px-4 py-2 text-sm font-medium bg-[var(--role-accent)] text-white rounded-lg hover:brightness-95 transition-colors">注册</router-link>
          </template>
        </div>
      </div>
    </div>
  </nav>
</template>
