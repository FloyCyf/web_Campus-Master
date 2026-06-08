<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'
import BaseButton from '@/components/BaseButton.vue'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  role: 'helper',
  agreeTerms: false
})

const errors = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  general: ''
})

const loading = ref(false)
const showPassword = ref(false)

const validate = () => {
  let isValid = true
  Object.keys(errors).forEach(key => errors[key] = '')

  if (!form.username) {
    errors.username = '请输入用户名'
    isValid = false
  } else if (form.username.length < 3) {
    errors.username = '用户名长度不能少于3位'
    isValid = false
  }

  if (!form.phone) {
    errors.phone = '请输入手机号'
    isValid = false
  } else if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    errors.phone = '请输入有效的手机号'
    isValid = false
  }

  if (!form.password) {
    errors.password = '请输入密码'
    isValid = false
  } else if (form.password.length < 6) {
    errors.password = '密码长度不能少于6位'
    isValid = false
  }

  if (form.password !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  if (!form.agreeTerms) {
    alert('请阅读并同意用户协议')
    isValid = false
  }

  return isValid
}

const handleRegister = async () => {
  if (!validate()) return

  loading.value = true

  try {
    const data = await userApi.register({
      username: form.username,
      phone: form.phone,
      password: form.password,
      role: form.role
    })
    userStore.login({
      userId: data.userId,
      username: data.username,
      phone: data.phone,
      role: data.role,
      creditScore: data.creditScore || 100,
      token: data.token
    })
    router.push('/home')
  } catch (error) {
    errors.general = error.response?.data?.message || '注册失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-surface-50 flex items-center justify-center p-4">
    <div class="w-full max-w-lg">
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
        <h2 class="text-xl font-semibold text-surface-900 mb-6">注册账号</h2>

        <form @submit.prevent="handleRegister" class="space-y-5">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-surface-700 mb-1.5">用户名</label>
              <input
                v-model="form.username"
                type="text"
                class="input"
                placeholder="请输入用户名"
              />
              <p v-if="errors.username" class="mt-1.5 text-sm text-rose-600">{{ errors.username }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-surface-700 mb-1.5">用户角色</label>
              <select
                v-model="form.role"
                class="input"
              >
                <option value="helper">接单方</option>
                <option value="requester">需求方</option>
              </select>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">手机号</label>
            <input
              v-model="form.phone"
              type="tel"
              class="input"
              placeholder="请输入手机号"
            />
            <p v-if="errors.phone" class="mt-1.5 text-sm text-rose-600">{{ errors.phone }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">密码</label>
            <div class="relative">
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                class="input pr-10"
                placeholder="请输入密码（至少6位）"
              />
              <button
                type="button"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-surface-400 hover:text-surface-600"
                @click="showPassword = !showPassword"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path v-if="showPassword" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                  <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
              </button>
            </div>
            <p v-if="errors.password" class="mt-1.5 text-sm text-rose-600">{{ errors.password }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">确认密码</label>
            <input
              v-model="form.confirmPassword"
              :type="showPassword ? 'text' : 'password'"
              class="input"
              placeholder="请再次输入密码"
            />
            <p v-if="errors.confirmPassword" class="mt-1.5 text-sm text-rose-600">{{ errors.confirmPassword }}</p>
          </div>

          <p v-if="errors.general" class="text-sm text-rose-600">{{ errors.general }}</p>

          <div class="flex items-start">
            <input
              id="agree-terms"
              v-model="form.agreeTerms"
              type="checkbox"
              class="w-4 h-4 mt-1 rounded border-surface-300 text-accent-mauve-600 focus:ring-accent-mauve-500"
            />
            <label for="agree-terms" class="ml-2 text-sm text-surface-600">
              我已阅读并同意
              <a href="#" class="text-accent-mauve-600 hover:text-accent-mauve-700">《用户服务协议》</a>
              和
              <a href="#" class="text-accent-mauve-600 hover:text-accent-mauve-700">《隐私政策》</a>
            </label>
          </div>

          <BaseButton
            type="submit"
            variant="primary"
            block
            :loading="loading"
          >
            注册
          </BaseButton>
        </form>

        <div class="mt-6 text-center">
          <p class="text-sm text-surface-500">
            已有账号？
            <router-link to="/login" class="text-accent-mauve-600 hover:text-accent-mauve-700 font-medium">
              立即登录
            </router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
