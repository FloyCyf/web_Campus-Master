<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { messageApi, taskApi } from '@/api'
import AppLayout from '@/components/AppLayout.vue'

const router = useRouter()
const userStore = useUserStore()

const conversations = ref([])
const tasks = ref({})
const loading = ref(true)

const groupedConversations = computed(() => {
  const grouped = {}
  conversations.value.forEach(msg => {
    const taskId = msg.taskId
    if (!grouped[taskId]) {
      const task = tasks.value[taskId]
      const isRequester = task?.requesterId === userStore.userId
      const otherUser = isRequester
        ? { username: task?.helper?.username || '接单方', id: task?.helperId }
        : { username: task?.requester?.username || '需求方', id: task?.requesterId }
      
      grouped[taskId] = {
        taskId: msg.taskId,
        taskTitle: task?.title || `任务 #${msg.taskId}`,
        otherUser,
        lastMessage: msg,
        unreadCount: 0
      }
    }
    if (msg.receiverId === userStore.userId && !msg.isRead) {
      grouped[taskId].unreadCount++
    }
  })
  
  return Object.values(grouped)
    .map(g => ({
      ...g,
      lastMessage: conversations.value
        .filter(m => m.taskId === g.taskId)
        .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))[0]
    }))
    .sort((a, b) => new Date(b.lastMessage?.createTime) - new Date(a.lastMessage?.createTime))
})

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const oneDay = 24 * 60 * 60 * 1000
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 7 * oneDay) return `${Math.floor(diff / oneDay)}天前`
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

const formatPreview = (content) => {
  if (!content) return ''
  return content.length > 30 ? content.substring(0, 30) + '...' : content
}

const handleConversationClick = (taskId) => {
  router.push(`/chat/${taskId}`)
}

const loadConversations = async () => {
  loading.value = true
  try {
    const data = await messageApi.getConversations()
    conversations.value = data || []
    
    const taskIds = [...new Set(conversations.value.map(m => m.taskId))]
    const taskPromises = taskIds.map(id => taskApi.getDetail(id).catch(() => null))
    const taskResults = await Promise.all(taskPromises)
    
    taskResults.forEach(task => {
      if (task) {
        tasks.value[task.id] = task
      }
    })
  } catch (error) {
    console.error('加载会话列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(loadConversations)
</script>

<template>
  <AppLayout>
    <div class="max-w-2xl mx-auto">
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-2xl font-semibold text-surface-900">消息</h1>
        <span v-if="groupedConversations.some(c => c.unreadCount > 0)" class="px-3 py-1 bg-rose-500 text-white text-xs font-semibold rounded-full">
          {{ groupedConversations.reduce((sum, c) => sum + c.unreadCount, 0) }} 条未读
        </span>
      </div>

      <div v-if="loading" class="space-y-3">
        <div v-for="i in 5" :key="i" class="bg-white rounded-xl border border-surface-200 p-4 animate-pulse">
          <div class="flex items-start gap-3">
            <div class="w-12 h-12 rounded-full bg-surface-100"></div>
            <div class="flex-1">
              <div class="h-4 bg-surface-100 rounded w-1/3 mb-2"></div>
              <div class="h-3 bg-surface-100 rounded w-full mb-1"></div>
              <div class="h-3 bg-surface-100 rounded w-1/2"></div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="groupedConversations.length === 0" class="text-center py-16">
        <div class="w-16 h-16 rounded-2xl bg-surface-100 flex items-center justify-center mx-auto mb-4">
          <svg class="w-8 h-8 text-surface-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
        </div>
        <p class="text-surface-600 font-medium">暂无消息</p>
        <p class="text-sm text-surface-400 mt-1">在任务中进行交流后，对话会显示在这里</p>
      </div>

      <div v-else class="space-y-2">
        <div
          v-for="conv in groupedConversations"
          :key="conv.taskId"
          class="bg-white rounded-xl border border-surface-200 p-4 hover:shadow-soft hover:-translate-y-0.5 transition-all cursor-pointer"
          @click="handleConversationClick(conv.taskId)"
        >
          <div class="flex items-start gap-3">
            <div class="relative">
              <div class="w-12 h-12 rounded-full bg-accent-mauve-100 flex items-center justify-center">
                <svg class="w-6 h-6 text-accent-mauve-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <span
                v-if="conv.unreadCount > 0"
                class="absolute -top-1 -right-1 min-w-5 h-5 px-1 bg-rose-500 text-white text-xs font-semibold rounded-full flex items-center justify-center"
              >
                {{ conv.unreadCount > 9 ? '9+' : conv.unreadCount }}
              </span>
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between mb-1">
                <h3 class="font-medium text-surface-900 truncate">{{ conv.otherUser?.username || '未知用户' }}</h3>
                <span class="text-xs text-surface-400">{{ formatTime(conv.lastMessage?.createTime) }}</span>
              </div>
              <p class="text-xs text-surface-500 mb-1 truncate">{{ conv.taskTitle }}</p>
              <p class="text-sm text-surface-600 truncate">{{ formatPreview(conv.lastMessage?.content) }}</p>
            </div>

            <svg class="w-5 h-5 text-surface-300 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>
