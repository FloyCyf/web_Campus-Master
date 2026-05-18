<script setup>import { ref, onMounted } from 'vue';
import AppLayout from '@/components/AppLayout.vue';
import StatusTag from '@/components/StatusTag.vue';
import BaseButton from '@/components/BaseButton.vue';
import { adminApi } from '@/api';
const loading = ref(true);
const activeTab = ref('overview');
const stats = ref({
 totalUsers: 0,
 totalTasks: 0,
 pendingTasks: 0,
 completedTasks: 0,
 disputedTasks: 0,
 totalAmount: 0
});
const allTasks = ref([]);
const disputedTasks = ref([]);
const handleApprove = (task) => {
 alert(`任务 #${task.id} 已通过审核`);
 allTasks.value = allTasks.value.filter(t => t.id !== task.id);
};
const handleReject = (task) => {
 alert(`任务 #${task.id} 已拒绝`);
 allTasks.value = allTasks.value.filter(t => t.id !== task.id);
};
const handleResolve = async (task, decision) => {
 try {
 await adminApi.resolveDispute({
 taskId: task.id,
 result: decision === 'requester' ? 'reject' : 'approve',
 remark: decision === 'requester' ? '支持需求方' : '支持接单方'
 });
 alert(`争议 #${task.id} 已处理：${decision === 'requester' ? '资金退还需求方' : '资金支付接单方'}`);
 disputedTasks.value = disputedTasks.value.filter(t => t.id !== task.id);
 }
 catch (error) {
 alert('处理失败，请重试');
 }
};
const loadData = async () => {
 loading.value = true;
 try {
 const [statsData, tasksData, disputesData] = await Promise.all([
 adminApi.getStatistics(),
 adminApi.getTasks(),
 adminApi.getDisputes()
 ]);
 stats.value = statsData;
 allTasks.value = tasksData.list || [];
 disputedTasks.value = disputesData.list || [];
 }
 catch (error) {
 console.error('加载数据失败:', error);
 }
 finally {
 loading.value = false;
 }
};
onMounted(() => {
 loadData();
});
</script>

