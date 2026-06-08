<script setup>
import { ref, computed, onMounted } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import StatusTag from '@/components/StatusTag.vue'
import BaseButton from '@/components/BaseButton.vue'
import { adminApi } from '@/api'

const loading = ref(true)
const activeTab = ref('monitor')
const stats = ref({
  totalUsers: 0,
  totalTasks: 0,
  pendingTasks: 0,
  completedTasks: 0,
  disputedTasks: 0,
  pendingAuditTasks: 0,
  openDisputes: 0,
  totalAmount: 0
})
const auditTasks = ref([])
const allTasks = ref([])
const disputes = ref([])
const users = ref([])

const tabs = computed(() => [
  { key: 'monitor', label: '平台监控', count: null },
  { key: 'audit', label: '违规任务审核', count: auditTasks.value.length },
  { key: 'disputes', label: '用户申诉处理', count: disputes.value.length },
  { key: 'users', label: '用户风控', count: users.value.filter(user => user.status === 1).length }
])

const taskStatusRows = computed(() => [
  { name: '待接单', value: stats.value.pendingTasks, color: 'bg-blue-500' },
  { name: '已完成', value: stats.value.completedTasks, color: 'bg-emerald-500' },
  { name: '争议中', value: stats.value.disputedTasks, color: 'bg-amber-500' },
  { name: '待审核', value: stats.value.pendingAuditTasks, color: 'bg-rose-500' }
])

const maxTaskMetric = computed(() => Math.max(...taskStatusRows.value.map(row => Number(row.value || 0)), 1))

const formatMoney = (value) => Number(value || 0).toFixed(2)
const formatDate = (value) => value ? new Date(value).toLocaleString('zh-CN') : '-'

const loadData = async () => {
  loading.value = true
  try {
    const [statsData, auditData, tasksData, disputesData, usersData] = await Promise.all([
      adminApi.getStatistics(),
      adminApi.getTasks({ pageSize: 50, auditStatus: 'pending' }),
      adminApi.getTasks({ pageSize: 50 }),
      adminApi.getDisputes({ pageSize: 50, status: 'pending' }),
      adminApi.getUsers({ pageSize: 50 })
    ])
    stats.value = { ...stats.value, ...statsData }
    auditTasks.value = auditData.list || []
    allTasks.value = tasksData.list || []
    disputes.value = disputesData.list || []
    users.value = usersData.list || []
  } catch (error) {
    console.error('加载管理端数据失败:', error)
    alert(error.response?.data?.message || '加载管理端数据失败')
  } finally {
    loading.value = false
  }
}

const approveTask = async (task) => {
  try {
    await adminApi.approveTask(task.id, { remark: '管理员审核通过' })
    auditTasks.value = auditTasks.value.filter(item => item.id !== task.id)
    await loadData()
  } catch (error) {
    alert(error.response?.data?.message || '审核通过失败')
  }
}

const takeDownTask = async (task) => {
  const reason = window.prompt('请输入下架原因', task.auditRemark || '任务内容疑似违规')
  if (!reason) return
  try {
    await adminApi.takeDownTask(task.id, { reason })
    auditTasks.value = auditTasks.value.filter(item => item.id !== task.id)
    await loadData()
  } catch (error) {
    alert(error.response?.data?.message || '下架任务失败')
  }
}

const resolveDispute = async (dispute, result) => {
  const defaultRemark = result === 'approve' ? '申诉成立，款项发放给接单方' : '申诉驳回，款项退回需求方'
  const remark = window.prompt('请输入处理说明', defaultRemark)
  if (!remark) return
  try {
    await adminApi.resolveDispute(dispute.id, { result, remark })
    disputes.value = disputes.value.filter(item => item.id !== dispute.id)
    await loadData()
  } catch (error) {
    alert(error.response?.data?.message || '申诉处理失败')
  }
}

