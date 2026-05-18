<script setup>
import { computed } from 'vue'
import StatusTag from './StatusTag.vue'
import BaseButton from './BaseButton.vue'

const props = defineProps({
  task: {
    type: Object,
    required: true
  },
  showActions: {
    type: Boolean,
    default: true
  },
  userRole: {
    type: String,
    default: 'helper'
  }
})

const emit = defineEmits(['click', 'accept', 'view'])

const statusType = computed(() => {
  const statusMap = {
    '待接单': 'pending',
    '进行中': 'ongoing',
    '待验收': 'pending_review',
    '已完成': 'completed',
    '争议中': 'disputed',
    '已取消': 'cancelled'
  }
  return statusMap[props.task.status] || 'pending'
})

const deadlineText = computed(() => {
  if (!props.task.deadline) return '不限时间'
  const deadline = new Date(props.task.deadline)
  const now = new Date()
  const diff = deadline - now
  if (diff < 0) return '已过期'
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days === 0) return '今天截止'
  if (days === 1) return '明天截止'
  return `${days}天后截止`
})

const isExpired = computed(() => {
  if (!props.task.deadline) return false
  return new Date(props.task.deadline) < new Date()
})

const canAccept = computed(() => {
  return statusType.value === 'pending' && !isExpired.value
})

const canView = computed(() => {
  return true
})
</script>

<template>
  <div
    class="card cursor-pointer hover:shadow-card-hover"
    @click="emit('click', task)"
  >
    <div class="flex justify-between items-start mb-3">
      <div class="flex-1">
        <h3 class="text-lg font-semibold text-gray-900 mb-1 line-clamp-2">
          {{ task.title }}
        </h3>
        <p class="text-sm text-gray-500 line-clamp-2">
          {{ task.description }}
        </p>
      </div>
      <StatusTag :status="statusType" size="sm" class="ml-3 flex-shrink-0" />
    </div>

    <div class="flex items-center justify-between text-sm mb-4">
      <div class="flex items-center text-gray-500">
        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span :class="isExpired ? 'text-danger-500' : ''">{{ deadlineText }}</span>
      </div>
      <div class="flex items-center text-gray-500">
        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
        <span>{{ task.applicantCount || 0 }}人已接单</span>
      </div>
    </div>

    <div class="flex items-center justify-between">
      <div class="flex items-center">
        <span class="text-2xl font-bold text-primary-600">¥{{ task.reward }}</span>
        <span class="text-sm text-gray-500 ml-1">报酬</span>
      </div>
      <div class="flex gap-2" v-if="showActions">
        <BaseButton
          v-if="canAccept && userRole === 'helper'"
          variant="primary"
          size="sm"
          @click.stop="emit('accept', task)"
        >
          立即接单
        </BaseButton>
        <BaseButton
          v-else-if="canView"
          variant="secondary"
          size="sm"
          @click.stop="emit('view', task)"
        >
          查看详情
        </BaseButton>
      </div>
    </div>

    <div v-if="task.tags && task.tags.length" class="flex flex-wrap gap-2 mt-3 pt-3 border-t border-gray-100">
      <span
        v-for="tag in task.tags"
        :key="tag"
        class="px-2 py-0.5 text-xs bg-gray-100 text-gray-600 rounded"
      >
        {{ tag }}
      </span>
    </div>
  </div>
</template>
