<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { accountApi, reviewApi } from '@/api'
import AppLayout from '@/components/AppLayout.vue'
import BaseButton from '@/components/BaseButton.vue'

const userStore = useUserStore()

const loading = ref(true)
const recharging = ref(false)
const activeTab = ref('balance')
const fundFlows = ref([])
const receivedReviews = ref([])
const sentReviews = ref([])

const roleLabel = computed(() => {
  if (userStore.role === 'admin') return '管理员'
  if (userStore.role === 'requester') return '需求方'
  return '接单方'
})

const creditLevel = computed(() => {
  const score = userStore.creditScore
  if (score >= 90) return { level: '优秀', color: 'emerald' }
  if (score >= 70) return { level: '良好', color: 'blue' }
  if (score >= 60) return { level: '一般', color: 'amber' }
  return { level: '待提升', color: 'rose' }
})

const tabs = computed(() => [
  { key: 'balance', label: '资金流水', count: fundFlows.value.length },
  { key: 'received', label: '收到的评价', count: receivedReviews.value.length },
  { key: 'sent', label: '我给出的评价', count: sentReviews.value.length }
])

const currentReviews = computed(() => activeTab.value === 'received' ? receivedReviews.value : sentReviews.value)

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const loadReviews = async () => {
  try {
    const [received, sent] = await Promise.all([
      reviewApi.getReceived(),
      reviewApi.getSent()
    ])
    receivedReviews.value = received || []
    sentReviews.value = sent || []
  } catch (error) {
    receivedReviews.value = []
    sentReviews.value = []
    console.error('加载评价记录失败:', error)
  }
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

    await loadReviews()
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
    alert('充值金额必须大于 0')
    return
  }

  recharging.value = true
  try {
    const accountData = await accountApi.recharge(amount)
    userStore.setAccount(accountData)
    const flowData = await accountApi.getFundFlow()
    fundFlows.value = flowData.list || []
    alert('充值成功')
  } catch (error) {
    alert(error.response?.data?.message || '充值失败，请稍后重试')
  } finally {
    recharging.value = false
  }
}

onMounted(loadAccountInfo)
</script>

