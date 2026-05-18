<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { taskApi } from '@/api'
import AppLayout from '@/components/AppLayout.vue'
import StatusTag from '@/components/StatusTag.vue'
import BaseButton from '@/components/BaseButton.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const activeTab = ref('published')
const filterStatus = ref('all')

const statusOptions = [
  { value: 'all', label: '全部' },
  { value: 'pending', label: '待接单' },
  { value: 'ongoing', label: '进行中' },
  { value: 'pending_review', label: '待验收' },
  { value: 'completed', label: '已完成' },
  { value: 'disputed', label: '争议中' }
]

const statusMap = {
  pending: 'pending',
  ongoing: 'ongoing',
  pending_review: 'pending_review',
  completed: 'completed',
  disputed: 'disputed',
  cancelled: 'cancelled'
}

const publishedTasks = ref([])
const acceptedTasks = ref([])

const currentTasks = computed(() => {
  const tasks = activeTab.value === 'published' ? publishedTasks.value : acceptedTasks.value
  if (filterStatus.value === 'all') return tasks
  return tasks.filter(task => task.status === filterStatus.value)
})

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const getActionButton = (task) => {
  const status = task.status
  if (activeTab.value === 'published') {
    if (status === 'ongoing' || status === 'pending_review') return { text: '验收', variant: 'primary', handler: () => router.push(`/review/${task.id}`) }
    if (status === 'completed') return { text: '评价', variant: 'secondary', handler: () => router.push(`/rate/${task.id}`) }
    if (status === 'pending') return { text: '取消', variant: 'danger', handler: () => handleCancel(task.id) }
  } else {
    if (status === 'ongoing') return { text: '提交完成', variant: 'primary', handler: () => router.push(`/submit/${task.id}`) }
    if (status === 'pending_review') return { text: '待验收', variant: 'secondary', disabled: true }
    if (status === 'completed') return { text: '评价', variant: 'secondary', handler: () => router.push(`/rate/${task.id}`) }
  }
  return null
}

const handleCancel = async (taskId) => {
  if (!confirm('确定要取消该任务吗？')) return
  try {
    await taskApi.cancel({ taskId })
    loadTasks()
  } catch (error) {
    alert('取消失败，请重试')
  }
}

const loadTasks = async () => {
  loading.value = true
  try {
    const [publishedData, acceptedData] = await Promise.all([
      taskApi.getMyTasks(userStore.userId, 'published'),
      taskApi.getMyTasks(userStore.userId, 'accepted')
    ])
    publishedTasks.value = publishedData.list || []
    acceptedTasks.value = acceptedData.list || []
  } catch (error) {
    console.error('加载任务失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTasks()
})
</script>

<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <h1 class="text-2xl font-semibold text-surface-900">我的任务</h1>
        <p class="text-surface-500 mt-1">管理您发布和承接的任务</p>
      </div>

      <div class="bg-white rounded-xl border border-surface-200">
        <div class="border-b border-surface-200">
          <div class="flex">
            <button
              v-for="tab in [{ key: 'published', label: '我发起的' }, { key: 'accepted', label: '我接收的' }]"
              :key="tab.key"
              :class="[
                'px-6 py-4 text-sm font-medium border-b-2 transition-colors',
                activeTab === tab.key
                  ? 'border-accent-mauve-600 text-accent-mauve-600'
                  : 'border-transparent text-surface-500 hover:text-surface-700'
              ]"
              @click="activeTab = tab.key"
            >
              {{ tab.label }}
              <span class="ml-2 text-xs px-2 py-0.5 rounded-full bg-surface-100">
                {{ tab.key === 'published' ? publishedTasks.length : acceptedTasks.length }}
              </span>
            </button>
          </div>
        </div>

        <div class="p-4 border-b border-surface-100 flex flex-wrap gap-2">
          <button
            v-for="option in statusOptions"
            :key="option.value"
            :class="[
              'px-3 py-1.5 rounded-lg text-sm font-medium transition-colors',
              filterStatus === option.value
                ? 'bg-accent-mauve-100 text-accent-mauve-700'
                : 'bg-surface-50 text-surface-600 hover:bg-surface-100'
            ]"
            @click="filterStatus = option.value"
          >
            {{ option.label }}
          </button>
        </div>

        <div v-if="loading" class="p-8 space-y-4">
          <div v-for="i in 3" :key="i" class="h-20 bg-surface-50 rounded-xl animate-pulse"></div>
        </div>

        <div v-else-if="currentTasks.length > 0" class="divide-y divide-surface-100">
          <div
            v-for="task in currentTasks"
            :key="task.id"
            class="p-5 hover:bg-surface-50 transition-colors cursor-pointer"
            @click="router.push(`/task/${task.id}`)"
          >
            <div class="flex items-start justify-between">
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <h3 class="font-medium text-surface-900 truncate">{{ task.title }}</h3>
                  <StatusTag :status="task.status" size="sm" />
                </div>
                <p class="text-sm text-surface-500 truncate mb-2">{{ task.description }}</p>
                <div class="flex items-center gap-4 text-xs text-surface-400">
                  <span v-if="activeTab === 'published'">接单方：{{ task.helperId ? '已接单' : '暂无' }}</span>
                  <span v-else>发布者：已接单</span>
                  <span>截止：{{ formatDate(task.deadline) }}</span>
                </div>
              </div>
              <div class="flex flex-col items-end gap-2 ml-4">
                <span class="text-lg font-semibold text-accent-mauve-600">¥{{ task.reward }}</span>
                <BaseButton
                  v-if="getActionButton(task)"
                  :variant="getActionButton(task).variant"
                  size="sm"
                  :disabled="getActionButton(task).disabled"
                  @click.stop="getActionButton(task).handler()"
                >
                  {{ getActionButton(task).text }}
                </BaseButton>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="p-16 text-center">
          <div class="w-16 h-16 rounded-full bg-surface-100 flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-surface-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
          </div>
          <p class="text-surface-500">{{ activeTab === 'published' ? '暂无发布任务' : '暂无接单任务' }}</p>
          <router-link to="/home">
            <BaseButton variant="primary" class="mt-4">
              {{ activeTab === 'published' ? '发布任务' : '浏览任务' }}
            </BaseButton>
          </router-link>
        </div>
      </div>
    </div>
  </AppLayout>
</template>
