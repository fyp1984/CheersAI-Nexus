<template>
  <el-card class="page-container" shadow="hover">
    <div class="header">
      <div class="header-left">
        <div class="title">用户管理</div>
        <div class="subtitle">账号查询、状态管理、角色与会员关联配置</div>
      </div>
      <el-tag type="danger" size="large">P0</el-tag>
    </div>

    <div class="stats-card mb-20">
      <div class="stat-item">
        <div class="stat-label">用户总数</div>
        <div class="stat-value">{{ list.length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">正常用户</div>
        <div class="stat-value success">{{ list.filter((item) => item.status === 'active').length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">冻结用户</div>
        <div class="stat-value warning">{{ list.filter((item) => item.status === 'frozen').length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">管理员</div>
        <div class="stat-value primary">{{ list.filter((item) => item.role !== 'user').length }}</div>
      </div>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户ID/昵称/账号/邮箱"
          clearable
          style="width: 280px"
          class="mr-10"
        />
        <el-select v-model="statusFilter" placeholder="账号状态" style="width: 140px" class="mr-10">
          <el-option label="全部" value="" />
          <el-option label="正常" value="active" />
          <el-option label="冻结" value="frozen" />
          <el-option label="待激活" value="pending" />
        </el-select>
        <el-select v-model="roleFilter" placeholder="角色" style="width: 140px" class="mr-10">
          <el-option label="全部" value="" />
          <el-option label="普通用户" value="user" />
          <el-option label="客服" value="support" />
          <el-option label="运营" value="operator" />
          <el-option label="管理员" value="admin" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="openEditDialog()">新增账号</el-button>
        <el-button type="warning" class="ml-10" :disabled="selectedRows.length === 0" @click="batchFreeze">批量冻结</el-button>
      </div>
    </div>

    <el-table
      :data="paginatedList"
      v-loading="loading"
      border
      stripe
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="60" align="center" />
      <el-table-column prop="id" label="用户ID" width="110" align="center" />
      <el-table-column prop="nickname" label="昵称" width="130" />
      <el-table-column prop="account" label="账号" min-width="160" />
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column prop="role" label="角色" width="120" align="center">
        <template #default="scope">
          <el-tag :type="getRoleTag(scope.row.role)">{{ roleTextMap[scope.row.role] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="memberPlan" label="会员方案" width="130" align="center" />
      <el-table-column prop="lastLoginTime" label="最近登录" width="170" align="center" />
      <el-table-column prop="status" label="账号状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="getStatusTag(scope.row.status)">{{ statusTextMap[scope.row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" align="center">
        <template #default="scope">
          <el-button size="small" @click="openDetailDialog(scope.row)">详情</el-button>
          <el-button size="small" type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
          <el-dropdown @command="handleDropdownCommand($event, scope.row)">
            <el-button size="small" type="text">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="resetPwd">重置密码</el-dropdown-item>
                <el-dropdown-item command="toggleStatus">
                  {{ scope.row.status === 'active' ? '冻结账号' : '恢复账号' }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[8, 16, 24, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredList.length"
      />
    </div>

    <el-dialog v-model="detailVisible" title="用户详情" width="620px" center>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentRow.nickname }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ currentRow.account }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentRow.email }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentRow.mobile }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ roleTextMap[currentRow.role] }}</el-descriptions-item>
        <el-descriptions-item label="会员方案">{{ currentRow.memberPlan }}</el-descriptions-item>
        <el-descriptions-item label="账号状态">{{ statusTextMap[currentRow.status] }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="最近登录">{{ currentRow.lastLoginTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" :title="editForm.id ? '编辑用户' : '新增用户'" width="620px" center>
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="90px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="账号" prop="account">
          <el-input v-model="editForm.account" :disabled="!!editForm.id" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="editForm.mobile" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" placeholder="请选择角色">
            <el-option label="普通用户" value="user" />
            <el-option label="客服" value="support" />
            <el-option label="运营" value="operator" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="会员方案" prop="memberPlan">
          <el-select v-model="editForm.memberPlan" placeholder="请选择会员方案">
            <el-option label="免费版" value="免费版" />
            <el-option label="专业版" value="专业版" />
            <el-option label="团队版" value="团队版" />
            <el-option label="企业版" value="企业版" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio label="active">正常</el-radio>
            <el-radio label="frozen">冻结</el-radio>
            <el-radio label="pending">待激活</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveUser">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)
const searchKeyword = ref('')
const statusFilter = ref('')
const roleFilter = ref('')
const selectedRows = ref<any[]>([])

const statusTextMap: Record<string, string> = {
  active: '正常',
  frozen: '冻结',
  pending: '待激活'
}

const roleTextMap: Record<string, string> = {
  user: '普通用户',
  support: '客服',
  operator: '运营',
  admin: '管理员'
}

const list = ref([
  {
    id: 'U1001',
    nickname: '王小明',
    account: 'xiaoming',
    email: 'xiaoming@cheersai.com',
    mobile: '13800000001',
    role: 'admin',
    memberPlan: '企业版',
    status: 'active',
    createTime: '2026-03-12 10:23',
    lastLoginTime: '2026-03-18 16:30'
  },
  {
    id: 'U1002',
    nickname: '李四',
    account: 'lisi',
    email: 'lisi@cheersai.com',
    mobile: '13800000002',
    role: 'operator',
    memberPlan: '团队版',
    status: 'active',
    createTime: '2026-03-13 11:12',
    lastLoginTime: '2026-03-18 15:08'
  },
  {
    id: 'U1003',
    nickname: '客服小刘',
    account: 'support_liu',
    email: 'support@cheersai.com',
    mobile: '13800000003',
    role: 'support',
    memberPlan: '专业版',
    status: 'frozen',
    createTime: '2026-03-14 09:05',
    lastLoginTime: '2026-03-17 20:16'
  },
  {
    id: 'U1004',
    nickname: '测试用户A',
    account: 'demo_user_a',
    email: 'demo_a@example.com',
    mobile: '13800000004',
    role: 'user',
    memberPlan: '免费版',
    status: 'pending',
    createTime: '2026-03-15 08:40',
    lastLoginTime: '-'
  }
])

const filteredList = computed(() => {
  return list.value.filter((item) => {
    const keyword = searchKeyword.value.trim()
    const keywordMatch =
      !keyword ||
      item.id.includes(keyword) ||
      item.nickname.includes(keyword) ||
      item.account.includes(keyword) ||
      item.email.includes(keyword)

    const statusMatch = !statusFilter.value || item.status === statusFilter.value
    const roleMatch = !roleFilter.value || item.role === roleFilter.value

    return keywordMatch && statusMatch && roleMatch
  })
})

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

watch([searchKeyword, statusFilter, roleFilter], () => {
  currentPage.value = 1
})

function getStatusTag(status: string) {
  if (status === 'active') return 'success'
  if (status === 'frozen') return 'danger'
  return 'warning'
}

function getRoleTag(role: string) {
  if (role === 'admin') return 'danger'
  if (role === 'operator') return 'primary'
  if (role === 'support') return 'warning'
  return 'default'
}

function handleSelectionChange(rows: any[]) {
  selectedRows.value = rows
}

function batchFreeze() {
  selectedRows.value.forEach((item) => {
    item.status = 'frozen'
  })
  ElMessage.success(`已冻结 ${selectedRows.value.length} 个账号`)
  selectedRows.value = []
}

const detailVisible = ref(false)
const editVisible = ref(false)
const currentRow = ref<any>({})
const editFormRef = ref<any>(null)

const editForm = reactive({
  id: '',
  nickname: '',
  account: '',
  email: '',
  mobile: '',
  role: 'user',
  memberPlan: '免费版',
  status: 'active'
})

const editRules = reactive({
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

function openDetailDialog(row: any) {
  currentRow.value = row
  detailVisible.value = true
}

function openEditDialog(row?: any) {
  if (row) {
    Object.assign(editForm, row)
  } else {
    editForm.id = ''
    editForm.nickname = ''
    editForm.account = ''
    editForm.email = ''
    editForm.mobile = ''
    editForm.role = 'user'
    editForm.memberPlan = '免费版'
    editForm.status = 'active'
  }
  editVisible.value = true
}

function handleSaveUser() {
  editFormRef.value.validate((valid: boolean) => {
    if (!valid) return

    if (!editForm.id) {
      list.value.unshift({
        ...editForm,
        id: `U${Math.floor(Math.random() * 9000 + 1000)}`,
        createTime: new Date().toLocaleString().replace(/\//g, '-'),
        lastLoginTime: '-'
      })
      ElMessage.success('用户创建成功')
    } else {
      const index = list.value.findIndex((item) => item.id === editForm.id)
      if (index >= 0) {
        list.value[index] = {
          ...list.value[index],
          ...editForm
        }
      }
      ElMessage.success('用户信息已更新')
    }

    // TODO: 调用用户创建/编辑接口
    editVisible.value = false
  })
}

function handleAction(command: string, row: any) {
  if (command === 'resetPwd') {
    ElMessageBox.confirm(`确认重置账号 ${row.account} 的密码？`, '重置密码确认', { type: 'warning' }).then(() => {
      // TODO: 调用重置密码接口
      ElMessage.success('重置密码成功，临时密码已发送')
    })
    return
  }

  if (command === 'toggleStatus') {
    row.status = row.status === 'active' ? 'frozen' : 'active'
    // TODO: 调用账号状态更新接口
    ElMessage.success(row.status === 'active' ? '账号已恢复' : '账号已冻结')
  }
}

function handleDropdownCommand(command: string, row: any) {
  handleAction(command, row)
}
</script>

<style scoped>
.page-container {
  padding: 24px;
  margin: 16px;
}

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

.stats-card {
  display: flex;
  gap: 24px;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
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

.stat-value.success {
  color: #10b981;
}

.stat-value.warning {
  color: #f59e0b;
}

.stat-value.primary {
  color: #409eff;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f9fafb;
  border-radius: 8px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.mr-10 {
  margin-right: 10px;
}

.ml-10 {
  margin-left: 10px;
}

.mb-20 {
  margin-bottom: 20px;
}

:deep(.el-table) {
  --el-table-header-text-color: #4b5563;
  --el-table-row-hover-bg-color: #f9fafb;
}

:deep(.el-table th) {
  background-color: #f9fafb;
  font-weight: 500;
}
</style>
