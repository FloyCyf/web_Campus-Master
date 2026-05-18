<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { taskApi } from '@/api'
import AppLayout from '@/components/AppLayout.vue'
import StatusTag from '@/components/StatusTag.vue'
import BaseButton from '@/components/BaseButton.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const accepting = ref(false)
const task = ref(null)

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const canAccept = () => {
  if (!task.value) return false
  return task.value.status === 'pending' && userStore.role === 'helper' && task.value.requesterId !== userStore.userId
}

const canSubmit = () => {
  if (!task.value) return false
  return task.value.status === 'ongoing' && task.value.helperId === userStore.userId
}

const canReview = () => {
  if (!task.value) return false
  return task.value.status === 'pending_review' && task.value.requesterId === userStore.userId
}

const canRate = () => {
  if (!task.value) return false
  return task.value.status === 'completed'
}

const handleAccept = async () => {
  if (!canAccept()) return
  
  accepting.value = true
  try {
    await taskApi.accept({
      taskId: task.value.id,
      userId: userStore.userId
    })
    alert('接单成功！')
    router.push('/my-tasks')
  } catch (error) {
    alert(error.response?.data?.message || '接单失败，请重试')
  } finally {
    accepting.value = false
  }
}

const loadTask = async () => {
  loading.value = true
  try {
    const data = await taskApi.getDetail(route.params.id)
    task.value = data
  } catch (error) {
    console.error('加载任务详情失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTask()
})
</script>

<template>
  <AppLayout>
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="animate-spin w-8 h-8 border-2 border-accent-mauve-600 border-t-transparent rounded-full"></div>
    </div>

    <div v-else-if="!task" class="text-center py-20">
      <p class="text-surface-500">任务不存在</p>
    </div>

    <div v-else class="max-w-3xl mx-auto">
      <button class="flex items-center text-surface-500 hover:text-surface-700 mb-6" @click="router.back()">
        <svg class="w-5 h-5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
        返回
      </button>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="lg:col-span-2 space-y-6">
          <div class="bg-white rounded-xl border border-surface-200 p-6">
            <div class="flex items-start justify-between mb-4">
              <div>
                <h1 class="text-xl font-semibold text-surface-900 mb-2">{{ task.title }}</h1>
                <div class="flex items-center gap-3">
                  <StatusTag :status="task.status" />
                  <span class="text-sm text-surface-500">{{ task.categoryText }}</span>
                </div>
              </div>
              <div class="text-right">
                <div class="text-2xl font-bold text-accent-mauve-600">¥{{ task.reward }}</div>
                <div class="text-xs text-surface-400">服务费 ¥{{ task.serviceFee }}</div>
              </div>
            </div>
            <div class="prose prose-sm text-surface-600 whitespace-pre-line">{{ task.description }}</div>
            
            <div class="mt-4 pt-4 border-t border-surface-100">
              <div class="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span class="text-surface-500">任务地点：</span>
                  <span class="text-surface-900">{{ task.location }}</span>
                </div>
                <div>
                  <span class="text-surface-500">联系方式：</span>
                  <span class="text-surface-900">{{ task.contactInfo }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="bg-white rounded-xl border border-surface-200 p-6">
            <h3 class="font-medium text-surface-900 mb-4">对方信息</h3>
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-full bg-gradient-to-br from-accent-mauve-400 to-accent-mauve-500 flex items-center justify-center text-white font-medium">
                {{ task.requester?.username?.charAt(0) || '?' }}
              </div>
              <div>
                <p class="font-medium text-surface-900">{{ task.requester?.username }}</p>
                <p class="text-sm text-surface-500">{{ task.requester?.phone }}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="space-y-6">
          <div class="bg-white rounded-xl border border-surface-200 p-6">
            <h4 class="font-medium text-surface-900 mb-4">任务信息</h4>
            <div class="space-y-3 text-sm">
              <div class="flex justify-between">
                <span class="text-surface-500">任务编号</span>
                <span class="text-surface-900">#{{ task.id }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-surface-500">发布时间</span>
                <span class="text-surface-900">{{ formatDate(task.createTime) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-surface-500">截止时间</span>
                <span class="text-surface-900">{{ formatDate(task.deadline) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-surface-500">浏览次数</span>
                <span class="text-surface-900">{{ task.viewCount }}</span>
              </div>
            </div>
          </div>

          <div class="space-y-3">
            <BaseButton v-if="canAccept()" variant="primary" block :loading="accepting" @click="handleAccept">
              立即接单
            </BaseButton>
            
            <BaseButton v-if="canSubmit()" variant="primary" block @click="router.push(`/submit/${task.id}`)">
              提交完成
            </BaseButton>
            
            <BaseButton v-if="canReview()" variant="primary" block @click="router.push(`/review/${task.id}`)">
              验收任务
            </BaseButton>
            
            <BaseButton v-if="canRate()" variant="secondary" block @click="router.push(`/rate/${task.id}`)">
              评价对方
            </BaseButton>
          </div>

          <div v-if="task.status === 'pending_review' && task.requesterId !== userStore.userId" class="bg-amber-50 border border-amber-200 rounded-xl p-4">
            <div class="flex items-start gap-2">
              <svg class="w-5 h-5 text-amber-600 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <div class="text-sm text-amber-800">
                <p class="font-medium">等待验收</p>
                <p class="text-amber-700">需求方正在验收您提交的任务</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>
