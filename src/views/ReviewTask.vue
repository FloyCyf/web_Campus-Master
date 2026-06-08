<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { taskApi } from '@/api'
import AppLayout from '@/components/AppLayout.vue'
import BaseButton from '@/components/BaseButton.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const rejectReason = ref('')
const task = ref(null)

const submission = computed(() => {
  if (!task.value?.proofImages) {
    return { description: '接单方未填写完成说明', images: [], submittedAt: task.value?.updateTime }
  }
  try {
    return JSON.parse(task.value.proofImages)
  } catch {
    return { description: task.value.proofImages, images: [], submittedAt: task.value?.updateTime }
  }
})

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const handleApprove = async () => {
  loading.value = true
  try {
    await taskApi.review(route.params.id)
    alert('验收通过！资金已转给接单方')
    router.push('/my-tasks')
  } catch (error) {
    alert(error.response?.data?.message || '验收失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleReject = async () => {
  if (!rejectReason.value) {
    alert('请填写拒绝原因')
    return
  }
  loading.value = true
  try {
    await taskApi.dispute(route.params.id, { reason: rejectReason.value })
    alert('已发起争议，等待管理员处理')
    router.push('/my-tasks')
  } catch (error) {
    alert(error.response?.data?.message || '发起争议失败，请重试')
  } finally {
    loading.value = false
  }
}

const loadTask = async () => {
  try {
    task.value = await taskApi.getDetail(route.params.id)
  } catch (error) {
    alert(error.response?.data?.message || '任务加载失败')
    router.push('/my-tasks')
  }
}

onMounted(loadTask)
</script>

<template>
  <AppLayout>
    <div class="max-w-2xl mx-auto">
      <div class="mb-8">
        <h1 class="text-2xl font-semibold text-surface-900">验收任务</h1>
        <p class="text-surface-500 mt-1">检查任务完成情况，确认支付</p>
      </div>

      <div class="bg-white rounded-xl border border-surface-200 p-6 space-y-6">
        <div class="p-4 bg-surface-50 rounded-xl">
          <p class="text-sm text-surface-500">任务报酬</p>
          <p class="text-2xl font-bold text-accent-mauve-600">¥{{ Number(task?.reward || 0).toFixed(2) }}</p>
        </div>

        <div>
          <h3 class="font-medium text-surface-900 mb-2">完成说明</h3>
          <div class="p-4 bg-surface-50 rounded-xl">
            <p class="text-surface-700">{{ submission.description }}</p>
          </div>
        </div>

        <div v-if="submission.images?.length">
          <h3 class="font-medium text-surface-900 mb-2">凭证图片</h3>
          <div class="grid grid-cols-3 gap-4">
            <div v-for="(img, i) in submission.images" :key="i" class="aspect-square rounded-lg overflow-hidden bg-surface-100">
              <img :src="img" class="w-full h-full object-cover" />
            </div>
          </div>
        </div>

        <div class="p-4 bg-emerald-50 rounded-xl text-emerald-800 text-sm">
          验收通过后，资金将立即转给接单方
        </div>

        <div>
          <label class="block text-sm font-medium text-surface-700 mb-1.5">验收不通过原因</label>
          <textarea
            v-model="rejectReason"
            rows="3"
            class="input resize-none"
            placeholder="如果验收不通过，请填写争议原因..."
          />
        </div>

        <div class="flex gap-3">
          <BaseButton variant="danger" class="flex-1" @click="handleReject">验收不通过</BaseButton>
          <BaseButton variant="primary" class="flex-1" :loading="loading" @click="handleApprove">验收通过</BaseButton>
        </div>
      </div>
    </div>
  </AppLayout>
</template>
