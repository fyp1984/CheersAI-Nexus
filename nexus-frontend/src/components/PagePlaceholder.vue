<script setup lang="ts">
defineProps<{
  title: string
  path: string
  priority: 'P0' | 'P1' | 'P2'
  description: string
}>()
</script>

<template>
  <div class="page-placeholder">
    <el-card shadow="never" class="placeholder-card">
      <template #header>
        <div class="card-header">
          <h2>{{ title }}</h2>
          <el-tag :type="priority === 'P0' ? 'danger' : priority === 'P1' ? 'warning' : 'info'">
            {{ priority }}
          </el-tag>
        </div>
      </template>

      <p class="path">{{ path }}</p>
      <p class="desc">{{ description }}</p>
    </el-card>
  </div>
</template>

<style scoped>
.page-placeholder {
  max-width: 960px;
}

.placeholder-card {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.path {
  margin: 12px 0;
  color: #6b7280;
  font-family: monospace;
  font-size: 13px;
}

.desc {
  margin: 0;
  color: #4b5563;
  font-size: 14px;
}
</style>
