<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { taskApi } from '@/api'
import AppLayout from '@/components/AppLayout.vue'
import BaseButton from '@/components/BaseButton.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const task = ref(null)
const rating = ref(5)
const tags = ref({ timely: true, quality: true, attitude: false })
const comment = ref('')

const tagOptions = [
  { key: 'timely', label: '准时完成' },
  { key: 'quality', label: '质量优秀' },
  { key: 'attitude', label: '态度友好' }
]

const setRating = (val) => { rating.value = val }

const targetName = computed(() => {
  if (!task.value) return ''
  return task.value.requesterId === userStore.userId ? '接单方' : '需求方'
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const selectedTags = tagOptions.filter(tag => tags.value[tag.key]).map(tag => tag.label)
    const content = [comment.value, selectedTags.join('、')].filter(Boolean).join('｜')
    await taskApi.rate(route.params.id, { rating: rating.value, comment: content })
    alert('评价成功！')
    router.push('/my-tasks')
  } catch (error) {
    alert(error.response?.data?.message || '评价失败，请重试')
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
    <div class="max-w-md mx-auto">
      <div class="mb-8">
        <h1 class="text-2xl font-semibold text-surface-900">任务评价</h1>
        <p class="text-surface-500 mt-1">为本次互助体验打分</p>
      </div>

      <div class="bg-white rounded-xl border border-surface-200 p-8">
        <div class="text-center mb-8">
          <div class="w-16 h-16 rounded-full bg-gradient-to-br from-accent-mauve-400 to-accent-mauve-500 flex items-center justify-center mx-auto mb-3 text-white text-xl font-medium">
            {{ userStore.userInfo?.username?.charAt(0) || 'U' }}
          </div>
          <h3 class="font-medium text-surface-900">{{ targetName || '对方用户' }}</h3>
          <p class="text-sm text-surface-500">本次互助对象</p>
        </div>

        <div class="mb-8">
          <p class="text-sm text-surface-600 text-center mb-3">给本次服务打分</p>
          <div class="flex justify-center gap-2">
            <button v-for="star in 5" :key="star" @click="setRating(star)">
              <svg :class="['w-10 h-10 transition-colors', star <= rating ? 'text-amber-400' : 'text-surface-200']" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
              </svg>
            </button>
          </div>
          <p class="text-center text-sm text-surface-500 mt-2">
            {{ ['', '非常不满意', '不满意', '一般', '满意', '非常满意'][rating] }}
          </p>
        </div>

        <div class="mb-6">
          <p class="text-sm text-surface-600 mb-3">服务标签（可多选）</p>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="tag in tagOptions"
              :key="tag.key"
              :class="[
                'px-4 py-2 rounded-full text-sm font-medium transition-all',
                tags[tag.key]
                  ? 'bg-accent-mauve-100 text-accent-mauve-700'
                  : 'bg-surface-100 text-surface-600'
              ]"
              @click="tags[tag.key] = !tags[tag.key]"
            >
              {{ tag.label }}
            </button>
          </div>
        </div>

        <div class="mb-6">
          <p class="text-sm text-surface-600 mb-1.5">评价留言</p>
          <textarea
            v-model="comment"
            rows="3"
            class="input resize-none"
            placeholder="分享您的体验..."
          />
        </div>

        <div class="flex gap-3">
          <BaseButton variant="secondary" class="flex-1" @click="router.push('/my-tasks')">跳过</BaseButton>
          <BaseButton variant="primary" class="flex-1" :loading="loading" @click="handleSubmit">提交评价</BaseButton>
        </div>
      </div>
    </div>
  </AppLayout>
</template>
