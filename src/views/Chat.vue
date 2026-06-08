<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { messageApi, taskApi } from '@/api'
import { onMessageReceived, offMessageReceived } from '@/utils/websocket'
import AppLayout from '@/components/AppLayout.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const taskId = ref(route.params.taskId)
const messages = ref([])
const newMessage = ref('')
const loading = ref(true)
const sending = ref(false)
const task = ref(null)
const chatContainer = ref(null)
const textareaRef = ref(null)

const otherUser = ref(null)

// 判断是否是自己发送的消息（确保类型一致）
const isMyMessage = (msg) => {
  return Number(msg.senderId) === Number(userStore.userId)
}

const formatTime = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

const loadTask = async () => {
  try {
    task.value = await taskApi.getDetail(taskId.value)
    const isRequester = task.value.requesterId === userStore.userId
    otherUser.value = isRequester
      ? { username: task.value.helper?.username || '接单方', id: task.value.helperId }
      : { username: task.value.requester?.username || '需求方', id: task.value.requesterId }
  } catch (error) {
    console.error('加载任务失败:', error)
  }
}

const loadMessages = async () => {
  try {
    const data = await messageApi.getMessages(taskId.value)
    messages.value = data || []
    scrollToBottom()
  } catch (error) {
    console.error('加载消息失败:', error)
  }
}

const handleSend = async () => {
  const content = newMessage.value.trim()
  if (!content || sending.value) return

  sending.value = true
  try {
    await messageApi.sendMessage(taskId.value, content)
    newMessage.value = ''
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
    }
    await loadMessages()
  } catch (error) {
    alert(error.response?.data?.message || '发送失败')
  } finally {
    sending.value = false
  }
}

const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

const autoResize = () => {
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
    textareaRef.value.style.height = Math.min(textareaRef.value.scrollHeight, 120) + 'px'
  }
}

const handleNewMessage = (msg) => {
  if (msg.taskId === Number(taskId.value) || msg.taskId === taskId.value) {
    const exists = messages.value.some(m => m.id === msg.id)
    if (!exists) {
      messages.value.push(msg)
      scrollToBottom()
    }
    messageApi.markRead(taskId.value)
  }
}

onMounted(async () => {
  loading.value = true
  await loadTask()
  await loadMessages()
  await messageApi.markRead(taskId.value)
  onMessageReceived(handleNewMessage)
  loading.value = false
})

onUnmounted(() => {
  offMessageReceived(handleNewMessage)
})
</script>

<template>
  <AppLayout>
    <div class="max-w-3xl mx-auto h-[calc(100vh-8rem)] flex flex-col w-full">
      <!-- Header -->
      <div class="flex items-center gap-3 mb-4">
        <button class="flex items-center text-surface-500 hover:text-surface-700" @click="router.back()">
          <svg class="w-5 h-5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          返回
        </button>
        <div class="flex-1">
          <h1 class="text-lg font-semibold text-surface-900">
            {{ task?.title ? `聊天 - ${task.title}` : '任务聊天' }}
          </h1>
          <p class="text-xs text-surface-500">
            与 {{ otherUser?.username }} 的对话
          </p>
        </div>
      </div>

      <!-- Chat Area -->
      <div
        ref="chatContainer"
        class="flex-1 bg-white rounded-xl border border-surface-200 overflow-y-auto p-6 space-y-4 w-full"
      >
        <div v-if="loading" class="flex items-center justify-center py-10">
          <div class="animate-spin w-6 h-6 border-2 border-accent-mauve-600 border-t-transparent rounded-full"></div>
        </div>

        <div v-else-if="messages.length === 0" class="text-center py-10 text-surface-400 text-sm">
          暂无消息，开始对话吧
        </div>

        <div
          v-for="msg in messages"
          :key="msg.id"
          class="flex w-full gap-3 mb-3"
          :style="isMyMessage(msg) ? 'justify-content: flex-end;' : 'justify-content: flex-start;'"
        >
          <div v-if="!isMyMessage(msg)" 
               class="w-9 h-9 rounded-full flex items-center justify-center text-xs font-medium shrink-0 bg-surface-200 text-surface-600">
            {{ otherUser?.username?.charAt(0) || '他' }}
          </div>
          
          <div
            :class="[
              'max-w-[65%] px-4 py-2.5 rounded-2xl text-sm',
              isMyMessage(msg)
                ? 'bg-accent-mauve-600 text-white rounded-tr-sm'
                : 'bg-surface-100 text-surface-800 rounded-tl-sm'
            ]"
          >
            <p class="whitespace-pre-wrap break-words leading-relaxed">{{ msg.content }}</p>
            <p
              :class="[
                'text-[10px] mt-1.5',
                isMyMessage(msg) ? 'text-white/70 text-right' : 'text-surface-400 text-left'
              ]"
            >
              {{ formatTime(msg.createTime) }}
            </p>
          </div>
          
          <div v-if="isMyMessage(msg)" 
               class="w-9 h-9 rounded-full flex items-center justify-center text-xs font-medium shrink-0 bg-accent-mauve-100 text-accent-mauve-600">
            {{ userStore.username?.charAt(0) || '我' }}
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="mt-4 bg-white rounded-xl border border-surface-200 p-3 flex gap-3">
        <textarea
          ref="textareaRef"
          v-model="newMessage"
          rows="1"
          placeholder="输入消息，按 Enter 发送..."
          class="flex-1 resize-none bg-transparent text-sm text-surface-900 placeholder-surface-400 focus:outline-none max-h-24"
          @input="autoResize"
          @keydown="handleKeydown"
        ></textarea>
        <button
          :disabled="!newMessage.trim() || sending"
          class="px-4 py-2 bg-accent-mauve-600 text-white text-sm font-medium rounded-lg hover:bg-accent-mauve-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          @click="handleSend"
        >
          {{ sending ? '发送中...' : '发送' }}
        </button>
      </div>
    </div>
  </AppLayout>
</template>
