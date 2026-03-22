<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from './store/modules/auth'

const authStore = useAuthStore()

onMounted(() => {
  void authStore.initializeAuth()
})
</script>

<template>
  <RouterView />
</template>
