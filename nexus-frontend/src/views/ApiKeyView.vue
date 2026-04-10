<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchApiKeys,
  createApiKey,
  revokeApiKey,
  type ApiKey
} from '../api/api-keys'

const loading = ref(false)
const keys = ref<ApiKey[]>([])
const createDialogVisible = ref(false)
const newKeyName = ref('')

async function loadKeys() {
  loading.value = true
  try {
    const res = await fetchApiKeys()
    keys.value = res?.data ?? res ?? []
  } catch (e: any) {
    ElMessage.error('加载 API 密钥失败: ' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!newKeyName.value.trim()) {
    ElMessage.warning('请输入密钥名称')
    return
  }
  try {
    const res = await createApiKey(newKeyName.value.trim())
    const data = res?.data ?? res
    ElMessageBox.alert(
      `密钥已创建！请妥善保存以下信息（密钥仅显示一次）：\n\nKey ID: ${data.keyId}\nKey Secret: ${data.keySecret}`,
      'API 密钥创建成功',
      { confirmButtonText: '我已保存', type: 'success' }
    )
    newKeyName.value = ''
    createDialogVisible.value = false
    loadKeys()
  } catch (e: any) {
    ElMessage.error('创建失败: ' + (e.message || '未知错误'))
  }
}

async function handleRevoke(key: ApiKey) {
  try {
    await ElMessageBox.confirm(
      `确定要吊销密钥 "${key.name}" 吗？此操作不可撤销。`,
      '确认吊销',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await revokeApiKey(key.id)
    ElMessage.success('密钥已吊销')
    loadKeys()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  loadKeys()
})
</script>

<template>
  <el-card shadow="hover">
    <div class="header">
      <div class="header-left">
        <div class="title">API 密钥管理</div>
        <div class="subtitle">管理外部 API 访问密钥（Desktop 客户端认证）</div>
      </div>
      <el-button type="primary" @click="createDialogVisible = true">创建密钥</el-button>
    </div>

    <el-table :data="keys" v-loading="loading" stripe>
      <el-table-column prop="keyId" label="Key ID" width="280" show-overflow-tooltip />
      <el-table-column prop="name" label="名称" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : row.status === 'revoked' ? 'danger' : 'info'" size="small">
            {{ row.status === 'active' ? '有效' : row.status === 'revoked' ? '已吊销' : row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rateLimit" label="限频" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="170">
        <template #default="{ row }">{{ row.createdAt ? row.createdAt.replace('T', ' ').substring(0, 19) : '-' }}</template>
      </el-table-column>
      <el-table-column prop="lastUsedAt" label="最后使用" width="170">
        <template #default="{ row }">{{ row.lastUsedAt ? row.lastUsedAt.replace('T', ' ').substring(0, 19) : '从未使用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'active'"
            type="danger"
            size="small"
            text
            @click="handleRevoke(row)"
          >吊销</el-button>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建密钥对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建 API 密钥" width="460px">
      <el-form @submit.prevent="handleCreate">
        <el-form-item label="密钥名称" required>
          <el-input v-model="newKeyName" placeholder="输入密钥名称，如 Desktop-Production" maxlength="128" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.title { font-size: 20px; font-weight: 600; color: #1f2937; }
.subtitle { font-size: 14px; color: #6b7280; margin-top: 4px; }
.text-muted { color: #9ca3af; }
</style>
