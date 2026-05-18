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

const navLinks = computed(() => {
  const links = [
    { path: '/home', label: '任务大厅' },
    { path: '/my-tasks', label: '我的任务' },
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
    const data = await notificationApi.getList(userStore.userId)
    notifications.value = data.list || []
    userStore.setUnreadCount(data.unreadCount || 0)
  } catch (error) {
    console.error('加载通知失败:', error)
  }
}

const markAllRead = async () => {
  if (!userStore.userId) return
  try {
    await notificationApi.markRead(userStore.userId)
    userStore.setUnreadCount(0)
    notifications.value.forEach(n => n.isRead = 1)
  } catch (error) {
    console.error('标记已读失败:', error)
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

onMounted(() => {
  loadNotifications()
})
</script>

<template>
  <nav class="sticky top-0 z-50 bg-white/80 backdrop-blur-md border-b border-surface-200">
    <div class="max-w-6xl mx-auto px-6">
      <div class="flex items-center justify-between h-16">
        <div class="flex items-center gap-8">
          <router-link to="/home" class="flex items-center gap-2">
            <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-accent-mauve-500 to-accent-mauve-600 flex items-center justify-center">
              <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <span class="text-lg font-semibold text-surface-900">万事达</span>
          </router-link>

          <div class="hidden md:flex items-center gap-1">
            <router-link
              v-for="link in navLinks"
              :key="link.path"
              :to="link.path"
              :class="[
                'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
                isActive(link.path)
                  ? 'bg-surface-100 text-surface-900'
                  : 'text-surface-500 hover:text-surface-700 hover:bg-surface-50'
              ]"
            >
              {{ link.label }}
            </router-link>
          </div>
        </div>

        <div class="flex items-center gap-3">
          <template v-if="userStore.isAuthenticated">
            <div class="relative">
              <button
                class="relative p-2 rounded-lg hover:bg-surface-100 transition-colors"
                @click="toggleNotification"
              >
                <svg class="w-5 h-5 text-surface-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                </svg>
                <span v-if="userStore.unreadCount > 0" class="absolute -top-1 -right-1 w-5 h-5 bg-rose-500 text-white text-xs font-medium rounded-full flex items-center justify-center">
                  {{ userStore.unreadCount > 9 ? '9+' : userStore.unreadCount }}
                </span>
              </button>

              <Transition
                enter-active-class="transition ease-out duration-100"
                enter-from-class="transform opacity-0 scale-95"
                enter-to-class="transform opacity-100 scale-100"
                leave-active-class="transition ease-in duration-75"
                leave-from-class="transform opacity-100 scale-100"
                leave-to-class="transform opacity-0 scale-95"
              >
                <div
                  v-if="showNotification"
                  class="absolute right-0 mt-2 w-80 bg-white rounded-xl shadow-large border border-surface-200 py-2 z-50"
                >
                  <div class="px-4 py-3 border-b border-surface-100 flex items-center justify-between">
                    <h3 class="font-medium text-surface-900">通知</h3>
                    <span class="text-xs text-surface-500">{{ notifications.length }} 条</span>
                  </div>
                  <div class="max-h-80 overflow-y-auto">
                    <div
                      v-for="notif in notifications"
                      :key="notif.id"
                      :class="[
                        'px-4 py-3 cursor-pointer hover:bg-surface-50 transition-colors',
                        notif.isRead === 0 ? 'bg-surface-50' : ''
                      ]"
                      @click="notif.taskId ? goToTaskDetail(notif.taskId) : showNotification = false"
                    >
                      <div class="flex items-start gap-3">
                        <div :class="[
                          'w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0',
                          notif.type === 'task_accepted' ? 'bg-emerald-100' :
                          notif.type === 'task_submitted' ? 'bg-blue-100' :
                          notif.type === 'dispute_created' ? 'bg-amber-100' : 'bg-surface-100'
                        ]">
                          <svg class="w-4 h-4" :class="[
                            notif.type === 'task_accepted' ? 'text-emerald-600' :
                            notif.type === 'task_submitted' ? 'text-blue-600' :
                            notif.type === 'dispute_created' ? 'text-amber-600' : 'text-surface-500'
                          ]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                          </svg>
                        </div>
                        <div class="flex-1 min-w-0">
                          <p class="font-medium text-surface-900 text-sm">{{ notif.title }}</p>
                          <p class="text-xs text-surface-500 mt-0.5 truncate">{{ notif.content }}</p>
                          <p class="text-xs text-surface-400 mt-1">{{ notif.createTime }}</p>
                        </div>
                      </div>
                    </div>
                    <div v-if="notifications.length === 0" class="px-4 py-8 text-center text-surface-500">
                      暂无通知
                    </div>
                  </div>
                </div>
              </Transition>
            </div>

            <div class="relative">
              <button
                class="flex items-center gap-2 px-3 py-1.5 rounded-full bg-surface-100 hover:bg-surface-200 transition-colors"
                @click="showUserMenu = !showUserMenu"
              >
                <div class="w-7 h-7 rounded-full bg-gradient-to-br from-accent-mauve-400 to-accent-mauve-500 flex items-center justify-center">
                  <span class="text-white text-xs font-medium">
                    {{ userStore.username?.charAt(0) || 'U' }}
                  </span>
                </div>
                <span class="text-sm font-medium text-surface-700 hidden sm:block">
                  {{ userStore.username }}
                </span>
                <svg class="w-4 h-4 text-surface-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                </svg>
              </button>

              <Transition
                enter-active-class="transition ease-out duration-100"
                enter-from-class="transform opacity-0 scale-95"
                enter-to-class="transform opacity-100 scale-100"
                leave-active-class="transition ease-in duration-75"
                leave-from-class="transform opacity-100 scale-100"
                leave-to-class="transform opacity-0 scale-95"
              >
                <div
                  v-if="showUserMenu"
                  class="absolute right-0 mt-2 w-48 bg-white rounded-xl shadow-large border border-surface-200 py-1 animate-scale-in"
                >
                  <button
                    class="w-full px-4 py-2 text-left text-sm text-surface-700 hover:bg-surface-50 flex items-center gap-2"
                    @click="goToProfile"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    个人中心
                  </button>
                  <button
                    v-if="userStore.role === 'admin'"
                    class="w-full px-4 py-2 text-left text-sm text-surface-700 hover:bg-surface-50 flex items-center gap-2"
                    @click="goToAdmin"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    管理后台
                  </button>
                  <div class="border-t border-surface-100 my-1"></div>
                  <button
                    class="w-full px-4 py-2 text-left text-sm text-surface-700 hover:bg-surface-50 flex items-center gap-2"
                    @click="handleLogout"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                    退出登录
                  </button>
                </div>
              </Transition>
            </div>
          </template>

          <template v-else>
            <router-link
              to="/login"
              class="px-4 py-2 text-sm font-medium text-surface-600 hover:text-surface-900 transition-colors"
            >
              登录
            </router-link>
            <router-link
              to="/register"
              class="px-4 py-2 text-sm font-medium bg-accent-mauve-600 text-white rounded-lg hover:bg-accent-mauve-700 transition-colors"
            >
              注册
            </router-link>
          </template>
        </div>
      </div>
    </div>
  </nav>
</template>
