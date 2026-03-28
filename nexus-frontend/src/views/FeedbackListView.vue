<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  assignFeedback,
  fetchFeedbackDetail,
  fetchFeedbackList,
  updateFeedback,
  type FeedbackRecord
} from '../api/feedback'
import { getErrorMessage } from '../utils/api'

const loading = ref(false)
const detailLoading = ref(false)
const actionLoading = ref(false)

const searchKeyword = ref('')
const productFilter = ref('')
const typeFilter = ref('')
const statusFilter = ref('')

const currentPage = ref(1)
const pageSize = ref(10)

const list = ref<FeedbackRecord[]>([])

const detailVisible = ref(false)
const assignVisible = ref(false)

const currentRow = ref<FeedbackRecord | null>(null)
const detailRecord = ref<FeedbackRecord | null>(null)

const assignForm = reactive({
  assigneeId: ''
})

const assigneeOptions = [
  { label: '运营小王', value: '4c6bcf29-958c-4f8e-89f8-f9a6cbef8f74' },
  { label: '产品老李', value: '0db4cf02-34f2-4bf7-932d-033e6a64a8dd' },
  { label: '技术小张', value: 'd09d2e6e-bdd9-4508-9f2f-4c151e53a96e' },
  { label: '客服小刘', value: 'd28d4329-e42e-4f2b-b96a-f5d616e5a531' }
]

const statusOptions = [
  { label: '待处理', value: 'pending' },
  { label: '处理中', value: 'processing' },
  { label: '已解决', value: 'resolved' },
  { label: '已关闭', value: 'closed' }
]

const priorityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'urgent' }
]

const typeOptions = [
  { label: '缺陷', value: 'bug' },
  { label: '功能建议', value: 'feature' },
  { label: '使用问题', value: 'question' },
  { label: '其他', value: 'other' }
]

const productOptions = computed(() => {
  const set = new Set<string>()
  list.value.forEach((item) => {
    if (item.productId) set.add(item.productId)
  })
  return Array.from(set)
})

const filteredList = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return list.value
  return list.value.filter((item) => {
    return (
      item.id.toLowerCase().includes(keyword) ||
      (item.title || '').toLowerCase().includes(keyword) ||
      (item.content || '').toLowerCase().includes(keyword) ||
      (item.userId || '').toLowerCase().includes(keyword)
    )
  })
})

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

const stats = computed(() => ({
  total: list.value.length,
  pending: list.value.filter((item) => item.status === 'pending').length,
  processing: list.value.filter((item) => item.status === 'processing').length,
  resolved: list.value.filter((item) => item.status === 'resolved').length,
  closed: list.value.filter((item) => item.status === 'closed').length
}))

function formatTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').replace('Z', '')
}

function getStatusLabel(status?: string) {
  return statusOptions.find((item) => item.value === status)?.label || status || '-'
}

function getPriorityLabel(priority?: string) {
  return priorityOptions.find((item) => item.value === priority)?.label || priority || '-'
}

function getTypeLabel(type?: string) {
  return typeOptions.find((item) => item.value === type)?.label || type || '-'
}

function getAssigneeLabel(assigneeId?: string) {
  if (!assigneeId) return '未分配'
  const matched = assigneeOptions.find((item) => item.value === assigneeId)
  return matched ? matched.label : assigneeId
}

function getProductLabel(productId?: string) {
  if (!productId) return '-'
  return `${productId.slice(0, 8)}...`
}

function getContentPreview(content?: string) {
  if (!content) return '-'
  return content.length > 48 ? `${content.slice(0, 48)}...` : content
}

function parseAttachments(value?: string) {
  if (!value) return []
  try {
    const parsed = JSON.parse(value)
    if (Array.isArray(parsed)) {
      return parsed.map((item) => String(item))
    }
  } catch {
    return [value]
  }
  return [value]
}

function getStatusTag(status: string) {
  if (status === 'pending') return 'warning'
  if (status === 'processing') return 'primary'
  if (status === 'resolved') return 'success'
  return 'info'
}

function getPriorityTag(priority: string) {
  if (priority === 'low') return 'info'
  if (priority === 'medium') return 'warning'
  if (priority === 'high') return 'danger'
  return 'danger'
}

