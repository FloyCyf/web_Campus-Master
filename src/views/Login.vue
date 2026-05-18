<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'
import BaseButton from '@/components/BaseButton.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const form = reactive({
  phone: '',
  password: ''
})
const errors = reactive({
  phone: '',
  password: '',
  general: ''
})

const validatePhone = (value) => {
  if (!value) {
    errors.phone = '请输入手机号'
    return false
  }
  if (!/^1[3-9]\d{9}$/.test(value)) {
    errors.phone = '手机号格式不正确'
    return false
  }
  errors.phone = ''
  return true
}

const validatePassword = (value) => {
  if (!value) {
    errors.password = '请输入密码'
    return false
  }
  if (value.length < 6) {
    errors.password = '密码至少6位'
    return false
  }
  errors.password = ''
  return true
}

const handleLogin = async () => {
  errors.general = ''
  const phoneValid = validatePhone(form.phone)
  const passwordValid = validatePassword(form.password)

  if (!phoneValid || !passwordValid) return

  loading.value = true

  try {
    const data = await userApi.login({
      phone: form.phone,
      password: form.password
    })
    userStore.login(data)
    router.push('/home')
  } catch (error) {
    errors.general = error.response?.data?.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-surface-50 flex items-center justify-center px-4">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-accent-mauve-500 to-accent-mauve-600 flex items-center justify-center mx-auto mb-4 shadow-lg">
          <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
        </div>
        <h1 class="text-2xl font-semibold text-surface-900">校园万事达</h1>
        <p class="text-surface-500 mt-1">互助与众包任务平台</p>
      </div>

      <div class="bg-white rounded-2xl shadow-medium p-8 border border-surface-100">
        <h2 class="text-xl font-semibold text-surface-900 mb-6">登录账号</h2>

        <form @submit.prevent="handleLogin" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">手机号</label>
            <input
              v-model="form.phone"
              type="tel"
              class="input"
              placeholder="请输入手机号"
              @blur="validatePhone(form.phone)"
            />
            <p v-if="errors.phone" class="mt-1.5 text-sm text-rose-600">{{ errors.phone }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">密码</label>
            <input
              v-model="form.password"
              type="password"
              class="input"
              placeholder="请输入密码"
              @blur="validatePassword(form.password)"
            />
            <p v-if="errors.password" class="mt-1.5 text-sm text-rose-600">{{ errors.password }}</p>
          </div>

          <p v-if="errors.general" class="text-sm text-rose-600">{{ errors.general }}</p>

          <div class="flex items-center justify-between text-sm">
            <label class="flex items-center gap-2 cursor-pointer">
              <input type="checkbox" class="w-4 h-4 rounded border-surface-300 text-accent-mauve-600 focus:ring-accent-mauve-500" />
              <span class="text-surface-600">记住我</span>
            </label>
            <a href="#" class="text-accent-mauve-600 hover:text-accent-mauve-700">忘记密码？</a>
          </div>

          <BaseButton
            type="submit"
            variant="primary"
            block
            :loading="loading"
          >
            登录
          </BaseButton>
        </form>

        <div class="mt-6 text-center text-sm text-surface-500">
          还没有账号？
          <router-link to="/register" class="text-accent-mauve-600 hover:text-accent-mauve-700 font-medium">
            立即注册
          </router-link>
        </div>
      </div>

      <div class="mt-6 p-4 bg-surface-100 rounded-xl">
        <p class="text-xs text-surface-500 text-center mb-2">测试账号</p>
        <div class="grid grid-cols-3 gap-2 text-xs">
          <div class="bg-white rounded-lg p-2 text-center">
            <p class="font-medium text-surface-700">需求方</p>
            <p class="text-surface-500">13800000001</p>
          </div>
          <div class="bg-white rounded-lg p-2 text-center">
            <p class="font-medium text-surface-700">接单方</p>
            <p class="text-surface-500">13800000003</p>
          </div>
          <div class="bg-white rounded-lg p-2 text-center">
            <p class="font-medium text-surface-700">管理员</p>
            <p class="text-surface-500">13800000000</p>
          </div>
        </div>
        <p class="text-xs text-surface-400 text-center mt-2">需求方/接单方密码：123456</p>
        <p class="text-xs text-surface-400 text-center">管理员密码：Admin123</p>
      </div>
    </div>
  </div>
</template>
