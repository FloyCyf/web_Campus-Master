<script setup>
defineProps({
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  text: {
    type: String,
    default: '加载中...'
  },
  fullscreen: {
    type: Boolean,
    default: false
  }
})

const sizeClasses = {
  sm: 'w-6 h-6',
  md: 'w-10 h-10',
  lg: 'w-16 h-16'
}
</script>

<template>
  <div
    :class="[
      'flex flex-col items-center justify-center',
      fullscreen ? 'fixed inset-0 bg-white/80 z-50' : 'py-8'
    ]"
  >
    <div
      :class="[
        'animate-spin rounded-full border-4 border-gray-200 border-t-primary-500',
        sizeClasses[size]
      ]"
    />
    <p v-if="text" class="mt-4 text-gray-600">{{ text }}</p>
  </div>
</template>