async function loadList() {
  loading.value = true
  try {
    list.value = await fetchFeedbackList({
      product: productFilter.value || undefined,
      type: typeFilter.value || undefined,
      status: statusFilter.value || undefined
    })
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '获取反馈列表失败'))
  } finally {
    loading.value = false
  }
}

async function openDetail(row: FeedbackRecord) {
  detailVisible.value = true
  detailLoading.value = true
  currentRow.value = row
  try {
    detailRecord.value = await fetchFeedbackDetail(row.id)
  } catch (error) {
    detailRecord.value = null
    ElMessage.error(getErrorMessage(error, '获取反馈详情失败'))
  } finally {
    detailLoading.value = false
  }
}

async function onStatusChange(row: FeedbackRecord, status: string) {
  if (row.status === status) return
  const previous = row.status
  row.status = status
  try {
    actionLoading.value = true
    await updateFeedback(row.id, { status })
    ElMessage.success('状态更新成功')
    if (detailRecord.value?.id === row.id) {
      detailRecord.value = { ...detailRecord.value, status }
    }
  } catch (error) {
    row.status = previous
    ElMessage.error(getErrorMessage(error, '状态更新失败'))
  } finally {
    actionLoading.value = false
  }
}

async function onPriorityChange(row: FeedbackRecord, priority: string) {
  if (row.priority === priority) return
  const previous = row.priority
  row.priority = priority
  try {
    actionLoading.value = true
    await updateFeedback(row.id, { priority })
    ElMessage.success('优先级更新成功')
    if (detailRecord.value?.id === row.id) {
      detailRecord.value = { ...detailRecord.value, priority }
    }
  } catch (error) {
    row.priority = previous
    ElMessage.error(getErrorMessage(error, '优先级更新失败'))
  } finally {
    actionLoading.value = false
  }
}

function openAssignDialog(row: FeedbackRecord) {
  currentRow.value = row
  assignForm.assigneeId = row.assigneeId || ''
  assignVisible.value = true
}

async function submitAssign() {
  if (!currentRow.value) return
  if (!assignForm.assigneeId) {
    ElMessage.warning('请选择处理人')
    return
  }
  try {
    actionLoading.value = true
    await assignFeedback(currentRow.value.id, { assigneeId: assignForm.assigneeId })
    ElMessage.success('分配成功')
    assignVisible.value = false
    await loadList()
    if (detailVisible.value && detailRecord.value?.id === currentRow.value.id) {
      detailRecord.value = await fetchFeedbackDetail(currentRow.value.id)
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '分配失败'))
  } finally {
    actionLoading.value = false
  }
}

watch([searchKeyword], () => {
  currentPage.value = 1
})

watch([productFilter, typeFilter, statusFilter], async () => {
  currentPage.value = 1
  await loadList()
})

onMounted(async () => {
  await loadList()
})
</script>

