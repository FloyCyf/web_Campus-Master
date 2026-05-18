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

const categories = [
  { value: 'all', label: '全部' },
  { value: 'delivery', label: '跑腿代办' },
  { value: 'food', label: '美食外卖' },
  { value: 'print', label: '打印复印' },
  { value: 'other', label: '其他' }
]

const tasks = ref([])

const filteredTasks = computed(() => {
  return tasks.value.filter(task => {
    const matchSearch = task.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                       (task.description && task.description.toLowerCase().includes(searchQuery.value.toLowerCase()))
    const matchCategory = selectedCategory.value === 'all' || task.category === selectedCategory.value
    return matchSearch && matchCategory
  })
})

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

onMounted(() => {
  loadTasks()
})
</script>

<template>
  <AppLayout>
    <div class="space-y-6">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 class="text-2xl font-semibold text-surface-900">任务大厅</h1>
          <p class="text-surface-500 mt-1">发现校园互助机会</p>
        </div>
        <router-link v-if="userStore.role === 'requester' || userStore.role === 'admin'" to="/publish">
          <BaseButton variant="primary">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            发布任务
          </BaseButton>
        </router-link>
      </div>

      <div class="flex flex-col sm:flex-row gap-4">
        <div class="flex-1 relative">
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-surface-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            class="input pl-10"
            placeholder="搜索任务..."
          />
        </div>
        <div class="flex gap-2 overflow-x-auto pb-2 sm:pb-0">
          <button
            v-for="cat in categories"
            :key="cat.value"
            :class="[
              'px-4 py-2 rounded-lg text-sm font-medium whitespace-nowrap transition-colors',
              selectedCategory === cat.value
                ? 'bg-accent-mauve-600 text-white'
                : 'bg-white text-surface-600 hover:bg-surface-100 border border-surface-200'
            ]"
            @click="selectedCategory = cat.value"
          >
            {{ cat.label }}
          </button>
        </div>
      </div>

      <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-for="i in 4" :key="i" class="bg-white rounded-xl p-5 border border-surface-200 animate-pulse">
          <div class="h-5 bg-surface-100 rounded w-3/4 mb-3"></div>
          <div class="h-4 bg-surface-100 rounded w-full mb-2"></div>
          <div class="h-4 bg-surface-100 rounded w-1/2"></div>
        </div>
      </div>

      <div v-else-if="filteredTasks.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div
          v-for="task in filteredTasks"
          :key="task.id"
          class="bg-white rounded-xl p-5 border border-surface-200 hover:border-accent-mauve-200 hover:shadow-medium transition-all cursor-pointer group"
          @click="handleViewTask(task.id)"
        >
          <div class="flex items-start justify-between mb-3">
            <h3 class="font-medium text-surface-900 group-hover:text-accent-mauve-600 transition-colors line-clamp-1">
              {{ task.title }}
            </h3>
            <StatusTag :status="task.status" size="sm" />
          </div>
          <p class="text-sm text-surface-500 line-clamp-2 mb-4">{{ task.description }}</p>
          <div class="flex items-center justify-between text-sm">
            <div class="flex items-center gap-4 text-surface-400">
              <span class="text-accent-mauve-600 font-semibold">¥{{ task.reward }}</span>
              <span class="flex items-center gap-1">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {{ formatDate(task.deadline) }}
              </span>
            </div>
            <span class="text-surface-400">{{ task.viewCount }}人浏览</span>
          </div>
        </div>
      </div>

      <div v-else class="text-center py-16">
        <div class="w-16 h-16 rounded-full bg-surface-100 flex items-center justify-center mx-auto mb-4">
          <svg class="w-8 h-8 text-surface-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <p class="text-surface-500">暂无相关任务</p>
        <p class="text-sm text-surface-400 mt-1">试试其他关键词或分类</p>
      </div>
    </div>
  </AppLayout>
</template>
