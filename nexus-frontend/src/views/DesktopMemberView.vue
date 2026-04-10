<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchDesktopMembers,
  fetchDesktopMemberStats,
  type DesktopMember,
  type DesktopMemberStats
} from '../api/desktop-members'

const loading = ref(false)
const members = ref<DesktopMember[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const statusFilter = ref('')
const stats = ref<DesktopMemberStats | null>(null)

async function loadData() {
  loading.value = true
  try {
    const res = await fetchDesktopMembers({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined
    })
    const data = res?.data ?? res
    members.value = data?.items ?? []
    total.value = data?.total ?? 0
  } catch (e: any) {
    ElMessage.error('加载桌面会员失败: ' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    const res = await fetchDesktopMemberStats()
    stats.value = res?.data ?? res
  } catch {
    // silent
  }
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadData()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

onMounted(() => {
  loadData()
  loadStats()
})
</script>

<template>
  <el-card shadow="hover">
    <div class="header">
      <div class="header-left">
        <div class="title">桌面会员管理</div>
        <div class="subtitle">管理 CheersAI Desktop 客户端用户</div>
      </div>
      <el-tag type="success" size="large">P1</el-tag>
    </div>

    <!-- 统计卡片 -->
    <div v-if="stats" class="stats-card">
      <div class="stat-item">
        <div class="stat-label">总会员</div>
        <div class="stat-value">{{ stats.totalMembers }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">DAU</div>
        <div class="stat-value primary">{{ stats.dau }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">WAU</div>
        <div class="stat-value success">{{ stats.wau }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">MAU</div>
        <div class="stat-value warning">{{ stats.mau }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">今日事件</div>
        <div class="stat-value">{{ stats.todayEvents }}</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名/邮箱/SSO ID"
        clearable
        style="width: 300px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-select v-model="statusFilter" placeholder="状态" style="width: 120px; margin-left: 10px" @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="活跃" value="active" />
        <el-option label="非活跃" value="inactive" />
      </el-select>
      <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
    </div>

    <!-- 会员列表 -->
    <el-table :data="members" v-loading="loading" stripe style="margin-top: 16px">
      <el-table-column prop="ssoUserId" label="SSO 用户 ID" width="200" show-overflow-tooltip />
      <el-table-column prop="name" label="用户名" width="140" />
      <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
            {{ row.status === 'active' ? '活跃' : row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginAt" label="最后登录" width="170">
        <template #default="{ row }">{{ row.lastLoginAt ? row.lastLoginAt.replace('T', ' ').substring(0, 19) : '-' }}</template>
      </el-table-column>
      <el-table-column prop="lastLoginIp" label="最后 IP" width="140" />
      <el-table-column prop="appVersion" label="版本" width="100" />
      <el-table-column prop="createdAt" label="注册时间" width="170">
        <template #default="{ row }">{{ row.createdAt ? row.createdAt.replace('T', ' ').substring(0, 19) : '-' }}</template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
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
.stats-card {
  display: flex;
  gap: 24px;
  padding: 16px 20px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 16px;
}
.stat-item { text-align: center; }
.stat-label { font-size: 12px; color: #9ca3af; }
.stat-value { font-size: 24px; font-weight: 700; color: #374151; margin-top: 4px; }
.stat-value.primary { color: #3b82f6; }
.stat-value.success { color: #10b981; }
.stat-value.warning { color: #f59e0b; }
.toolbar { display: flex; align-items: center; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
