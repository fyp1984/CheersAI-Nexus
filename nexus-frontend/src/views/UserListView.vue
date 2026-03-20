<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchUserDetail, fetchUsers, searchUsers, updateUserStatus, type UserPage, type UserRecord } from '../api/users'

const loading = ref(false)
const detailLoading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalRow = ref(0)
const mode = ref<'list' | 'search'>('list')

const pageData = ref<UserPage>({
  records: [],
  pageNumber: 1,
  pageSize: 10,
  totalPage: 0,
  totalRow: 0
})

const searchData = ref<UserRecord[]>([])

const detailVisible = ref(false)
const detailRecord = ref<UserRecord | null>(null)

const statusOptions = [
  { label: 'active', value: 'active' },
  { label: 'inactive', value: 'inactive' },
  { label: 'disabled', value: 'disabled' },
  { label: 'deleted', value: 'deleted' }
]

const currentRows = computed(() => (mode.value === 'search' ? searchData.value : pageData.value.records))

function formatTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').replace('Z', '')
}

function getStatusTag(status?: string) {
  if (status === 'active') return 'success'
  if (status === 'inactive') return 'warning'
  if (status === 'disabled' || status === 'deleted') return 'danger'
  return 'info'
}

async function loadList() {
  loading.value = true
  try {
    const result = await fetchUsers({
      pageNumber: currentPage.value,
      pageSize: pageSize.value,
      totalRow: totalRow.value
    })
    pageData.value = {
      records: Array.isArray(result?.records) ? result.records : [],
      pageNumber: Number(result?.pageNumber ?? currentPage.value),
      pageSize: Number(result?.pageSize ?? pageSize.value),
      totalPage: Number(result?.totalPage ?? 0),
      totalRow: Number(result?.totalRow ?? 0)
    }
    totalRow.value = pageData.value.totalRow
    mode.value = 'list'
  } catch (error: any) {
    const message = error?.response?.data?.message || '获取用户列表失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    await loadList()
    return
  }

  loading.value = true
  try {
    searchData.value = await searchUsers(keyword)
    mode.value = 'search'
  } catch (error: any) {
    const message = error?.response?.data?.message || '搜索失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

async function openDetail(userId: string) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detailRecord.value = await fetchUserDetail(userId)
  } catch (error: any) {
    detailRecord.value = null
    const message = error?.response?.data?.message || '获取用户详情失败'
    ElMessage.error(message)
  } finally {
    detailLoading.value = false
  }
}

async function handleStatusChange(row: UserRecord, status: string) {
  if (!row.userId) {
    ElMessage.warning('缺少用户ID，无法更新状态')
    return
  }

  try {
    const updated = await updateUserStatus(row.userId, status)
    row.status = updated.status
    ElMessage.success('状态更新成功')
    if (detailRecord.value?.userId === row.userId) {
      detailRecord.value = { ...detailRecord.value, status: updated.status }
    }
  } catch (error: any) {
    const message = error?.response?.data?.message || '状态更新失败'
    ElMessage.error(message)
  }
}

function onStatusSelect(row: UserRecord, value: string | number | boolean) {
  void handleStatusChange(row, String(value))
}

watch([currentPage, pageSize], async () => {
  if (mode.value === 'list') {
    await loadList()
  }
})

onMounted(async () => {
  await loadList()
})
</script>

<template>
  <el-card class="page-container" shadow="hover">
    <div class="header">
      <div>
        <div class="title">用户管理</div>
        <div class="subtitle">当前仅对接：列表、搜索、详情、状态更新</div>
      </div>
    </div>

    <div class="toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索 userId / 昵称 / 邮箱 / 手机号"
        clearable
        style="width: 320px"
      />
      <div class="toolbar-actions">
        <el-button @click="handleSearch">查询</el-button>
        <el-button type="primary" @click="loadList">重置</el-button>
      </div>
    </div>

    <el-table :data="currentRows" v-loading="loading" border stripe>
      <el-table-column prop="userId" label="用户ID" min-width="160" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column prop="phone" label="手机号" min-width="140" />
      <el-table-column prop="status" label="状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="getStatusTag(scope.row.status)">{{ scope.row.status || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginAt" label="最后登录" min-width="170">
        <template #default="scope">{{ formatTime(scope.row.lastLoginAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center">
        <template #default="scope">
          <el-button size="small" @click="openDetail(scope.row.userId)">详情</el-button>
          <el-select
            :model-value="scope.row.status"
            style="width: 140px"
            size="small"
            @change="onStatusSelect(scope.row, $event)"
          >
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container" v-if="mode === 'list'">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalRow"
      />
    </div>

    <el-dialog v-model="detailVisible" title="用户详情" width="640px">
      <el-skeleton :rows="8" animated v-if="detailLoading" />
      <el-descriptions v-else :column="1" border>
        <el-descriptions-item label="用户ID">{{ detailRecord?.userId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailRecord?.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detailRecord?.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailRecord?.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailRecord?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailRecord?.status || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱已验证">{{ detailRecord?.emailVerified ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="手机号已验证">{{ detailRecord?.phoneVerified ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="最后登录时间">{{ formatTime(detailRecord?.lastLoginAt) }}</el-descriptions-item>
        <el-descriptions-item label="最后登录IP">{{ detailRecord?.lastLoginIp || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(detailRecord?.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.page-container {
  padding: 24px;
  margin: 16px;
}

.header {
  margin-bottom: 20px;
}

.title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.subtitle {
  margin-top: 6px;
  color: #6b7280;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.pagination-container {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}
</style>
