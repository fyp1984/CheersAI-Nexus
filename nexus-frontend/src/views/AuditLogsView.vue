<template>
  <el-card class="page-container" shadow="hover">
    <!-- 顶部标题区 -->
    <div class="header">
      <div class="header-left">
        <div class="title">审计日志</div>
        <div class="subtitle">查询系统操作记录，支持合规审计和异常追溯</div>
      </div>
      <el-tag type="warning" size="large">P1</el-tag>
    </div>
    <!-- 统计卡片 -->
    <div class="stats-card mb-20">
      <div class="stat-item">
        <div class="stat-label">日志总数</div>
        <div class="stat-value">{{ stats.total }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">用户行为</div>
        <div class="stat-value primary">{{ stats.userAction }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">管理操作</div>
        <div class="stat-value success">{{ stats.adminAction }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">安全事件</div>
        <div class="stat-value danger">{{ stats.securityEvent }}</div>
      </div>
    </div>
    <!-- 搜索筛选区 -->
    <div class="search-section">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="日志类型">
          <el-select v-model="searchForm.logType" placeholder="请选择" clearable style="width: 140px">
            <el-option v-for="item in LOG_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-input v-model="searchForm.action" placeholder="请输入操作类型" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="searchForm.operatorName" placeholder="请输入操作人" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="searchForm.ipAddress" placeholder="请输入IP地址" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="操作结果">
          <el-select v-model="searchForm.result" placeholder="请选择" clearable style="width: 100px">
            <el-option v-for="item in RESULT_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <!-- TODO: 后端需要实现导出功能接口 -->
        <!-- 接口: GET /api/v1/audit-logs/export - 导出审计日志,返回文件流 -->
        <el-button type="primary" icon="el-icon-download" @click="handleExport">导出日志</el-button>
        <el-button icon="el-icon-refresh" @click="loadData">刷新</el-button>
        <!-- TODO: 后端需要实现清理功能接口 -->
        <!-- 接口: DELETE /api/v1/audit-logs/cleanup - 清理过期日志(已有,需测试) -->
        <el-button type="danger" icon="el-icon-delete" @click="handleCleanup">清理过期日志</el-button>
      </div>
      <div class="toolbar-right">
        <span class="total-count">共 {{ total }} 条记录</span>
      </div>
    </div>
    <!-- 日志列表 -->
    <el-table
      :data="tableData"
      v-loading="loading"
      border
      stripe
      style="width: 100%"
      :empty-text="'暂无日志记录'"
    >
      <el-table-column prop="createdAt" label="操作时间" width="180" align="center">
        <template #default="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column prop="logTypeDesc" label="日志类型" width="120" align="center">
        <template #default="scope">
          <el-tag :type="getLogTypeTag(scope.row.logType)">{{ scope.row.logTypeDesc }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="actionDesc" label="操作类型" width="140" align="center">
        <template #default="scope">
          <span>{{ scope.row.actionDesc || scope.row.action }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="operatorName" label="操作人" width="120" align="center" />
      <el-table-column prop="targetType" label="目标类型" width="120" align="center">
        <template #default="scope">
          <span v-if="scope.row.targetType">{{ scope.row.targetType }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="ipAddress" label="IP地址" width="140" align="center">
        <template #default="scope">
          <span v-if="scope.row.ipAddress">{{ scope.row.ipAddress }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="result" label="结果" width="80" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.result === 'success' ? 'success' : 'danger'" size="small">
            {{ scope.row.result === 'success' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="scope">
          <el-button size="small" type="text" @click="handleViewDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页器 -->
    <div class="pagination-container">

<!-- 分页器 -->
<div class="pagination-container">
  <el-pagination
    v-model:current-page="pagination.page"
    v-model:page-size="pagination.pageSize"
    :page-sizes="[10, 20, 50, 100]"
    :total="total"
    layout="total, sizes, prev, pager, next, jumper"
    @update:current-page="handlePageChange"
    @update:page-size="handleSizeChange"
  />
</div>
    </div>
    <!-- 日志详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="日志详情" width="800px" center>
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="日志ID" :span="2">
          <div class="log-id">{{ currentLog.id }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="日志类型">
          <el-tag :type="getLogTypeTag(currentLog.logType)">{{ currentLog.logTypeDesc }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作结果">
          <el-tag :type="currentLog.result === 'success' ? 'success' : 'danger'">
            {{ currentLog.result === 'success' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作类型" :span="2">
          {{ currentLog.actionDesc || currentLog.action }}
        </el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ currentLog.operatorName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="操作人ID">
          {{ currentLog.operatorId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="目标类型">
          {{ currentLog.targetType || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="目标ID">
          {{ currentLog.targetId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">
          {{ currentLog.ipAddress || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">
          {{ formatDateTime(currentLog.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMessage">
          <div class="error-message">{{ currentLog.errorMessage }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="操作前数据" :span="2">
          <pre class="json-content" v-if="currentLog.beforeData">{{ JSON.stringify(currentLog.beforeData, null, 2) }}</pre>
          <span v-else class="text-muted">无</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作后数据" :span="2">
          <pre class="json-content" v-if="currentLog.afterData">{{ JSON.stringify(currentLog.afterData, null, 2) }}</pre>
          <span v-else class="text-muted">无</span>
        </el-descriptions-item>
        <el-descriptions-item label="用户代理" :span="2">
          <div class="user-agent">{{ currentLog.userAgent || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  // ========== 后端接口已接入 ==========
  // 接口: GET /api/v1/audit-logs - 查询审计日志列表(带分页)
  fetchAuditLogs, 
  // 接口: GET /api/v1/audit-logs/{id} - 获取审计日志详情
  fetchAuditLogDetail,
  // 接口: GET /api/v1/audit-logs/export - 导出审计日志(需后端实现)
  exportAuditLogs, 
  // 接口: DELETE /api/v1/audit-logs/cleanup - 清理过期日志
  cleanExpiredLogs,
  downloadExportedFile,
  LOG_TYPE_OPTIONS, 
  RESULT_OPTIONS,
  formatDateTime,
  type AuditLogDTO 
} from '../api/audit'
// ========== 数据 ==========
const loading = ref(false)
const tableData = ref<AuditLogDTO[]>([])
const total = ref(0)
const detailDialogVisible = ref(false)
const currentLog = ref<AuditLogDTO | null>(null)
// 搜索表单
const searchForm = reactive({
  logType: '',
  action: '',
  operatorName: '',
  ipAddress: '',
  result: '',
  dateRange: [] as string[]
})
// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20
})
// 统计数据
const stats = reactive({
  total: 0,
  userAction: 0,
  adminAction: 0,
  securityEvent: 0
})
// ========== 方法 ==========
// 加载数据
// 接口: GET /api/v1/audit-logs - 查询审计日志列表
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      logType: searchForm.logType || undefined,
      action: searchForm.action || undefined,
      operatorName: searchForm.operatorName || undefined,
      ipAddress: searchForm.ipAddress || undefined,
      result: searchForm.result || undefined,
      startTime: searchForm.dateRange?.[0] || undefined,
      endTime: searchForm.dateRange?.[1] || undefined,
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    // 调用后端接口: GET /api/v1/audit-logs
    const result = await fetchAuditLogs(params)
    tableData.value = result.list
    total.value = result.total
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}
// 加载统计数据
// 接口: 多次调用 GET /api/v1/audit-logs 获取各类型日志数量
const loadStats = async () => {
  try {
    // 并行请求各类型日志数量
    const [userAction, adminAction, securityEvent] = await Promise.all([
      // 接口: GET /api/v1/audit-logs?logType=user_action&pageSize=1
      fetchAuditLogs({ logType: 'user_action', pageSize: 1 }),
      // 接口: GET /api/v1/audit-logs?logType=admin_action&pageSize=1
      fetchAuditLogs({ logType: 'admin_action', pageSize: 1 }),
      // 接口: GET /api/v1/audit-logs?logType=security_event&pageSize=1
      fetchAuditLogs({ logType: 'security_event', pageSize: 1 })
    ])
    stats.userAction = userAction.total
    stats.adminAction = adminAction.total
    stats.securityEvent = securityEvent.total
    stats.total = userAction.total + adminAction.total + securityEvent.total
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}
// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadData()
}
// 重置
const handleReset = () => {
  searchForm.logType = ''
  searchForm.action = ''
  searchForm.operatorName = ''
  searchForm.ipAddress = ''
  searchForm.result = ''
  searchForm.dateRange = []
  pagination.page = 1
  loadData()
}
// 分页变化（页码）
const handlePageChange = (page: number) => {
  pagination.page = page
  loadData()
}

// 每页数量变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.page = 1
  loadData()
}
// 查看详情
// 接口: GET /api/v1/audit-logs/{id} - 获取审计日志详情
const handleViewDetail = async (row: AuditLogDTO) => {
  try {
    // 调用后端接口: GET /api/v1/audit-logs/{id}
    const detail = await fetchAuditLogDetail(row.id)
    currentLog.value = detail
    detailDialogVisible.value = true
  } catch (error: any) {
    // 如果详情接口失败,使用列表数据
    currentLog.value = row
    detailDialogVisible.value = true
  }
}
// 导出
// 接口: GET /api/v1/audit-logs/export - 导出审计日志(需后端实现返回文件流)
const handleExport = async () => {
  try {
    await ElMessageBox.confirm('确定要导出当前筛选条件的日志吗？', '导出确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const params = {
      logType: searchForm.logType || undefined,
      action: searchForm.action || undefined,
      operatorName: searchForm.operatorName || undefined,
      ipAddress: searchForm.ipAddress || undefined,
      result: searchForm.result || undefined,
      startTime: searchForm.dateRange?.[0] || undefined,
      endTime: searchForm.dateRange?.[1] || undefined
    }
    // 调用后端接口: GET /api/v1/audit-logs/export
    const response = await exportAuditLogs(params)
    const filename = `审计日志_${new Date().toISOString().slice(0, 10)}.xlsx`
    downloadExportedFile(response.data, filename)
    ElMessage.success('导出成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '导出失败')
    }
  }
}
// 清理过期日志
// 接口: DELETE /api/v1/audit-logs/cleanup - 清理过期日志
const handleCleanup = async () => {
  try {
    await ElMessageBox.confirm('确定要清理过期日志吗？此操作不可恢复。', '清理确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 调用后端接口: DELETE /api/v1/audit-logs/cleanup
    const result = await cleanExpiredLogs()
    ElMessage.success(`清理完成，共删除 ${result} 条日志`)
    loadData()
    loadStats()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '清理失败')
    }
  }
}
// 获取日志类型标签
const getLogTypeTag = (logType: string) => {
  switch (logType) {
    case 'user_action': return 'primary'
    case 'admin_action': return 'success'
    case 'system_event': return 'info'
    case 'security_event': return 'danger'
    default: return 'info'
  }
}
// ========== 生命周期 ==========
onMounted(() => {
  loadData()
  loadStats()
})
</script>
<style scoped>
.page-container {
  padding: 24px;
  margin: 16px;
}
/* 头部样式 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e6e6e6;
}
.header-left {
  display: flex;
  flex-direction: column;
}
.title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}
.subtitle {
  font-size: 14px;
  color: #6b7280;
}
/* 统计卡片 */
.stats-card {
  display: flex;
  gap: 24px;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}
.stat-item {
  display: flex;
  flex-direction: column;
  flex: 1;
  text-align: center;
}
.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 4px;
}
.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}
.stat-value.primary { color: #409eff; }
.stat-value.success { color: #10b981; }
.stat-value.danger { color: #f56c6c; }
/* 搜索区 */
.search-section {
  margin-bottom: 16px;
  padding: 16px;
  background-color: #f9fafb;
  border-radius: 8px;
}
.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.toolbar-left {
  display: flex;
  gap: 8px;
}
.toolbar-right {
  display: flex;
  align-items: center;
}
.total-count {
  color: #6b7280;
  font-size: 14px;
}
/* 分页 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
/* 详情弹窗 */
.log-id {
  font-family: monospace;
  font-size: 12px;
  word-break: break-all;
}
.json-content {
  background-color: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  max-height: 200px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
.error-message {
  color: #f56c6c;
  font-size: 14px;
}
.user-agent {
  font-size: 12px;
  color: #6b7280;
  word-break: break-all;
}
.text-muted {
  color: #9ca3af;
}
/* 通用间距 */
.mb-20 {
  margin-bottom: 20px;
}
</style>