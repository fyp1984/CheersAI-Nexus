<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  createUser,
  fetchUserDetail,
  fetchUsers,
  resetUserPassword,
  updateBatchUserStatus,
  updateUser,
  updateUserStatus,
  type MemberPlanCode,
  type UserRecord,
  type UserRole,
  type UserStatus
} from '../api/users'
import { fetchSubscriptionAuditLogs } from '../api/membership'
import { getErrorMessage } from '../utils/api'

type EditMode = 'create' | 'edit'

const statusOptions: Array<{ label: string; value: UserStatus }> = [
  { label: '正常', value: 'active' },
  { label: '未激活', value: 'inactive' },
  { label: '冻结', value: 'disabled' },
  { label: '已删除', value: 'deleted' }
]

const roleOptions: Array<{ label: string; value: UserRole }> = [
  { label: '普通用户', value: 'user' },
  { label: '客服', value: 'support' },
  { label: '运营', value: 'operator' },
  { label: '管理员', value: 'admin' }
]

const memberPlanOptions: Array<{ label: string; value: MemberPlanCode }> = [
  { label: '免费版', value: 'free' },
  { label: '专业版', value: 'pro' },
  { label: '团队版', value: 'pro_team' },
  { label: '企业版', value: 'enterprise' }
]

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const phonePattern = /^1\d{10}$/

const loading = ref(false)
const saving = ref(false)
const detailLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref<UserRecord[]>([])
const list = ref<UserRecord[]>([])

const filters = reactive({
  keyword: '',
  status: '',
  role: '',
  memberPlanCode: ''
})

const detailVisible = ref(false)
const detailRecord = ref<UserRecord | null>(null)

// 会员变更日志相关
const auditLogVisible = ref(false)
const auditLogLoading = ref(false)
const auditLogList = ref<any[]>([])
const currentAuditUserId = ref('')

const editVisible = ref(false)
const editMode = ref<EditMode>('create')
const formRef = ref<FormInstance>()
const form = reactive({
  userId: '',
  username: '',
  nickname: '',
  email: '',
  phone: '',
  password: '',
  status: 'active' as UserStatus,
  role: 'user' as UserRole,
  memberPlanCode: 'free' as MemberPlanCode,
  memberExpireAt: ''
})

function validateEmail(_rule: unknown, value: string, callback: (error?: Error) => void) {
  const email = (value || '').trim()
  const phone = form.phone.trim()
  if (email && !emailPattern.test(email)) {
    callback(new Error('邮箱格式不正确'))
    return
  }
  if (!email && !phone) {
    callback(new Error('邮箱和手机号至少填写一个'))
    return
  }
  callback()
}

function validatePhone(_rule: unknown, value: string, callback: (error?: Error) => void) {
  const phone = (value || '').trim()
  const email = form.email.trim()
  if (phone && !phonePattern.test(phone)) {
    callback(new Error('手机号格式不正确'))
    return
  }
  if (!phone && !email) {
    callback(new Error('邮箱和手机号至少填写一个'))
    return
  }
  callback()
}

function validatePassword(_rule: unknown, value: string, callback: (error?: Error) => void) {
  if (editMode.value === 'edit') {
    callback()
    return
  }
  const password = (value || '').trim()
  if (!password) {
    callback(new Error('密码不能为空'))
    return
  }
  if (password.length < 6 || password.length > 64) {
    callback(new Error('密码长度需在6-64之间'))
    return
  }
  callback()
}

