<script setup lang="ts">
defineProps<{
  title: string
  path: string
  priority: 'P0' | 'P1' | 'P2'
  description: string
}>()
</script>

<template>
  <el-card shadow="never" class="page-card">
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
</template>

<style scoped>
.page-card {
  max-width: 960px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

h2 {
  margin: 0;
  font-size: 20px;
}

.path {
  margin: 8px 0;
  color: #64748b;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.desc {
  margin: 0 0 16px;
}
</style>