<template>
  <AppLayout>
    <div>
      <div class="mb-8">
        <h1 class="text-2xl font-semibold text-surface-900">管理后台</h1>
        <p class="text-surface-500 mt-1">系统管理与数据监控</p>
      </div>

      <div class="grid grid-cols-2 lg:grid-cols-6 gap-4 mb-6">
        <div class="bg-white rounded-xl p-4 border border-surface-200">
          <p class="text-sm text-surface-500 mb-1">用户总数</p>
          <p class="text-2xl font-bold text-surface-900">{{ stats.totalUsers.toLocaleString() }}</p>
        </div>
        <div class="bg-white rounded-xl p-4 border border-surface-200">
          <p class="text-sm text-surface-500 mb-1">任务总数</p>
          <p class="text-2xl font-bold text-surface-900">{{ stats.totalTasks }}</p>
        </div>
        <div class="bg-white rounded-xl p-4 border border-surface-200">
          <p class="text-sm text-surface-500 mb-1">待接单</p>
          <p class="text-2xl font-bold text-surface-900">{{ stats.pendingTasks }}</p>
        </div>
        <div class="bg-white rounded-xl p-4 border border-surface-200">
          <p class="text-sm text-surface-500 mb-1">已完成</p>
          <p class="text-2xl font-bold text-surface-900">{{ stats.completedTasks }}</p>
        </div>
        <div class="bg-white rounded-xl p-4 border border-surface-200">
          <p class="text-sm text-surface-500 mb-1">争议中</p>
          <p class="text-2xl font-bold text-surface-900">{{ stats.disputedTasks }}</p>
        </div>
        <div class="bg-white rounded-xl p-4 border border-surface-200">
          <p class="text-sm text-surface-500 mb-1">总交易额</p>
          <p class="text-2xl font-bold text-surface-900">¥{{ stats.totalAmount }}</p>
        </div>
      </div>

      <div class="bg-white rounded-xl border border-surface-200">
        <div class="border-b border-surface-200">
          <div class="flex">
            <button v-for="tab in [{ key: 'overview', label: '数据概览' }, { key: 'tasks', label: '任务列表' }, { key: 'disputes', label: '争议处理' }]" :key="tab.key" :class="['px-6 py-4 text-sm font-medium border-b-2', activeTab === tab.key ? 'border-accent-mauve-600 text-accent-mauve-600' : 'border-transparent text-surface-500']" @click="activeTab = tab.key">
              {{ tab.label }}
              <span v-if="tab.key === 'tasks' && allTasks.length > 0" class="ml-2 px-2 py-0.5 bg-rose-100 text-rose-600 text-xs rounded-full">{{ allTasks.length }}</span>
              <span v-if="tab.key === 'disputes' && disputedTasks.length > 0" class="ml-2 px-2 py-0.5 bg-amber-100 text-amber-600 text-xs rounded-full">{{ disputedTasks.length }}</span>
            </button>
          </div>
        </div>

        <div class="p-6">
          <div v-if="activeTab === 'overview'" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div class="bg-surface-50 rounded-xl p-6">
              <h3 class="font-semibold text-surface-900 mb-4">本周任务趋势</h3>
              <div class="h-40 flex items-end gap-2">
                <div v-for="(val, i) in [12, 18, 15, 22, 28, 25, 32]" :key="i" class="flex-1 bg-accent-mauve-200 rounded-t" :style="{ height: `${val * 3}px` }"></div>
              </div>
            </div>
            <div class="bg-surface-50 rounded-xl p-6">
              <h3 class="font-semibold text-surface-900 mb-4">任务分类分布</h3>
              <div v-for="cat in [{ name: '代取快递', percent: 35 }, { name: '代买外卖', percent: 27 }, { name: '文件打印', percent: 16 }, { name: '其他', percent: 22 }]" :key="cat.name" class="flex items-center gap-3 mb-3">
                <span class="w-20 text-sm text-surface-600">{{ cat.name }}</span>
                <div class="flex-1 bg-surface-200 rounded-full h-2"><div class="bg-accent-mauve-500 h-2 rounded-full" :style="{ width: `${cat.percent}%` }"></div></div>
                <span class="w-12 text-sm text-surface-500 text-right">{{ cat.percent }}%</span>
              </div>
            </div>
          </div>

          <div v-if="activeTab === 'tasks'" class="space-y-4">
            <div v-for="task in allTasks" :key="task.id" class="border border-surface-200 rounded-xl p-4">
              <div class="flex items-start justify-between">
                <div>
                  <div class="flex items-center gap-2">
                    <h4 class="font-medium text-surface-900">{{ task.title }}</h4>
                    <StatusTag :status="task.status" size="sm" />
                  </div>
                  <p class="text-sm text-surface-500 mt-1">{{ task.categoryText }} · ¥{{ task.reward }} · {{ task.location }}</p>
                  <p class="text-sm text-surface-600 mt-1">{{ task.description }}</p>
                </div>
                <div class="text-right">
                  <p class="text-xs text-surface-400">发布时间：{{ task.createTime }}</p>
                  <p class="text-xs text-surface-400">截止时间：{{ task.deadline }}</p>
                </div>
              </div>
            </div>
            <div v-if="allTasks.length === 0" class="text-center py-12 text-surface-500">暂无任务</div>
          </div>

          <div v-if="activeTab === 'disputes'" class="space-y-4">
            <div v-for="dispute in disputedTasks" :key="dispute.id" class="border border-amber-200 bg-amber-50 rounded-xl p-4">
              <div class="flex items-start justify-between mb-4">
                <div>
                  <h4 class="font-medium text-surface-900">{{ dispute.title }}</h4>
                  <p class="text-sm text-surface-500 mt-1">{{ dispute.requester }} · {{ dispute.helper }} · ¥{{ dispute.reward }}</p>
                </div>
                <StatusTag status="disputed" size="sm" />
              </div>
              <p class="text-sm text-surface-700 mb-4">争议原因：{{ dispute.disputeReason }}</p>
              <div class="flex justify-end gap-2">
                <BaseButton variant="secondary" size="sm" @click="handleResolve(dispute, 'helper')">判给接单方</BaseButton>
                <BaseButton variant="primary" size="sm" @click="handleResolve(dispute, 'requester')">判给需求方</BaseButton>
              </div>
            </div>
            <div v-if="disputedTasks.length === 0" class="text-center py-12 text-surface-500">暂无争议任务</div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>
