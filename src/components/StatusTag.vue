<script setup>
const props = defineProps({
  status: {
    type: String,
    default: 'pending'
  },
  size: {
    type: String,
    default: 'md'
  }
})

const statusMap = {
  pending: { label: '待接单', bg: 'bg-surface-100', text: 'text-surface-600', dot: 'bg-surface-400' },
  ongoing: { label: '进行中', bg: 'bg-accent-blue-100', text: 'text-accent-blue-700', dot: 'bg-accent-blue-500' },
  pending_review: { label: '待验收', bg: 'bg-amber-100', text: 'text-amber-700', dot: 'bg-amber-500' },
  completed: { label: '已完成', bg: 'bg-emerald-100', text: 'text-emerald-700', dot: 'bg-emerald-500' },
  disputed: { label: '争议中', bg: 'bg-rose-100', text: 'text-rose-700', dot: 'bg-rose-500' },
  cancelled: { label: '已取消', bg: 'bg-surface-100', text: 'text-surface-500', dot: 'bg-surface-400' }
}

const currentStatus = statusMap[props.status] || statusMap.pending
</script>

<template>
  <span
    :class="[
      'inline-flex items-center gap-1.5 rounded-full font-medium',
      currentStatus.bg,
      currentStatus.text,
      size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-1 text-xs'
    ]"
  >
    <span :class="['rounded-full', currentStatus.dot, size === 'sm' ? 'w-1.5 h-1.5' : 'w-2 h-2']"></span>
    {{ currentStatus.label }}
  </span>
</template>