<template>
  <el-card class="page-container" shadow="hover">
    <div class="header">
      <div>
        <div class="title">用户反馈管理</div>
        <div class="subtitle">仅对接：列表、详情、状态/优先级更新、分配处理人</div>
      </div>
    </div>

    <div class="stats-card">
      <div class="stat-item">
        <div class="stat-label">总反馈数</div>
        <div class="stat-value">{{ stats.total }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">待处理</div>
        <div class="stat-value warning">{{ stats.pending }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">处理中</div>
        <div class="stat-value primary">{{ stats.processing }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">已解决</div>
        <div class="stat-value success">{{ stats.resolved }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">已关闭</div>
        <div class="stat-value">{{ stats.closed }}</div>
      </div>
    </div>

    <div class="toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="前端关键词过滤：ID/标题/内容/用户ID"
        clearable
        style="width: 300px"
      />
      <el-select v-model="productFilter" placeholder="产品ID" clearable style="width: 180px">
        <el-option label="全部" value="" />
        <el-option v-for="item in productOptions" :key="item" :label="getProductLabel(item)" :value="item" />
      </el-select>
      <el-select v-model="typeFilter" placeholder="反馈类型" clearable style="width: 160px">
        <el-option label="全部" value="" />
        <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="处理状态" clearable style="width: 160px">
        <el-option label="全部" value="" />
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button :loading="loading" type="primary" @click="loadList">刷新</el-button>
    </div>

    <el-table :data="paginatedList" v-loading="loading || actionLoading" border stripe empty-text="暂无反馈数据">
      <el-table-column prop="id" label="反馈ID" min-width="180" show-overflow-tooltip />
      <el-table-column label="反馈信息" min-width="260">
        <template #default="scope">
          <div class="feedback-title">{{ scope.row.title || '-' }}</div>
          <div class="feedback-sub">{{ getContentPreview(scope.row.content) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="110" align="center">
        <template #default="scope">
          <el-tag effect="plain">{{ getTypeLabel(scope.row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="产品" width="120" align="center">
        <template #default="scope">{{ getProductLabel(scope.row.productId) }}</template>
      </el-table-column>
      <el-table-column prop="userId" label="用户ID" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="160" align="center">
        <template #default="scope">
          <el-select :model-value="scope.row.status" style="width: 130px" @change="onStatusChange(scope.row, String($event))">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="优先级" width="150" align="center">
        <template #default="scope">
          <el-select :model-value="scope.row.priority" style="width: 120px" @change="onPriorityChange(scope.row, String($event))">
            <el-option v-for="item in priorityOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="处理人" min-width="140" show-overflow-tooltip>
        <template #default="scope">{{ getAssigneeLabel(scope.row.assigneeId) }}</template>
      </el-table-column>
      <el-table-column label="提交时间" min-width="170">
        <template #default="scope">{{ formatTime(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button size="small" @click="openDetail(scope.row)">详情</el-button>
          <el-button size="small" type="primary" @click="openAssignDialog(scope.row)">分配</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredList.length"
      />
    </div>

    <el-dialog v-model="detailVisible" title="反馈详情" width="760px">
      <el-skeleton :rows="8" animated v-if="detailLoading" />
      <el-descriptions v-else :column="1" border>
        <el-descriptions-item label="反馈ID">{{ detailRecord?.id || '-' }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ detailRecord?.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="内容">{{ detailRecord?.content || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品ID">{{ detailRecord?.productId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detailRecord?.userId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag>{{ getTypeLabel(detailRecord?.type) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(detailRecord?.status || '')">{{ getStatusLabel(detailRecord?.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityTag(detailRecord?.priority || '')">{{ getPriorityLabel(detailRecord?.priority) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人">{{ getAssigneeLabel(detailRecord?.assigneeId) }}</el-descriptions-item>
        <el-descriptions-item label="附件">
          <div v-if="parseAttachments(detailRecord?.attachments).length">
            <div v-for="item in parseAttachments(detailRecord?.attachments)" :key="item">{{ item }}</div>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="解决时间">{{ formatTime(detailRecord?.resolvedAt) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(detailRecord?.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(detailRecord?.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="assignVisible" title="分配处理人" width="520px">
      <el-form label-width="100px">
        <el-form-item label="反馈ID">{{ currentRow?.id }}</el-form-item>
        <el-form-item label="处理人">
          <el-select v-model="assignForm.assigneeId" placeholder="请选择处理人" style="width: 100%">
            <el-option
              v-for="item in assigneeOptions"
              :key="item.value"
              :label="`${item.label} (${item.value})`"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button :loading="actionLoading" type="primary" @click="submitAssign">确认分配</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.page-container {
  padding: 0;
}

.header {
  margin-bottom: 24px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.subtitle {
  margin-top: 6px;
  color: #6b7280;
  font-size: 14px;
}

.stats-card {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-item {
  background: #f8fafc;
  border-radius: 8px;
  padding: 16px;
}

.stat-label {
  color: #6b7280;
  font-size: 14px;
}

.stat-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.stat-value.warning {
  color: #f59e0b;
}

.stat-value.primary {
  color: #3b82f6;
}

.stat-value.success {
  color: #10b981;
}

.feedback-title {
  font-weight: 600;
  color: #111827;
}

.feedback-sub {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
}

.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1100px) {
  .stats-card {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