const freezeUser = async (user) => {
  if (!confirm(`确认冻结用户 ${user.username || user.phone} 吗？`)) return
  try {
    await adminApi.freezeUser(user.id)
    await loadData()
  } catch (error) {
    alert(error.response?.data?.message || '冻结用户失败')
  }
}

onMounted(loadData)
</script>

<template>
  <AppLayout>
    <div class="space-y-6">
      <section class="page-hero">
        <div>
          <p class="page-kicker">Admin console</p>
          <h1 class="text-3xl font-semibold text-surface-950">管理工作台</h1>
          <p class="text-surface-500 mt-2">审核违规任务、处理用户申诉、监控平台运行数据</p>
        </div>
        <BaseButton variant="secondary" :loading="loading" @click="loadData">刷新数据</BaseButton>
      </section>

      <div class="grid grid-cols-2 lg:grid-cols-6 gap-4">
        <div class="surface-panel p-5">
          <p class="text-sm text-surface-500">用户总数</p>
          <p class="text-2xl font-semibold text-surface-950 mt-1">{{ stats.totalUsers }}</p>
        </div>
        <div class="surface-panel p-5">
          <p class="text-sm text-surface-500">任务总数</p>
          <p class="text-2xl font-semibold text-surface-950 mt-1">{{ stats.totalTasks }}</p>
        </div>
        <div class="surface-panel p-5">
          <p class="text-sm text-surface-500">待审核</p>
          <p class="text-2xl font-semibold text-rose-600 mt-1">{{ stats.pendingAuditTasks || auditTasks.length }}</p>
        </div>
        <div class="surface-panel p-5">
          <p class="text-sm text-surface-500">待处理申诉</p>
          <p class="text-2xl font-semibold text-amber-600 mt-1">{{ stats.openDisputes || disputes.length }}</p>
        </div>
        <div class="surface-panel p-5">
          <p class="text-sm text-surface-500">已完成</p>
          <p class="text-2xl font-semibold text-emerald-600 mt-1">{{ stats.completedTasks }}</p>
        </div>
        <div class="surface-panel p-5">
          <p class="text-sm text-surface-500">平台流水</p>
          <p class="text-2xl font-semibold text-surface-950 mt-1">¥{{ formatMoney(stats.totalAmount) }}</p>
        </div>
      </div>

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
              <span v-if="tab.count !== null" class="ml-2 text-xs px-2 py-0.5 rounded-full bg-surface-100 text-surface-500">{{ tab.count }}</span>
            </button>
          </div>
        </div>

        <div class="p-5">
          <div v-if="loading" class="space-y-3">
            <div v-for="i in 4" :key="i" class="h-20 bg-surface-50 rounded-xl animate-pulse"></div>
          </div>

          <div v-else-if="activeTab === 'monitor'" class="grid grid-cols-1 lg:grid-cols-2 gap-5">
            <div class="bg-white border border-surface-100 rounded-xl p-5">
              <h3 class="font-semibold text-surface-950 mb-4">任务状态监控</h3>
              <div class="space-y-4">
                <div v-for="row in taskStatusRows" :key="row.name" class="flex items-center gap-3">
                  <span class="w-16 text-sm text-surface-600">{{ row.name }}</span>
                  <div class="flex-1 h-2 bg-surface-100 rounded-full overflow-hidden">
                    <div :class="['h-full rounded-full', row.color]" :style="{ width: `${(Number(row.value || 0) / maxTaskMetric) * 100}%` }"></div>
                  </div>
                  <span class="w-10 text-right text-sm text-surface-500">{{ row.value || 0 }}</span>
                </div>
              </div>
            </div>
            <div class="bg-white border border-surface-100 rounded-xl p-5">
              <h3 class="font-semibold text-surface-950 mb-4">最近任务</h3>
              <div class="space-y-3">
                <div v-for="task in allTasks.slice(0, 6)" :key="task.id" class="flex items-center justify-between gap-3">
                  <div class="min-w-0">
                    <p class="font-medium text-surface-900 truncate">{{ task.title }}</p>
                    <p class="text-xs text-surface-400">#{{ task.id }} · ¥{{ formatMoney(task.reward) }}</p>
                  </div>
                  <StatusTag :status="task.status" size="sm" />
                </div>
                <div v-if="allTasks.length === 0" class="text-center py-8 text-surface-500">暂无任务数据</div>
              </div>
            </div>
          </div>

          <div v-else-if="activeTab === 'audit'" class="space-y-4">
            <article v-for="task in auditTasks" :key="task.id" class="bg-white border border-surface-100 rounded-xl p-5">
              <div class="flex items-start justify-between gap-4">
                <div class="min-w-0">
                  <div class="flex items-center gap-2 mb-2">
                    <h3 class="font-semibold text-surface-950 truncate">{{ task.title }}</h3>
                    <StatusTag :status="task.status" size="sm" />
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium bg-rose-100 text-rose-600">待审核</span>
                  </div>
                  <p class="text-sm text-surface-500 line-clamp-2">{{ task.description }}</p>
                  <p class="text-xs text-surface-400 mt-2">任务 #{{ task.id }} · 分类 {{ task.category }} · 奖励 ¥{{ formatMoney(task.reward) }} · 发布于 {{ formatDate(task.createTime) }}</p>
                </div>
                <div class="flex gap-2 shrink-0">
                  <BaseButton variant="outline" size="sm" @click="approveTask(task)">通过</BaseButton>
                  <BaseButton variant="danger" size="sm" @click="takeDownTask(task)">下架</BaseButton>
                </div>
              </div>
            </article>
            <div v-if="auditTasks.length === 0" class="text-center py-12 text-surface-500">暂无待审核任务</div>
          </div>

          <div v-else-if="activeTab === 'disputes'" class="space-y-4">
            <article v-for="dispute in disputes" :key="dispute.id" class="bg-amber-50 border border-amber-100 rounded-xl p-5">
              <div class="flex items-start justify-between gap-4">
                <div>
                  <h3 class="font-semibold text-surface-950">申诉 #{{ dispute.id }} · 任务 #{{ dispute.taskId }}</h3>
                  <p class="text-sm text-surface-600 mt-2">原因：{{ dispute.reason }}</p>
                  <p v-if="dispute.description" class="text-sm text-surface-500 mt-1">{{ dispute.description }}</p>
                  <p class="text-xs text-surface-400 mt-2">发起用户 #{{ dispute.initiatorId }} · {{ formatDate(dispute.createTime) }}</p>
                </div>
                <div class="flex gap-2 shrink-0">
                  <BaseButton variant="secondary" size="sm" @click="resolveDispute(dispute, 'reject')">退回需求方</BaseButton>
                  <BaseButton variant="primary" size="sm" @click="resolveDispute(dispute, 'approve')">发放接单方</BaseButton>
                </div>
              </div>
            </article>
            <div v-if="disputes.length === 0" class="text-center py-12 text-surface-500">暂无待处理申诉</div>
          </div>

          <div v-else class="space-y-4">
            <article v-for="user in users" :key="user.id" class="bg-white border border-surface-100 rounded-xl p-5 flex items-center justify-between gap-4">
              <div>
                <h3 class="font-semibold text-surface-950">{{ user.username || '未命名用户' }}</h3>
                <p class="text-sm text-surface-500 mt-1">{{ user.phone }} · {{ user.role }} · 信用分 {{ user.creditScore }}</p>
              </div>
              <div class="flex items-center gap-3">
                <span :class="['px-2 py-1 rounded-full text-xs font-medium', user.status === 1 ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700']">
                  {{ user.status === 1 ? '正常' : '已冻结' }}
                </span>
                <BaseButton v-if="user.status === 1 && user.role !== 'admin'" variant="danger" size="sm" @click="freezeUser(user)">冻结</BaseButton>
              </div>
            </article>
            <div v-if="users.length === 0" class="text-center py-12 text-surface-500">暂无用户数据</div>
          </div>
        </div>
      </section>
    </div>
  </AppLayout>
</template>