<template>
  <AppLayout>
    <div class="space-y-6">
      <section class="page-hero">
        <div>
          <p class="page-kicker">Profile</p>
          <h1 class="text-3xl font-semibold text-surface-950">个人中心</h1>
          <p class="text-surface-500 mt-2">查看账户资金、信用状态和互评记录</p>
        </div>
      </section>

      <div class="grid grid-cols-1 lg:grid-cols-[1fr_320px] gap-6">
        <div class="space-y-6">
          <section class="surface-panel p-6">
            <div class="flex items-center justify-between gap-4 mb-6">
              <div>
                <p class="page-kicker">Wallet</p>
                <h2 class="text-xl font-semibold text-surface-950">账户余额</h2>
              </div>
              <BaseButton variant="primary" size="sm" :loading="recharging" @click="handleRecharge">充值</BaseButton>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div class="metric-card bg-[var(--role-accent-soft)]">
                <p class="text-sm text-[var(--role-accent-strong)]">可用余额</p>
                <p class="text-3xl font-semibold text-[var(--role-accent-strong)] mt-2">¥{{ userStore.balance.toFixed(2) }}</p>
              </div>
              <div class="metric-card bg-emerald-50">
                <p class="text-sm text-emerald-700">已提现</p>
                <p class="text-3xl font-semibold text-emerald-700 mt-2">¥0.00</p>
              </div>
              <div class="metric-card bg-amber-50">
                <p class="text-sm text-amber-700">冻结中</p>
                <p class="text-3xl font-semibold text-amber-700 mt-2">¥{{ userStore.frozenBalance.toFixed(2) }}</p>
              </div>
            </div>
          </section>

          <section class="surface-panel overflow-hidden">
            <div class="border-b border-surface-100 bg-white/70">
              <div class="flex flex-wrap">
                <button
                  v-for="tab in tabs"
                  :key="tab.key"
                  :class="[
                    'px-5 py-4 text-sm font-semibold border-b-2 transition-colors',
                    activeTab === tab.key
                      ? 'border-[var(--role-accent)] text-[var(--role-accent)]'
                      : 'border-transparent text-surface-500 hover:text-surface-800'
                  ]"
                  @click="activeTab = tab.key"
                >
                  {{ tab.label }}
                  <span class="ml-2 text-xs px-2 py-0.5 rounded-full bg-surface-100 text-surface-500">{{ tab.count }}</span>
                </button>
              </div>
            </div>

            <div class="p-4">
              <div v-if="loading" class="space-y-3">
                <div v-for="i in 3" :key="i" class="h-16 bg-surface-50 rounded-xl animate-pulse"></div>
              </div>

              <div v-else-if="activeTab === 'balance'" class="space-y-3">
                <div v-for="flow in fundFlows" :key="flow.id" class="flex items-center justify-between gap-4 p-4 bg-white border border-surface-100 rounded-xl">
                  <div class="flex items-center gap-3">
                    <div :class="['w-11 h-11 rounded-xl flex items-center justify-center', flow.amount > 0 ? 'bg-emerald-100 text-emerald-700' : 'bg-surface-100 text-surface-600']">
                      <span class="font-semibold text-sm">{{ flow.amount > 0 ? '+' : '' }}{{ Number(flow.amount || 0).toFixed(2) }}</span>
                    </div>
                    <div>
                      <p class="font-medium text-surface-950">{{ flow.typeText }}</p>
                      <p class="text-xs text-surface-400">{{ formatDate(flow.createTime) }}</p>
                    </div>
                  </div>
                  <span class="text-sm text-surface-500 text-right">{{ flow.remark }}</span>
                </div>
                <div v-if="fundFlows.length === 0" class="text-center py-10 text-surface-500">暂无资金流水</div>
              </div>

              <div v-else class="space-y-3">
                <div v-for="review in currentReviews" :key="review.id" class="p-4 bg-white border border-surface-100 rounded-xl">
                  <div class="flex items-start justify-between gap-4">
                    <div>
                      <p class="font-medium text-surface-950">任务 #{{ review.taskId }}</p>
                      <p class="text-sm text-surface-500 mt-1">{{ review.content || '对方没有填写文字评价' }}</p>
                      <p class="text-xs text-surface-400 mt-2">{{ formatDate(review.createTime) }}</p>
                    </div>
                    <div class="flex items-center gap-1 text-amber-400 shrink-0">
                      <span v-for="star in 5" :key="star" :class="star <= review.rating ? 'text-amber-400' : 'text-surface-200'">★</span>
                    </div>
                  </div>
                </div>
                <div v-if="currentReviews.length === 0" class="text-center py-10 text-surface-500">
                  {{ activeTab === 'received' ? '暂无别人给你的评价' : '暂无你给出的评价' }}
                </div>
              </div>
            </div>
          </section>
        </div>

        <aside class="space-y-6">
          <section class="surface-panel p-6">
            <div class="text-center mb-5">
              <div class="w-20 h-20 rounded-2xl bg-[var(--role-accent)] flex items-center justify-center mx-auto mb-3 text-white text-2xl font-semibold shadow-soft">
                {{ userStore.username?.charAt(0) || 'U' }}
              </div>
              <h3 class="font-semibold text-surface-950">{{ userStore.username }}</h3>
              <p class="text-sm text-surface-500 mt-1">{{ roleLabel }}</p>
            </div>
            <div class="space-y-3 text-sm">
              <div class="flex justify-between gap-4"><span class="text-surface-500">手机号</span><span class="text-surface-900 truncate">{{ userStore.phone }}</span></div>
              <div class="flex justify-between gap-4"><span class="text-surface-500">收到评价</span><span class="text-surface-900">{{ receivedReviews.length }} 条</span></div>
              <div class="flex justify-between gap-4"><span class="text-surface-500">给出评价</span><span class="text-surface-900">{{ sentReviews.length }} 条</span></div>
            </div>
          </section>

          <section class="surface-panel p-6">
            <h3 class="font-semibold text-surface-950 mb-4">信用评分</h3>
            <div class="text-center mb-4">
              <div class="relative inline-flex items-center justify-center">
                <svg class="w-24 h-24 transform -rotate-90">
                  <circle cx="48" cy="48" r="40" stroke="#e5e7eb" stroke-width="6" fill="none" />
                  <circle cx="48" cy="48" r="40" :stroke="creditLevel.color === 'emerald' ? '#10B981' : creditLevel.color === 'blue' ? '#3B82F6' : creditLevel.color === 'amber' ? '#F59E0B' : '#EF4444'" stroke-width="6" fill="none" :stroke-dasharray="`${(userStore.creditScore / 100) * 251.2} 251.2`" stroke-linecap="round" />
                </svg>
                <div class="absolute inset-0 flex flex-col items-center justify-center">
                  <span class="text-2xl font-bold text-surface-950">{{ userStore.creditScore }}</span>
                  <span class="text-xs text-surface-500">信用分</span>
                </div>
              </div>
            </div>
            <div class="text-center">
              <span :class="['px-3 py-1 rounded-full text-sm font-medium', creditLevel.color === 'emerald' ? 'bg-emerald-100 text-emerald-700' : creditLevel.color === 'blue' ? 'bg-blue-100 text-blue-700' : creditLevel.color === 'amber' ? 'bg-amber-100 text-amber-700' : 'bg-rose-100 text-rose-700']">
                {{ creditLevel.level }}
              </span>
            </div>
          </section>
        </aside>
      </div>
    </div>
  </AppLayout>
</template>
