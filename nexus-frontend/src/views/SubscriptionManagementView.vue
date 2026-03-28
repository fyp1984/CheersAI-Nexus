<template>
  <el-card class="page-container" shadow="hover">
    <!-- 顶部标题区 -->
    <div class="header">
      <div class="header-left">
        <div class="title">订阅管理</div>
        <div class="subtitle">管理用户订阅、续费及状态变更</div>
      </div>
      <el-tag type="warning" size="large">P1</el-tag>
    </div>

    <!-- 数据概览卡片 -->
    <div class="stats-card mb-20">
      <div class="stat-item">
        <div class="stat-label">总订阅数</div>
        <div class="stat-value">{{ list.length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">活跃中</div>
        <div class="stat-value success">{{ list.filter(item => item.status === 'active').length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">已过期</div>
        <div class="stat-value warning">{{ list.filter(item => item.status === 'expired').length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">已取消</div>
        <div class="stat-value danger">{{ list.filter(item => item.status === 'cancelled').length }}</div>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchUserId"
          placeholder="搜索用户ID"
          clearable
          style="width: 200px"
          class="mr-10"
          prefix-icon="el-icon-search"
        />

        <el-input
          v-model="searchPlan"
          placeholder="搜索方案名称"
          clearable
          style="width: 200px"
          class="mr-10"
          prefix-icon="el-icon-search"
        />

        <el-select v-model="statusFilter" placeholder="订阅状态" style="width: 140px" class="mr-10">
          <el-option label="全部" value="" />
          <el-option label="活跃" value="active" />
          <el-option label="已过期" value="expired" />
          <el-option label="已取消" value="cancelled" />
        </el-select>

        <el-select v-model="renewalFilter" placeholder="续费状态" style="width: 140px" class="mr-10">
          <el-option label="全部" value="" />
          <el-option label="自动续费" value="true" />
          <el-option label="手动续费" value="false" />
        </el-select>
      </div>

      <div class="toolbar-right">
        <el-button type="primary" @click="openCreateDialog()" icon="el-icon-plus">新建订阅</el-button>
        <el-button type="info" @click="refreshList()" icon="el-icon-refresh" class="ml-10">刷新</el-button>
        <el-button class="ml-10 mr-5" icon="el-icon-s-tools" @click="openAdvancedFilter">高级筛选</el-button>
        <el-button type="warning" icon="el-icon-document" @click="exportData">导出数据</el-button>
      </div>
    </div>

    <!-- 表格 -->
    <el-table
      :data="paginatedList"
      v-loading="loading"
      @selection-change="handleSelectionChange"
      border
      stripe
      :empty-text="paginatedList.length === 0 ? '暂无匹配的订阅记录' : ''"
      style="width: 100%"
    >
      <el-table-column type="selection" width="60" align="center" />
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="userId" label="用户ID" width="120" align="center" />
      <el-table-column prop="planName" label="订阅方案" min-width="140" />
      <el-table-column prop="planCode" label="方案编码" width="120" align="center" />
      <el-table-column label="订阅状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="自动续费" width="100" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.autoRenew"
            @change="handleAutoRenewChange(scope.row)"
            active-text="是"
            inactive-text="否"
          />
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="开始时间" width="180" align="center">
        <template #default="scope">
          {{ formatDateTime(scope.row.startDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="endDate" label="到期时间" width="180" align="center">
        <template #default="scope">
          {{ formatDateTime(scope.row.endDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="paymentMethod" label="支付方式" width="120" align="center">
        <template #default="scope">
          {{ getPaymentMethodText(scope.row.paymentMethod) }}
        </template>
      </el-table-column>
      <el-table-column prop="lastPaymentAt" label="最后付费" width="180" align="center">
        <template #default="scope">
          {{ formatDateTime(scope.row.lastPaymentAt) }}
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" icon="el-icon-view" @click="openDetailDialog(scope.row)">
            详情
          </el-button>
          <el-button size="small" type="warning" icon="el-icon-edit" @click="openEditDialog(scope.row)">
            编辑
          </el-button>
          <el-dropdown @command="(cmd: string) => handleAction(cmd, scope.row)">
            <el-button size="small" type="text" icon="el-icon-more">更多</el-button>
            <template #dropdown>
              <el-dropdown-item command="renew" icon="el-icon-refresh">续费</el-dropdown-item>
              <el-dropdown-item command="cancel" icon="el-icon-close">取消订阅</el-dropdown-item>
              <el-dropdown-item
                command="delete"
                icon="el-icon-delete"
                divided
                style="color: #f56c6c"
              >
                删除
              </el-dropdown-item>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页器 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredList.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新建订阅弹窗 -->
    <el-dialog v-model="createDialogVisible" title="新建订阅" width="600px" center>
      <el-form :model="createForm" label-width="80px" :rules="createRules" ref="createFormRef">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="createForm.userId" placeholder="请输入用户ID" />
        </el-form-item>

        <el-form-item label="订阅方案" prop="planCode">
          <el-select v-model="createForm.planCode" placeholder="请选择订阅方案" style="width: 100%">
            <el-option
              v-for="plan in planOptions"
              :key="plan.code"
              :label="plan.name"
              :value="plan.code"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="开始时间" prop="startDate">
          <el-date-picker
            v-model="createForm.startDate"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endDate">
          <el-date-picker
            v-model="createForm.endDate"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="自动续费">
          <el-switch v-model="createForm.autoRenew" active-text="是" inactive-text="否" />
        </el-form-item>

        <el-form-item label="支付方式">
          <el-select v-model="createForm.paymentMethod" placeholder="请选择支付方式" style="width: 100%">
            <el-option label="支付宝" value="alipay" />
            <el-option label="微信支付" value="wechat" />
            <el-option label="银行卡" value="bank_card" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="saving">创建</el-button>
      </template>
    </el-dialog>

    <!-- 订阅详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="订阅详情" width="700px" center>
      <el-descriptions :column="2" border v-if="currentSubscription.id">
        <el-descriptions-item label="订阅ID">{{ currentSubscription.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentSubscription.userId }}</el-descriptions-item>
        <el-descriptions-item label="订阅方案">{{ currentSubscription.planName }}</el-descriptions-item>
        <el-descriptions-item label="方案编码">{{ currentSubscription.planCode }}</el-descriptions-item>
        <el-descriptions-item label="订阅状态">
          <el-tag :type="getStatusType(currentSubscription.status)">
            {{ getStatusText(currentSubscription.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="自动续费">
          <el-tag :type="currentSubscription.autoRenew ? 'success' : 'info'">
            {{ currentSubscription.autoRenew ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(currentSubscription.startDate) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(currentSubscription.endDate) }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ getPaymentMethodText(currentSubscription.paymentMethod) }}</el-descriptions-item>
        <el-descriptions-item label="最后付费">{{ formatDateTime(currentSubscription.lastPaymentAt) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentSubscription.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(currentSubscription.updatedAt) }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="openEditDialog(currentSubscription)">编辑</el-button>
      </template>
    </el-dialog>

    <!-- 编辑订阅弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑订阅" width="600px" center>
      <el-form :model="editForm" label-width="80px" :rules="editRules" ref="editFormRef">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="editForm.userId" placeholder="请输入用户ID" />
        </el-form-item>

        <el-form-item label="订阅方案" prop="planCode">
          <el-select v-model="editForm.planCode" placeholder="请选择订阅方案" style="width: 100%">
            <el-option
              v-for="plan in planOptions"
              :key="plan.code"
              :label="plan.name"
              :value="plan.code"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="开始时间" prop="startDate">
          <el-date-picker
            v-model="editForm.startDate"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endDate">
          <el-date-picker
            v-model="editForm.endDate"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="自动续费">
          <el-switch v-model="editForm.autoRenew" active-text="是" inactive-text="否" />
        </el-form-item>

        <el-form-item label="支付方式">
          <el-select v-model="editForm.paymentMethod" placeholder="请选择支付方式" style="width: 100%">
            <el-option label="支付宝" value="alipay" />
            <el-option label="微信支付" value="wechat" />
            <el-option label="银行卡" value="bank_card" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 高级筛选 -->
    <el-drawer v-model="advancedVisible" title="高级筛选" size="500px">
      <div class="advanced-filter-content">
        <el-form :model="advancedForm" label-width="100px">
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="advancedForm.createTime"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="到期时间">
            <el-date-picker
              v-model="advancedForm.expireTime"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="支付方式">
            <el-select v-model="advancedForm.paymentMethod" placeholder="请选择支付方式" multiple>
              <el-option label="支付宝" value="alipay" />
              <el-option label="微信支付" value="wechat" />
              <el-option label="银行卡" value="bank_card" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="filter-actions" style="margin-top: 20px; text-align: right">
          <el-button @click="resetAdvancedFilter">重置</el-button>
          <el-button type="primary" class="ml-10" @click="applyAdvancedFilter">应用筛选</el-button>
        </div>
      </div>
    </el-drawer>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as membershipApi from '../api/membership'
import * as planApi from '../api/membership'
import type { 
  SubscriptionDetailDTO, 
  SubscriptionCreateDTO,
  PlanDetailDTO 
} from '../api/membership'

// ========== API 接口说明 ==========
// 订阅管理相关 API（已接入）：
// - GET    /api/v1/subscriptions              - fetchSubscriptionList()           - 获取订阅列表
// - GET    /api/v1/subscriptions/{id}         - fetchSubscriptionDetail()        - 获取订阅详情  
// - POST   /api/v1/subscriptions              - createSubscription()             - 创建订阅
// - PUT    /api/v1/subscriptions/{id}         - updateSubscription()             - 更新订阅
//
// 会员计划相关 API（已接入）：
// - GET    /api/v1/plans                      - fetchPlanList()                  - 获取会员计划列表（用于方案选择）

// loading 状态
const loading = ref(false)
const saving = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 查询条件
const searchUserId = ref('')
const searchPlan = ref('')
const statusFilter = ref('')
const renewalFilter = ref('')

// 选中行
const selectedRows = ref([])

// 订阅列表数据 - API: GET /api/v1/subscriptions
const list = ref<SubscriptionDetailDTO[]>([])

// 会员方案选项 - API: GET /api/v1/plans
const planOptions = ref<PlanDetailDTO[]>([])

// 当前操作的订阅
const currentSubscription = ref<SubscriptionDetailDTO>({} as SubscriptionDetailDTO)

// 弹窗控制
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const advancedVisible = ref(false)

// 表单ref
const createFormRef = ref<any>(null)
const editFormRef = ref<any>(null)

// 新建订阅表单
const createForm = reactive({
  userId: '',
  planCode: '',
  startDate: '',
  endDate: '',
  autoRenew: false,
  paymentMethod: ''
})

// 编辑订阅表单
const editForm = reactive({
  userId: '',
  planCode: '',
  startDate: '',
  endDate: '',
  autoRenew: false,
  paymentMethod: ''
})

// 高级筛选表单
const advancedForm = reactive({
  createTime: [],
  expireTime: [],
  paymentMethod: [] as string[]
})

// 表单验证规则
const createRules = reactive({
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  planCode: [{ required: true, message: '请选择订阅方案', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始时间', trigger: 'blur' }],
  endDate: [{ required: true, message: '请选择结束时间', trigger: 'blur' }]
})

const editRules = reactive({
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  planCode: [{ required: true, message: '请选择订阅方案', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始时间', trigger: 'blur' }],
  endDate: [{ required: true, message: '请选择结束时间', trigger: 'blur' }]
})

// 加载订阅列表 - API: GET /api/v1/subscriptions
const loadSubscriptionList = async () => {
  loading.value = true
  try {
    // API: fetchSubscriptionList() -> GET /api/v1/subscriptions
    const data = await membershipApi.fetchSubscriptionList({
      userId: searchUserId.value || undefined,
      status: statusFilter.value || undefined
    })
    list.value = data
  } catch (error: any) {
    ElMessage.error(error.message || '加载订阅列表失败')
  } finally {
    loading.value = false
  }
}

// 加载会员方案列表 - API: GET /api/v1/plans
const loadPlanOptions = async () => {
  try {
    // API: fetchPlanList() -> GET /api/v1/plans
    const data = await planApi.fetchPlanList()
    planOptions.value = data.filter(plan => plan.status === 'active')
  } catch (error: any) {
    ElMessage.error(error.message || '加载会员方案失败')
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadSubscriptionList()
  loadPlanOptions()
})

// 过滤后的列表
const filteredList = computed(() => {
  return list.value.filter(item => {
    return (
      (!searchUserId.value || item.userId.includes(searchUserId.value)) &&
      (!searchPlan.value || item.planName.includes(searchPlan.value)) &&
      (!statusFilter.value || item.status === statusFilter.value) &&
      (renewalFilter.value === '' || item.autoRenew.toString() === renewalFilter.value)
    )
  })
})

// 分页后的列表
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

// 处理分页大小改变
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

// 处理页码改变
const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

// 批量选择
const handleSelectionChange = (rows: any) => {
  selectedRows.value = rows
}

// 打开新建订阅弹窗
const openCreateDialog = () => {
  Object.assign(createForm, {
    userId: '',
    planCode: '',
    startDate: '',
    endDate: '',
    autoRenew: false,
    paymentMethod: ''
  })
  createDialogVisible.value = true
}

// 打开订阅详情弹窗 - API: GET /api/v1/subscriptions/{id}
const openDetailDialog = async (row: SubscriptionDetailDTO) => {
  try {
    // API: fetchSubscriptionDetail() -> GET /api/v1/subscriptions/{id}
    const detail = await membershipApi.fetchSubscriptionDetail(row.id)
    currentSubscription.value = detail
    detailDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取订阅详情失败')
  }
}

// 打开编辑订阅弹窗 - API: GET /api/v1/subscriptions/{id}
const openEditDialog = async (row: SubscriptionDetailDTO) => {
  try {
    // API: fetchSubscriptionDetail() -> GET /api/v1/subscriptions/{id}
    const detail = await membershipApi.fetchSubscriptionDetail(row.id)
    Object.assign(editForm, {
      userId: detail.userId,
      planCode: detail.planCode,
      startDate: detail.startDate,
      endDate: detail.endDate,
      autoRenew: detail.autoRenew,
      paymentMethod: detail.paymentMethod || ''
    })
    currentSubscription.value = detail
    editDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取订阅详情失败')
  }
}

// 创建订阅 - API: POST /api/v1/subscriptions
const handleCreate = async () => {
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    saving.value = true
    
    // API: createSubscription() -> POST /api/v1/subscriptions
    // 后端期望 LocalDate 格式 (yyyy-MM-dd)
    const createData: SubscriptionCreateDTO = {
      userId: createForm.userId,
      planCode: createForm.planCode,
      startDate: formatDateForApi(createForm.startDate),
      endDate: formatDateForApi(createForm.endDate),
      autoRenew: createForm.autoRenew,
      paymentMethod: createForm.paymentMethod
    }
    
    await membershipApi.createSubscription(createData)
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    await loadSubscriptionList()
  } catch (error: any) {
    ElMessage.error(error.message || '创建失败')
  } finally {
    saving.value = false
  }
}

// 更新订阅 - API: PUT /api/v1/subscriptions/{id}
const handleUpdate = async () => {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    saving.value = true
    
    // API: updateSubscription() -> PUT /api/v1/subscriptions/{id}
    // 后端期望 LocalDate 格式 (yyyy-MM-dd)
    const updateData: SubscriptionCreateDTO = {
      userId: editForm.userId,
      planCode: editForm.planCode,
      startDate: formatDateForApi(editForm.startDate),
      endDate: formatDateForApi(editForm.endDate),
      autoRenew: editForm.autoRenew,
      paymentMethod: editForm.paymentMethod
    }
    
    await membershipApi.updateSubscription(currentSubscription.value.id, updateData)
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    await loadSubscriptionList()
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    saving.value = false
  }
}

// 自动续费状态变更 - API: PUT /api/v1/subscriptions/{id}
const handleAutoRenewChange = async (row: SubscriptionDetailDTO) => {
  try {
    // API: updateSubscription() -> PUT /api/v1/subscriptions/{id}
    // 后端期望 LocalDate 格式 (yyyy-MM-dd)
    await membershipApi.updateSubscription(row.id, {
      userId: row.userId,
      planCode: row.planCode,
      startDate: formatDateForApi(row.startDate),
      endDate: formatDateForApi(row.endDate),
      autoRenew: row.autoRenew,
      paymentMethod: row.paymentMethod
    })
    ElMessage.success(`自动续费已${row.autoRenew ? '开启' : '关闭'}`)
  } catch (error: any) {
    // 恢复原状态
    row.autoRenew = !row.autoRenew
    ElMessage.error(error.message || '状态更新失败')
  }
}

// 删除订阅
const handleDelete = async (_row: SubscriptionDetailDTO) => {
  try {
    await ElMessageBox.confirm(
      '确认删除该订阅吗？删除后将无法恢复',
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    // 注意：后端没有提供删除订阅的接口，这里需要根据实际需求添加
    ElMessage.warning('删除订阅功能待后端接口支持')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  } finally {
    loading.value = false
  }
}

// 下拉菜单操作处理
const handleAction = (cmd: string, row: SubscriptionDetailDTO) => {
  if (cmd === 'renew') {
    ElMessage.info('续费功能待实现')
  } else if (cmd === 'cancel') {
    ElMessage.info('取消订阅功能待实现')
  } else if (cmd === 'delete') {
    handleDelete(row)
  }
}

// 刷新列表
const refreshList = () => {
  loadSubscriptionList()
}

// 导出数据
const exportData = () => {
  ElMessage.info('导出功能待实现')
}

// 高级筛选
const openAdvancedFilter = () => {
  advancedVisible.value = true
}

// 重置高级筛选
const resetAdvancedFilter = () => {
  advancedForm.createTime = []
  advancedForm.expireTime = []
  advancedForm.paymentMethod = []
}

// 应用高级筛选
const applyAdvancedFilter = () => {
  // TODO: 实现高级筛选逻辑
  advancedVisible.value = false
  ElMessage.success('筛选已应用')
}

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'active': return 'success'
    case 'expired': return 'warning'
    case 'cancelled': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'active': return '活跃'
    case 'expired': return '已过期'
    case 'cancelled': return '已取消'
    default: return status
  }
}

// 获取支付方式文本
const getPaymentMethodText = (method?: string) => {
  switch (method) {
    case 'alipay': return '支付宝'
    case 'wechat': return '微信支付'
    case 'bank_card': return '银行卡'
    case 'other': return '其他'
    default: return '-'
  }
}

// 格式化日期时间
const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).replace(/\//g, '-')
}

// 格式化日期为 LocalDate 格式 (yyyy-MM-dd)，供后端API使用
const formatDateForApi = (date: string) => {
  if (!date) return ''
  // 如果已经是 yyyy-MM-dd 格式，直接返回
  if (/^\d{4}-\d{2}-\d{2}$/.test(date)) return date
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 监听搜索和过滤，重置页码
watch([searchUserId, searchPlan, statusFilter, renewalFilter], () => {
  currentPage.value = 1
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

/* 头部样式 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 4px;
}

.subtitle {
  font-size: 14px;
  color: #6b7280;
}

/* 数据概览卡片 */
.stats-card {
  display: flex;
  gap: 24px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
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

/* 工具栏样式 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f9fafb;
  border-radius: 8px;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

/* 高级筛选样式 */
.advanced-filter-content {
  padding: 16px;
}

/* 通用间距类 */
.mr-5 {
  margin-right: 5px;
}

.mr-10 {
  margin-right: 10px;
}

.ml-5 {
  margin-left: 5px;
}

.ml-10 {
  margin-left: 10px;
}

.mb-10 {
  margin-bottom: 10px;
}

.mb-20 {
  margin-bottom: 20px;
}

.mt-10 {
  margin-top: 10px;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* 表格样式优化 */
:deep(.el-table) {
  --el-table-header-text-color: #6b7280;
  --el-table-row-hover-bg-color: #f9fafb;
}

:deep(.el-table th) {
  background-color: #f9fafb;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-size: 11px;
}

/* 开关样式优化 */
:deep(.el-switch) {
  --el-switch-on-color: #10b981;
  --el-switch-off-color: #94a3b8;
}

/* 抽屉样式优化 */
:deep(.el-drawer__body) {
  padding: 20px;
}
</style>
