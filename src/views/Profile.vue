<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { accountApi } from '@/api'
import AppLayout from '@/components/AppLayout.vue'
import BaseButton from '@/components/BaseButton.vue'

const userStore = useUserStore()

const loading = ref(true)
const recharging = ref(false)
const activeTab = ref('balance')
const fundFlows = ref([])

const creditLevel = computed(() => {
  const score = userStore.creditScore
  if (score >= 90) return { level: '优秀', color: 'emerald' }
  if (score >= 70) return { level: '良好', color: 'blue' }
  if (score >= 60) return { level: '一般', color: 'amber' }
  return { level: '较差', color: 'rose' }
})

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const loadAccountInfo = async () => {
  loading.value = true
  try {
    const accountData = await accountApi.getInfo()
    userStore.setAccount(accountData)

    try {
      const flowData = await accountApi.getFundFlow()
      fundFlows.value = flowData.list || []
    } catch (error) {
      fundFlows.value = []
      console.error('加载资金流水失败:', error)
    }
  } catch (error) {
    console.error('加载账户信息失败:', error)
  } finally {
    loading.value = false
  }
}

const handleRecharge = async () => {
  const value = window.prompt('请输入充值金额', '50')
  if (value === null) return

  const amount = Number(value)
  if (!Number.isFinite(amount) || amount <= 0) {
    alert('请输入大于 0 的充值金额')
    return
  }

  recharging.value = true
  try {
    const accountData = await accountApi.recharge(amount)
    userStore.setAccount(accountData)
    try {
      const flowData = await accountApi.getFundFlow()
      fundFlows.value = flowData.list || []
    } catch (error) {
      fundFlows.value = []
      console.error('加载资金流水失败:', error)
    }
    alert('充值成功')
  } catch (error) {
    alert(error.response?.data?.message || '充值失败，请重试')
  } finally {
    recharging.value = false
  }
}

onMounted(() => {
  loadAccountInfo()
})
</script>

