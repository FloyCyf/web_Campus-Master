<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { taskApi } from '@/api'
import AppLayout from '@/components/AppLayout.vue'
import BaseButton from '@/components/BaseButton.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const uploadedImages = ref([])
const form = reactive({ description: '' })

const handleImageUpload = (event) => {
  const files = event.target.files
  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    if (file.size > 5 * 1024 * 1024) {
      alert('图片大小不能超过5MB')
      continue
    }
    const reader = new FileReader()
    reader.onload = (e) => {
      uploadedImages.value.push({ id: Date.now() + i, url: e.target.result })
    }
    reader.readAsDataURL(file)
  }
}

const removeImage = (id) => {
  uploadedImages.value = uploadedImages.value.filter(img => img.id !== id)
}

const handleSubmit = async () => {
  if (!form.description) {
    alert('请填写完成说明')
    return
  }
  loading.value = true
  try {
    const proofImages = JSON.stringify({
      description: form.description,
      images: uploadedImages.value.map(img => img.url)
    })
    await taskApi.submit(route.params.id, { proofImages })
    alert('提交成功！等待验收')
    router.push('/my-tasks')
  } catch (error) {
    alert(error.response?.data?.message || '提交失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AppLayout>
    <div class="max-w-2xl mx-auto">
      <div class="mb-8">
        <h1 class="text-2xl font-semibold text-surface-900">提交完成凭证</h1>
        <p class="text-surface-500 mt-1">上传任务完成的证明材料</p>
      </div>

      <div class="bg-white rounded-xl border border-surface-200 p-6">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">完成说明</label>
            <textarea
              v-model="form.description"
              rows="4"
              class="input resize-none"
              placeholder="请详细描述任务完成情况..."
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-surface-700 mb-2">上传凭证（可选）</label>
            <div class="grid grid-cols-3 gap-4">
              <div v-for="img in uploadedImages" :key="img.id" class="relative aspect-square rounded-lg overflow-hidden bg-surface-100">
                <img :src="img.url" class="w-full h-full object-cover" />
                <button type="button" class="absolute top-2 right-2 w-6 h-6 bg-black/50 text-white rounded-full flex items-center justify-center" @click="removeImage(img.id)">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              <label v-if="uploadedImages.length < 9" class="aspect-square rounded-lg border-2 border-dashed border-surface-300 flex flex-col items-center justify-center cursor-pointer hover:border-accent-mauve-400 hover:bg-accent-mauve-50 transition-colors">
                <svg class="w-8 h-8 text-surface-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                </svg>
                <span class="text-sm text-surface-500 mt-2">添加图片</span>
                <input type="file" accept="image/*" multiple class="hidden" @change="handleImageUpload" />
              </label>
            </div>
          </div>

          <div class="flex gap-3">
            <BaseButton type="button" variant="secondary" class="flex-1" @click="router.back()">取消</BaseButton>
            <BaseButton type="submit" variant="primary" class="flex-1" :loading="loading">提交完成</BaseButton>
          </div>
        </form>
      </div>
    </div>
  </AppLayout>
</template>
