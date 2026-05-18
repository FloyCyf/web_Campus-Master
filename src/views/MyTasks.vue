<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { taskApi } from '@/api'
import AppLayout from '@/components/AppLayout.vue'
import StatusTag from '@/components/StatusTag.vue'
import BaseButton from '@/components/BaseButton.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const activeTab = ref(userStore.role === 'helper' ? 'accepted' : 'published')
const filterStatus = ref('all')

const statusOptions = [
  { value: 'all', label: '全部' },
  { value: 'pending', label: '待接单' },
  { value: 'ongoing', label: '进行中' },
  { value: 'pending_review', label: '待验收' },
  { value: 'completed', label: '已完成' },
  { value: 'disputed', label: '争议中' }
]

const publishedTasks = ref([])
const acceptedTasks = ref([])

const roleMeta = computed(() => {
  if (userStore.role === 'helper') {
    return {
      title: '我的接单',
      subtitle: '跟进已接任务的交付、验收和评价',
      empty: '暂无接单任务',
      emptyAction: '去任务广场看看',
      emptyPath: '/home'
    }
  }
  return {
    title: '我的任务',
    subtitle: '管理您发布的需求、验收进度和评价反馈',
    empty: '暂无发布任务',
    emptyAction: '发布任务',
    emptyPath: '/publish'
  }
})

const tabs = computed(() => {
  if (userStore.role === 'helper') return [{ key: 'accepted', label: '我接收的', count: acceptedTasks.value.length }]
  if (userStore.role === 'requester') return [{ key: 'published', label: '我发起的', count: publishedTasks.value.length }]
  return [
    { key: 'published', label: '我发起的', count: publishedTasks.value.length },
    { key: 'accepted', label: '我接收的', count: acceptedTasks.value.length }
  ]
})

const currentTasks = computed(() => {
  const tasks = activeTab.value === 'published' ? publishedTasks.value : acceptedTasks.value
  if (filterStatus.value === 'all') return tasks
  return tasks.filter(task => task.status === filterStatus.value)
})

const canShowEmptyAction = computed(() => !(userStore.role === 'helper' && activeTab.value === 'published'))

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const getActionButton = (task) => {
  const status = task.status
  if (activeTab.value === 'published') {
    if (status === 'pending_review') return { text: '去验收', variant: 'primary', handler: () => router.push(`/review/${task.id}`) }
    if (status === 'completed') return { text: '评价对方', variant: 'outline', handler: () => router.push(`/rate/${task.id}`) }
    if (status === 'pending') return { text: '取消', variant: 'danger', handler: () => handleCancel(task.id) }
  } else {
    if (status === 'ongoing') return { text: '提交完成', variant: 'primary', handler: () => router.push(`/submit/${task.id}`) }
    if (status === 'pending_review') return { text: '等待验收', variant: 'secondary', disabled: true }
    if (status === 'completed') return { text: '评价对方', variant: 'outline', handler: () => router.push(`/rate/${task.id}`) }
  }
  return null
}

const handleCancel = async (taskId) => {
  if (!confirm('确认取消这个任务吗？冻结金额会退回账户余额。')) return
  try {
    await taskApi.cancel({ taskId })
    loadTasks()
  } catch (error) {
    alert(error.response?.data?.message || '取消失败，请稍后重试')
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

watch(tabs, (value) => {
  if (!value.some(tab => tab.key === activeTab.value)) {
    activeTab.value = value[0]?.key || 'published'
  }
})

onMounted(loadTasks)
</script>

<template>
  <AppLayout>
    <div class="space-y-6">
      <section class="page-hero">
        <div>
          <p class="page-kicker">{{ userStore.role === 'helper' ? 'Helper workspace' : 'Requester workspace' }}</p>
          <h1 class="text-3xl font-semibold text-surface-950">{{ roleMeta.title }}</h1>
          <p class="text-surface-500 mt-2">{{ roleMeta.subtitle }}</p>
        </div>
      </section>

      <section class="surface-panel overflow-hidden">
        <div class="border-b border-surface-100 bg-white/70">
          <div class="flex">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              :class="[
                'px-6 py-4 text-sm font-semibold border-b-2 transition-colors',
                activeTab === tab.key
                  ? 'border-[var(--role-accent)] text-[var(--role-accent)]'
                  : 'border-transparent text-surface-500 hover:text-surface-800'
              ]"
              @click="activeTab = tab.key"
            >
              {{ tab.label }}
              <span class="ml-2 text-xs px-2 py-0.5 rounded-full bg-surface-100 text-surface-500">
                {{ tab.count }}
              </span>
            </button>
          </div>
        </div>

        <div class="p-4 border-b border-surface-100 flex flex-wrap gap-2 bg-white/50">
          <button
            v-for="option in statusOptions"
            :key="option.value"
            :class="[
              'px-3 py-1.5 rounded-lg text-sm font-medium transition-colors',
              filterStatus === option.value
                ? 'bg-[var(--role-accent-soft)] text-[var(--role-accent-strong)]'
                : 'bg-white text-surface-600 hover:bg-surface-50 border border-surface-100'
            ]"
            @click="filterStatus = option.value"
          >
            {{ option.label }}
          </button>
        </div>

        <div v-if="loading" class="p-6 space-y-4">
          <div v-for="i in 3" :key="i" class="h-24 bg-surface-50 rounded-xl animate-pulse"></div>
        </div>

        <div v-else-if="currentTasks.length > 0" class="divide-y divide-surface-100">
          <article
            v-for="task in currentTasks"
            :key="task.id"
            class="p-5 hover:bg-white/80 transition-colors cursor-pointer"
            @click="router.push(`/task/${task.id}`)"
          >
            <div class="flex items-start justify-between gap-4">
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-2">
                  <h3 class="font-semibold text-surface-950 truncate">{{ task.title }}</h3>
                  <StatusTag :status="task.status" size="sm" />
                </div>
                <p class="text-sm text-surface-500 line-clamp-2 mb-3">{{ task.description }}</p>
                <div class="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-surface-400">
                  <span v-if="activeTab === 'published'">接单状态：{{ task.helperId ? '已接单' : '等待接单' }}</span>
                  <span v-else>当前身份：接单方</span>
                  <span>截止：{{ formatDate(task.deadline) }}</span>
                </div>
              </div>
              <div class="flex flex-col items-end gap-2">
                <span class="text-xl font-semibold text-[var(--role-accent)]">¥{{ Number(task.reward || 0).toFixed(2) }}</span>
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
          </article>
        </div>

        <div v-else class="px-6 py-20 text-center">
          <div class="w-16 h-16 rounded-2xl bg-[var(--role-accent-soft)] flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-[var(--role-accent)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5h6m-6 4h6m-7 4h8m-9 8h10a2 2 0 002-2V7.8a2 2 0 00-.6-1.4l-3.8-3.8A2 2 0 0013.2 2H7a2 2 0 00-2 2v15a2 2 0 002 2z" />
            </svg>
          </div>
          <p class="text-surface-600 font-medium">{{ activeTab === 'published' ? roleMeta.empty : '暂无接单任务' }}</p>
          <router-link v-if="canShowEmptyAction" :to="activeTab === 'published' ? roleMeta.emptyPath : '/home'">
            <BaseButton variant="primary" class="mt-4">
              {{ activeTab === 'published' ? roleMeta.emptyAction : '去任务广场看看' }}
            </BaseButton>
          </router-link>
        </div>
      </section>
    </div>
  </AppLayout>
</template>