<template>
  <AppLayout>
    <div class="max-w-4xl mx-auto">
      <div class="mb-8">
        <h1 class="text-2xl font-semibold text-surface-900">个人中心</h1>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="lg:col-span-2">
          <div class="bg-white rounded-xl border border-surface-200 p-6 mb-6">
            <div class="flex items-center justify-between mb-6">
              <h2 class="text-lg font-semibold text-surface-900">账户余额</h2>
              <BaseButton variant="primary" size="sm" :loading="recharging" @click="handleRecharge">充值</BaseButton>
            </div>
            <div class="grid grid-cols-3 gap-4">
              <div class="p-4 bg-accent-mauve-50 rounded-xl">
                <p class="text-sm text-accent-mauve-600">账户余额</p>
                <p class="text-2xl font-bold text-accent-mauve-700">¥{{ userStore.balance.toFixed(2) }}</p>
              </div>
              <div class="p-4 bg-emerald-50 rounded-xl">
                <p class="text-sm text-emerald-600">已提现</p>
                <p class="text-2xl font-bold text-emerald-700">¥0.00</p>
              </div>
              <div class="p-4 bg-amber-50 rounded-xl">
                <p class="text-sm text-amber-600">冻结中</p>
                <p class="text-2xl font-bold text-amber-700">¥{{ userStore.frozenBalance.toFixed(2) }}</p>
              </div>
            </div>
          </div>

          <div class="bg-white rounded-xl border border-surface-200">
            <div class="border-b border-surface-200">
              <div class="flex">
                <button
                  v-for="tab in [{ key: 'balance', label: '资金流水' }, { key: 'credit', label: '信用分记录' }]"
                  :key="tab.key"
                  :class="[
                    'px-6 py-4 text-sm font-medium border-b-2',
                    activeTab === tab.key
                      ? 'border-accent-mauve-600 text-accent-mauve-600'
                      : 'border-transparent text-surface-500'
                  ]"
                  @click="activeTab = tab.key"
                >
                  {{ tab.label }}
                </button>
              </div>
            </div>

            <div class="p-4">
              <div v-if="activeTab === 'balance'" class="space-y-3">
                <div v-for="flow in fundFlows" :key="flow.id" class="flex items-center justify-between p-3 bg-surface-50 rounded-lg">
                  <div class="flex items-center gap-3">
                    <div :class="['w-10 h-10 rounded-full flex items-center justify-center', flow.amount > 0 ? 'bg-emerald-100' : 'bg-surface-200']">
                      <span :class="flow.amount > 0 ? 'text-emerald-600' : 'text-surface-600'" class="font-bold text-sm">{{ flow.amount > 0 ? '+' : '' }}{{ flow.amount }}</span>
                    </div>
                    <div>
                      <p class="font-medium text-surface-900">{{ flow.typeText }}</p>
                      <p class="text-xs text-surface-400">{{ formatDate(flow.createTime) }}</p>
                    </div>
                  </div>
                  <span class="text-sm text-surface-500">{{ flow.remark }}</span>
                </div>
                <div v-if="fundFlows.length === 0" class="text-center py-8 text-surface-500">
                  暂无资金流水
                </div>
              </div>

              <div v-else class="space-y-3">
                <div class="flex items-center justify-between p-3 bg-surface-50 rounded-lg">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center bg-emerald-100">
                      <span class="text-emerald-600 font-bold">+5</span>
                    </div>
                    <div>
                      <p class="font-medium text-surface-900">完成任务</p>
                      <p class="text-xs text-surface-400">刚刚</p>
                    </div>
                  </div>
                </div>
                <div class="flex items-center justify-between p-3 bg-surface-50 rounded-lg">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center bg-rose-100">
                      <span class="text-rose-600 font-bold">-2</span>
                    </div>
                    <div>
                      <p class="font-medium text-surface-900">延迟完成</p>
                      <p class="text-xs text-surface-400">3天前</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div>
          <div class="bg-white rounded-xl border border-surface-200 p-6 mb-6">
            <div class="text-center mb-4">
              <div class="w-16 h-16 rounded-full bg-gradient-to-br from-accent-mauve-400 to-accent-mauve-500 flex items-center justify-center mx-auto mb-3 text-white text-xl font-medium">
                {{ userStore.username?.charAt(0) || 'U' }}
              </div>
              <h3 class="font-semibold text-surface-900">{{ userStore.username }}</h3>
              <p class="text-sm text-surface-500">{{ userStore.role === 'admin' ? '管理员' : userStore.role === 'requester' ? '需求方' : '接单方' }}</p>
            </div>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between"><span class="text-surface-500">手机号</span><span class="text-surface-900">{{ userStore.phone }}</span></div>
              <div class="flex justify-between"><span class="text-surface-500">注册时间</span><span class="text-surface-900">2024年9月</span></div>
            </div>
          </div>

          <div class="bg-white rounded-xl border border-surface-200 p-6">
            <h3 class="font-semibold text-surface-900 mb-4">信用评分</h3>
            <div class="text-center mb-4">
              <div class="relative inline-flex items-center justify-center">
                <svg class="w-24 h-24 transform -rotate-90">
                  <circle cx="48" cy="48" r="40" stroke="#e5e5e5" stroke-width="6" fill="none" />
                  <circle cx="48" cy="48" r="40" :stroke="creditLevel.color === 'emerald' ? '#10B981' : creditLevel.color === 'blue' ? '#3B82F6' : creditLevel.color === 'amber' ? '#F59E0B' : '#EF4444'" stroke-width="6" fill="none" :stroke-dasharray="`${(userStore.creditScore / 100) * 251.2} 251.2`" stroke-linecap="round" />
                </svg>
                <div class="absolute inset-0 flex flex-col items-center justify-center">
                  <span class="text-2xl font-bold text-surface-900">{{ userStore.creditScore }}</span>
                  <span class="text-xs text-surface-500">信用分</span>
                </div>
              </div>
            </div>
            <div class="text-center">
              <span :class="['px-3 py-1 rounded-full text-sm font-medium', creditLevel.color === 'emerald' ? 'bg-emerald-100 text-emerald-700' : creditLevel.color === 'blue' ? 'bg-blue-100 text-blue-700' : creditLevel.color === 'amber' ? 'bg-amber-100 text-amber-700' : 'bg-rose-100 text-rose-700']">
                {{ creditLevel.level }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>