const formRules: FormRules<typeof form> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 100, message: '用户名长度需在3-100之间', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 100, message: '昵称长度不能超过100', trigger: 'blur' }
  ],
  email: [{ validator: validateEmail, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  status: [{ required: true, message: '请选择账号状态', trigger: 'change' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  memberPlanCode: [{ required: true, message: '请选择会员方案', trigger: 'change' }]
}

const statusLabelMap: Record<UserStatus, string> = {
  active: '正常',
  inactive: '未激活',
  disabled: '冻结',
  deleted: '已删除'
}

const roleLabelMap: Record<UserRole, string> = {
  user: '普通用户',
  support: '客服',
  operator: '运营',
  admin: '管理员'
}

const memberPlanLabelMap: Record<MemberPlanCode, string> = {
  free: '免费版',
  pro: '专业版',
  pro_team: '团队版',
  enterprise: '企业版'
}

const stats = computed(() => ({
  pageTotal: list.value.length,
  active: list.value.filter((item) => item.status === 'active').length,
  inactive: list.value.filter((item) => item.status === 'inactive').length,
  disabled: list.value.filter((item) => item.status === 'disabled').length,
  verified: list.value.filter((item) => item.emailVerified || item.phoneVerified).length
}))

function formatTime(value?: string) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}:${s}`
}

function getDisplayName(row: UserRecord) {
  return row.nickname || row.username || row.email || row.phone || row.userId
}

function getStatusTag(status?: UserStatus) {
  if (status === 'active') return 'success'
  if (status === 'inactive') return 'warning'
  if (status === 'disabled' || status === 'deleted') return 'danger'
  return 'info'
}

async function loadUsers() {
  loading.value = true
  try {
    const result = await fetchUsers({
      keyword: filters.keyword.trim() || undefined,
      status: (filters.status || undefined) as UserStatus | undefined,
      role: (filters.role || undefined) as UserRole | undefined,
      memberPlanCode: (filters.memberPlanCode || undefined) as MemberPlanCode | undefined,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    list.value = Array.isArray(result.items) ? result.items : []
    total.value = Number(result.total || 0)
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '获取用户列表失败'))
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  currentPage.value = 1
  await loadUsers()
}

async function handleResetFilter() {
  filters.keyword = ''
  filters.status = ''
  filters.role = ''
  filters.memberPlanCode = ''
  currentPage.value = 1
  await loadUsers()
}

function handleSelectionChange(rows: UserRecord[]) {
  selectedRows.value = rows
}

async function handlePageChange(page: number) {
  currentPage.value = page
  await loadUsers()
}

async function handlePageSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  await loadUsers()
}

async function openDetailDialog(row: UserRecord) {
  if (!row.userId) return
  detailVisible.value = true
  detailLoading.value = true
  try {
    detailRecord.value = await fetchUserDetail(row.userId)
  } catch (error) {
    detailRecord.value = null
    ElMessage.error(getErrorMessage(error, '获取用户详情失败'))
  } finally {
    detailLoading.value = false
  }
}

// 打开会员变更日志弹窗
async function openAuditLogDialog(userId: string) {
  if (!userId) return
  currentAuditUserId.value = userId
  auditLogVisible.value = true
  auditLogLoading.value = true
  try {
    auditLogList.value = await fetchSubscriptionAuditLogs(userId)
  } catch (error) {
    auditLogList.value = []
    ElMessage.error(getErrorMessage(error, '获取会员变更记录失败'))
  } finally {
    auditLogLoading.value = false
  }
}

// 获取审计日志类型描述
function getAuditLogTypeDesc(type?: string) {
  const typeMap: Record<string, string> = {
    'create': '开通会员',
    'upgrade': '升级',
    'downgrade': '降级',
    'renew': '续费',
    'cancel': '取消',
    'expire': '过期',
    'adjust': '手动调整'
  }
  return typeMap[type || ''] || type || '未知'
}

// 获取审计日志类型标签
function getAuditLogTypeTag(type?: string) {
  switch (type) {
    case 'create':
    case 'upgrade':
    case 'renew':
      return 'success'
    case 'downgrade':
    case 'cancel':
    case 'expire':
      return 'warning'
    case 'adjust':
      return 'primary'
    default:
      return 'info'
  }
}

function resetFormData() {
  form.userId = ''
  form.username = ''
  form.nickname = ''
  form.email = ''
  form.phone = ''
  form.password = ''
  form.status = 'active'
  form.role = 'user'
  form.memberPlanCode = 'free'
  form.memberExpireAt = ''
}

async function openEditDialog(row?: UserRecord) {
  if (row) {
    editMode.value = 'edit'
    form.userId = row.userId
    form.username = row.username || ''
    form.nickname = row.nickname || ''
    form.email = row.email || ''
    form.phone = row.phone || ''
    form.password = ''
    form.status = (row.status || 'active') as UserStatus
    form.role = (row.role || 'user') as UserRole
    form.memberPlanCode = (row.memberPlanCode || 'free') as MemberPlanCode
    form.memberExpireAt = row.memberExpireAt || ''
  } else {
    editMode.value = 'create'
    resetFormData()
  }
  editVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function handleSaveUser() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = {
      username: form.username.trim(),
      nickname: form.nickname.trim(),
      email: form.email.trim() || undefined,
      phone: form.phone.trim() || undefined,
      status: form.status,
      role: form.role,
      memberPlanCode: form.memberPlanCode,
      memberExpireAt: form.memberExpireAt || undefined
    }

    if (editMode.value === 'create') {
      await createUser({
        ...payload,
        password: form.password.trim()
      })
      ElMessage.success('用户创建成功')
    } else {
      await updateUser(form.userId, payload)
      ElMessage.success('用户信息已更新')
    }

    editVisible.value = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '保存失败'))
  } finally {
    saving.value = false
  }
}

async function handleStatusChange(row: UserRecord, status: UserStatus) {
  if (!row.userId || !status) return
  if (row.status === status) return

  try {
    const result = await updateUserStatus(row.userId, status)
    row.status = (result.status || status) as UserStatus
    if (detailRecord.value?.userId === row.userId) {
      detailRecord.value.status = row.status
    }
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '状态更新失败'))
  }
}

function handleRowStatusSelect(row: UserRecord, value: string | number | boolean) {
  void handleStatusChange(row, String(value) as UserStatus)
}

async function handleBatchFreeze() {
  const ids = selectedRows.value.map((item) => item.userId).filter(Boolean)
  if (!ids.length) {
    ElMessage.warning('请先选择要冻结的用户')
    return
  }

  try {
    await ElMessageBox.confirm(`确认冻结选中的 ${ids.length} 个账号吗？`, '批量冻结确认', { type: 'warning' })
    await updateBatchUserStatus({
      userIds: ids,
      status: 'disabled'
    })
    ElMessage.success('批量冻结成功')
    selectedRows.value = []
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(getErrorMessage(error, '批量冻结失败'))
    }
  }
}

async function handleResetPassword(row: UserRecord) {
  if (!row.userId) return
  try {
    await ElMessageBox.confirm(`确认重置账号 ${getDisplayName(row)} 的密码吗？`, '重置密码确认', { type: 'warning' })
    const result = await resetUserPassword(row.userId)
    ElMessage.success(`密码已重置为 ${result.resetTo}`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(getErrorMessage(error, '重置密码失败'))
    }
  }
}

onMounted(async () => {
  await loadUsers()
})
</script>

<template>
  <el-card class="page-container" shadow="hover">
    <div class="header">
      <div>
        <div class="title">用户管理</div>
        <div class="subtitle">账号管理、角色配置、会员方案维护</div>
      </div>
      <el-tag type="danger">P0</el-tag>
    </div>

    <div class="stats-card">
      <div class="stat-item">
        <div class="stat-label">总用户数</div>
        <div class="stat-value">{{ total }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">当前页用户</div>
        <div class="stat-value">{{ stats.pageTotal }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">正常</div>
        <div class="stat-value success">{{ stats.active }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">未激活</div>
        <div class="stat-value warning">{{ stats.inactive }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">冻结</div>
        <div class="stat-value danger">{{ stats.disabled }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">已验证</div>
        <div class="stat-value primary">{{ stats.verified }}</div>
      </div>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索用户ID/用户名/昵称/邮箱/手机号"
          clearable
          style="width: 280px"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="filters.status" placeholder="账号状态" clearable style="width: 140px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="filters.role" placeholder="角色" clearable style="width: 140px">
          <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="filters.memberPlanCode" placeholder="会员方案" clearable style="width: 140px">
          <el-option v-for="item in memberPlanOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleResetFilter">重置</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="openEditDialog()">新增账号</el-button>
        <el-button type="warning" :disabled="selectedRows.length === 0" @click="handleBatchFreeze">批量冻结</el-button>
      </div>
    </div>

    <el-table :data="list" v-loading="loading" border stripe empty-text="暂无用户数据" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="56" align="center" />
      <el-table-column prop="userId" label="用户ID" min-width="180" show-overflow-tooltip />
      <el-table-column label="用户信息" min-width="220">
        <template #default="{ row }">
          <div class="user-main">{{ getDisplayName(row) }}</div>
          <div class="user-sub">用户名：{{ row.username || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="220" show-overflow-tooltip />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="角色" width="120" align="center">
        <template #default="{ row }">{{ roleLabelMap[(row.role || 'user') as UserRole] }}</template>
      </el-table-column>
      <el-table-column label="会员方案" width="120" align="center">
        <template #default="{ row }">{{ memberPlanLabelMap[(row.memberPlanCode || 'free') as MemberPlanCode] }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.status as UserStatus)">
            {{ statusLabelMap[(row.status || 'inactive') as UserStatus] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态变更" width="140" align="center">
        <template #default="{ row }">
          <el-select
            :model-value="row.status || 'inactive'"
            size="small"
            style="width: 120px"
            @change="handleRowStatusSelect(row, $event)"
          >
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="最近登录" width="170" align="center">
        <template #default="{ row }">{{ formatTime(row.lastLoginAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="300" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="openDetailDialog(row)">详情</el-button>
          <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="info" @click="openAuditLogDialog(row.userId)">会员日志</el-button>
          <el-button size="small" type="warning" @click="handleResetPassword(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @current-change="handlePageChange"
        @size-change="handlePageSizeChange"
      />
    </div>

    <el-dialog v-model="detailVisible" title="用户详情" width="680px">
      <el-skeleton :rows="8" animated v-if="detailLoading" />
      <el-descriptions v-else :column="1" border>
        <el-descriptions-item label="用户ID">{{ detailRecord?.userId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailRecord?.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detailRecord?.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailRecord?.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailRecord?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          {{ roleLabelMap[(detailRecord?.role || 'user') as UserRole] }}
        </el-descriptions-item>
        <el-descriptions-item label="会员方案">
          {{ memberPlanLabelMap[(detailRecord?.memberPlanCode || 'free') as MemberPlanCode] }}
        </el-descriptions-item>
        <el-descriptions-item label="会员到期时间">{{ formatTime(detailRecord?.memberExpireAt) }}</el-descriptions-item>
        <el-descriptions-item label="账号状态">
          {{ statusLabelMap[(detailRecord?.status || 'inactive') as UserStatus] }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱已验证">{{ detailRecord?.emailVerified ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="手机号已验证">{{ detailRecord?.phoneVerified ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="最后登录时间">{{ formatTime(detailRecord?.lastLoginAt) }}</el-descriptions-item>
        <el-descriptions-item label="最后登录IP">{{ detailRecord?.lastLoginIp || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(detailRecord?.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(detailRecord?.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 会员变更日志弹窗 -->
    <el-dialog v-model="auditLogVisible" title="会员变更记录" width="800px">
      <div class="audit-log-toolbar mb-10">
        <span class="audit-user-info">用户ID: {{ currentAuditUserId }}</span>
      </div>
      <el-table
        :data="auditLogList"
        v-loading="auditLogLoading"
        border
        stripe
        style="width: 100%"
        :empty-text="auditLogList.length === 0 ? '暂无会员变更记录' : ''"
      >
        <el-table-column prop="createdAt" label="变更时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="operateType" label="变更类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getAuditLogTypeTag(row.operateType)">
              {{ getAuditLogTypeDesc(row.operateType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="beforePlanCode" label="变更前方案" width="120" align="center">
          <template #default="{ row }">
            {{ row.beforePlanCode || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="afterPlanCode" label="变更后方案" width="120" align="center">
          <template #default="{ row }">
            {{ row.afterPlanCode || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="beforeEndDate" label="变更前到期日" width="140" align="center">
          <template #default="{ row }">
            {{ row.beforeEndDate ? formatTime(row.beforeEndDate) : '永久' }}
          </template>
        </el-table-column>
        <el-table-column prop="afterEndDate" label="变更后到期日" width="140" align="center">
          <template #default="{ row }">
            {{ row.afterEndDate ? formatTime(row.afterEndDate) : '永久' }}
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" align="center" />
        <el-table-column prop="reason" label="变更原因" min-width="150" />
      </el-table>
      <template #footer>
        <el-button @click="auditLogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新增账号' : '编辑账号'" width="640px" center>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="3-100位，唯一" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="邮箱和手机号至少填写一个" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="11位大陆手机号" />
        </el-form-item>
        <el-form-item v-if="editMode === 'create'" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="会员方案" prop="memberPlanCode">
          <el-select v-model="form.memberPlanCode" style="width: 100%">
            <el-option v-for="item in memberPlanOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="会员到期时间">
          <el-date-picker
            v-model="form.memberExpireAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="可选"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="账号状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="active">正常</el-radio>
            <el-radio label="inactive">未激活</el-radio>
            <el-radio label="disabled">冻结</el-radio>
            <el-radio label="deleted">已删除</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveUser">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.page-container {
  padding: 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.subtitle {
  margin-top: 4px;
  font-size: 14px;
  color: #6b7280;
}

.stats-card {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
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

.stat-value.success {
  color: #10b981;
}

.stat-value.warning {
  color: #f59e0b;
}

.stat-value.danger {
  color: #ef4444;
}

.stat-value.primary {
  color: #3b82f6;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 20px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.user-main {
  font-weight: 600;
  color: #111827;
}

.user-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #6b7280;
}

@media (max-width: 1280px) {
  .stats-card {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .stats-card {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

/* 会员变更日志样式 */
.audit-log-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.audit-user-info {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}
</style>
