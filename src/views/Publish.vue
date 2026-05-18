<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { taskApi } from '@/api'
import AppLayout from '@/components/AppLayout.vue'
import BaseButton from '@/components/BaseButton.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const errors = reactive({
  title: '',
  description: '',
  category: '',
  deadline: '',
  reward: ''
})

const form = reactive({
  title: '',
  description: '',
  category: '',
  deadline: '',
  reward: '',
  location: '',
  contactInfo: ''
})

const categories = [
  { value: 'delivery', label: '跑腿代办' },
  { value: 'food', label: '美食外卖' },
  { value: 'print', label: '打印复印' },
  { value: 'other', label: '其他' }
]

const serviceFee = computed(() => {
  const reward = parseFloat(form.reward) || 0
  return reward > 0 ? (reward * 0.05).toFixed(2) : '0.00'
})

const totalFee = computed(() => {
  const reward = parseFloat(form.reward) || 0
  const fee = parseFloat(serviceFee.value) || 0
  return (reward + fee).toFixed(2)
})

const validate = () => {
  Object.keys(errors).forEach(key => errors[key] = '')
  
  if (!form.title.trim()) {
    errors.title = '请输入任务标题'
    return false
  }
  if (!form.description.trim()) {
    errors.description = '请输入任务描述'
    return false
  }
  if (!form.category) {
    errors.category = '请选择任务分类'
    return false
  }
  if (!form.deadline) {
    errors.deadline = '请选择截止时间'
    return false
  }
  if (!form.reward || parseFloat(form.reward) <= 0) {
    errors.reward = '请输入有效的报酬金额'
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!validate()) return

  loading.value = true

  try {
    await taskApi.publish({
      title: form.title,
      description: form.description,
      category: form.category,
      reward: parseFloat(form.reward),
      deadline: new Date(form.deadline).toISOString(),
      location: form.location,
      contactInfo: form.contactInfo
    })
    alert('任务发布成功！')
    router.push('/my-tasks')
  } catch (error) {
    console.error('发布任务失败:', error)
    alert('发布任务失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AppLayout>
    <div class="max-w-2xl mx-auto">
      <div class="mb-8">
        <h1 class="text-2xl font-semibold text-surface-900">发布任务</h1>
        <p class="text-surface-500 mt-1">描述您的需求，找到合适的帮助者</p>
      </div>

      <div class="bg-white rounded-xl border border-surface-200 p-8">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">任务标题</label>
            <input v-model="form.title" type="text" class="input" placeholder="简洁明了地描述任务" maxlength="50" />
            <p v-if="errors.title" class="mt-1.5 text-sm text-rose-600">{{ errors.title }}</p>
            <p v-else class="mt-1 text-xs text-surface-400">{{ form.title.length }}/50</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">任务分类</label>
            <p v-if="errors.category" class="text-sm text-rose-600 mb-1.5">{{ errors.category }}</p>
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <button
                v-for="cat in categories"
                :key="cat.value"
                type="button"
                :class="[
                  'p-4 rounded-xl border text-center transition-all',
                  form.category === cat.value
                    ? 'border-accent-mauve-500 bg-accent-mauve-50 text-accent-mauve-700'
                    : 'border-surface-200 hover:border-surface-300 hover:bg-surface-50'
                ]"
                @click="form.category = cat.value"
              >
                <span class="text-sm font-medium">{{ cat.label }}</span>
              </button>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">任务描述</label>
            <textarea
              v-model="form.description"
              rows="5"
              class="input resize-none"
              placeholder="详细描述任务内容、要求、地点、时间等信息..."
            />
            <p v-if="errors.description" class="mt-1.5 text-sm text-rose-600">{{ errors.description }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">任务地点</label>
            <input v-model="form.location" type="text" class="input" placeholder="例如：西区菜鸟驿站" />
          </div>

          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">联系方式</label>
            <input v-model="form.contactInfo" type="text" class="input" placeholder="您的联系方式" />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-surface-700 mb-1.5">截止时间</label>
              <input v-model="form.deadline" type="datetime-local" class="input" />
              <p v-if="errors.deadline" class="mt-1.5 text-sm text-rose-600">{{ errors.deadline }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-surface-700 mb-1.5">任务报酬 (元)</label>
              <input v-model="form.reward" type="number" class="input" placeholder="0.00" min="0" step="0.01" />
              <p v-if="errors.reward" class="mt-1.5 text-sm text-rose-600">{{ errors.reward }}</p>
            </div>
          </div>

          <div class="p-4 bg-surface-50 rounded-xl space-y-2">
            <div class="flex justify-between text-sm">
              <span class="text-surface-500">任务报酬</span>
              <span class="text-surface-700">¥{{ form.reward || '0.00' }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-surface-500">平台服务费 (5%)</span>
              <span class="text-surface-700">¥{{ serviceFee }}</span>
            </div>
            <div class="border-t border-surface-200 pt-2 flex justify-between">
              <span class="font-medium text-surface-700">合计</span>
              <span class="font-semibold text-accent-mauve-600">¥{{ totalFee }}</span>
            </div>
          </div>

          <div class="flex gap-3">
            <BaseButton type="button" variant="secondary" class="flex-1" @click="router.back()">
              取消
            </BaseButton>
            <BaseButton type="submit" variant="primary" class="flex-1" :loading="loading">
              发布任务
            </BaseButton>
          </div>
        </form>
      </div>
    </div>
  </AppLayout>
</template>
