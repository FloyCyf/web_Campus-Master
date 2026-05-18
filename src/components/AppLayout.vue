<script setup>
import Navbar from './Navbar.vue'
</script>

<template>
  <div class="min-h-screen bg-surface-50">
    <Navbar />
    <main class="max-w-6xl mx-auto px-6 py-8">
      <slot />
    </main>
  </div>
</template>
