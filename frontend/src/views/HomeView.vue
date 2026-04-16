<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
</script>

<template>
  <div class="p-10">
    <h1 class="text-3xl font-bold">Welcome, {{ authStore.user?.name }}!</h1>
    <div class="mt-4 p-4 bg-blue-50 rounded">
      <p><strong>Email:</strong> {{ authStore.user?.email }}</p>
      <p><strong>Status:</strong> Registration Complete ✅</p>
    </div>
  </div>
</template>

<style scoped>

</style>