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
const searchQuery = ref('')
const selectedCategory = ref('all')
const tasks = ref([])

const categories = [
  { value: 'all', label: '全部' },
  { value: 'delivery', label: '跑腿代取' },
  { value: 'food', label: '餐饮外卖' },
  { value: 'print', label: '打印资料' },
  { value: 'other', label: '其他' }
]

const heroCopy = computed(() => {
  if (userStore.role === 'helper') {
    return {
      kicker: 'Task market',
      title: '任务广场',
      subtitle: '筛选合适的校园任务，接单后按流程提交完成'
    }
  }
  return {
    kicker: 'Campus tasks',
    title: '任务大厅',
    subtitle: '查看校园互助任务，需求方可以快速发布新的委托'
  }
})

const filteredTasks = computed(() => {
  return tasks.value.filter(task => {
    const title = task.title || ''
    const description = task.description || ''
    const matchSearch = title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      description.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchCategory = selectedCategory.value === 'all' || task.category === selectedCategory.value
    return matchSearch && matchCategory
  })
})

const openTasks = computed(() => tasks.value.filter(task => task.status === 'pending').length)

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

const handleViewTask = (taskId) => {
  router.push(`/task/${taskId}`)
}

const loadTasks = async () => {
  loading.value = true
  try {
    const data = await taskApi.getList()
    tasks.value = data.records || data.list || []
  } catch (error) {
    console.error('加载任务列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(loadTasks)
</script>

<template>
  <AppLayout>
    <div class="space-y-6">
      <section class="page-hero">
        <div>
          <p class="page-kicker">{{ heroCopy.kicker }}</p>
          <h1 class="text-3xl font-semibold text-surface-950">{{ heroCopy.title }}</h1>
          <p class="text-surface-500 mt-2">{{ heroCopy.subtitle }}</p>
        </div>
        <router-link v-if="userStore.role === 'requester' || userStore.role === 'admin'" to="/publish">
          <BaseButton variant="primary">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            发布任务
          </BaseButton>
        </router-link>
      </section>

      <section class="surface-panel p-4 sm:p-5">
        <div class="grid grid-cols-1 lg:grid-cols-[1fr_auto] gap-4">
          <div class="relative">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-surface-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input v-model="searchQuery" type="text" class="input pl-10" placeholder="搜索任务标题、描述..." />
          </div>
          <div class="flex gap-2 overflow-x-auto pb-1 lg:pb-0">
            <button
              v-for="cat in categories"
              :key="cat.value"
              :class="[
                'px-4 py-2 rounded-lg text-sm font-medium whitespace-nowrap transition-colors border',
                selectedCategory === cat.value
                  ? 'bg-[var(--role-accent)] text-white border-transparent'
                  : 'bg-white text-surface-600 hover:bg-surface-50 border-surface-100'
              ]"
              @click="selectedCategory = cat.value"
            >
              {{ cat.label }}
            </button>
          </div>
        </div>
      </section>

      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div class="surface-panel p-5">
          <p class="text-sm text-surface-500">任务总数</p>
          <p class="text-2xl font-semibold text-surface-950 mt-1">{{ tasks.length }}</p>
        </div>
        <div class="surface-panel p-5">
          <p class="text-sm text-surface-500">可接任务</p>
          <p class="text-2xl font-semibold text-[var(--role-accent)] mt-1">{{ openTasks }}</p>
        </div>
        <div class="surface-panel p-5">
          <p class="text-sm text-surface-500">当前筛选</p>
          <p class="text-2xl font-semibold text-surface-950 mt-1">{{ filteredTasks.length }}</p>
        </div>
      </div>

      <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-for="i in 4" :key="i" class="surface-panel p-5 animate-pulse">
          <div class="h-5 bg-surface-100 rounded w-3/4 mb-3"></div>
          <div class="h-4 bg-surface-100 rounded w-full mb-2"></div>
          <div class="h-4 bg-surface-100 rounded w-1/2"></div>
        </div>
      </div>

      <div v-else-if="filteredTasks.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <article
          v-for="task in filteredTasks"
          :key="task.id"
          class="surface-panel p-5 hover:shadow-medium hover:-translate-y-0.5 transition-all cursor-pointer group"
          @click="handleViewTask(task.id)"
        >
          <div class="flex items-start justify-between gap-3 mb-3">
            <h3 class="font-semibold text-surface-950 group-hover:text-[var(--role-accent)] transition-colors line-clamp-1">{{ task.title }}</h3>
            <StatusTag :status="task.status" size="sm" />
          </div>
          <p class="text-sm text-surface-500 line-clamp-2 mb-4">{{ task.description }}</p>
          <div class="flex items-center justify-between text-sm">
            <div class="flex items-center gap-4 text-surface-400">
              <span class="text-[var(--role-accent)] font-semibold">¥{{ Number(task.reward || 0).toFixed(2) }}</span>
              <span class="flex items-center gap-1">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {{ formatDate(task.deadline) }}
              </span>
            </div>
            <span class="text-surface-400">{{ task.viewCount || 0 }} 浏览</span>
          </div>
        </article>
      </div>

      <div v-else class="surface-panel text-center py-16">
        <div class="w-16 h-16 rounded-2xl bg-[var(--role-accent-soft)] flex items-center justify-center mx-auto mb-4">
          <svg class="w-8 h-8 text-[var(--role-accent)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.2 16.2a4 4 0 015.6 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <p class="text-surface-600 font-medium">暂无符合条件的任务</p>
        <p class="text-sm text-surface-400 mt-1">可以换个分类或关键词再试试</p>
      </div>
    </div>
  </AppLayout>
</template>
