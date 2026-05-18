<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import Navbar from './Navbar.vue'

const userStore = useUserStore()

const shellClass = computed(() => `role-${userStore.role || 'guest'}`)
</script>

<template>
  <div :class="['min-h-screen app-shell', shellClass]">
    <Navbar />
    <main class="max-w-6xl mx-auto px-5 sm:px-6 py-8 sm:py-10">
      <slot />
    </main>
  </div>
</template>
