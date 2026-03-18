<template>
  <el-card class="page-container" shadow="hover">
    <!-- 顶部标题区 -->
    <div class="header">
      <div class="header-left">
        <div class="title">会员方案管理</div>
        <div class="subtitle">管理会员等级、价格及权益配置</div>
      </div>
      <el-tag type="danger" size="large">P0 核心</el-tag>
    </div>

    <!-- 数据概览卡片 -->
    <div class="stats-card mb-20">
      <div class="stat-item">
        <div class="stat-label">总方案数</div>
        <div class="stat-value">{{ list.length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">启用中</div>
        <div class="stat-value success">{{ list.filter(item => item.status === 'enabled').length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">已禁用</div>
        <div class="stat-value warning">{{ list.filter(item => item.status === 'disabled').length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">待审批</div>
        <div class="stat-value primary">{{ list.filter(item => item.approveStatus === 'pending').length }}</div>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchName"
          placeholder="搜索会员方案"
          clearable
          style="width: 280px"
          class="mr-10"
          prefix-icon="el-icon-search"
        />

        <el-select v-model="statusFilter" placeholder="状态" style="width: 140px" class="mr-10">
          <el-option label="全部" value="" />
          <el-option label="启用" value="enabled" />
          <el-option label="禁用" value="disabled" />
        </el-select>

        <el-select v-model="approveFilter" placeholder="审批状态" style="width: 140px" class="mr-10">
          <el-option label="全部" value="" />
          <el-option label="待审批" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已驳回" value="rejected" />
        </el-select>
      </div>

      <div class="toolbar-right">
        <el-button type="primary" @click="openDialog()" icon="el-icon-plus">新建方案</el-button>
        <el-button type="info" @click="openUserAdjustDialog()" icon="el-icon-user" class="ml-10">调整用户会员</el-button>
        <el-button class="ml-10 mr-5" icon="el-icon-s-tools" @click="openAdvancedFilter">高级筛选</el-button>
        <el-button type="warning" icon="el-icon-document" @click="openLog">操作日志</el-button>
      </div>
    </div>

    <!-- 表格 -->
    <el-table
      :data="paginatedList"
      v-loading="loading"
      @selection-change="handleSelectionChange"
      border
      stripe
      :empty-text="paginatedList.length === 0 ? '暂无匹配的会员方案' : ''"
      style="width: 100%"
    >
      <el-table-column type="selection" width="60" align="center" />
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="name" label="方案名称" min-width="180" />
      <el-table-column prop="id" label="方案ID" width="120" align="center" />
      <el-table-column label="月付价格(元)" width="120" align="center">
        <template #default="scope">
          <span class="price-text">¥{{ scope.row.priceMonthly }}</span>
        </template>
      </el-table-column>
      <el-table-column label="年付价格(元)" width="120" align="center">
        <template #default="scope">
          <span class="price-text">¥{{ scope.row.priceYearly }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="duration" label="有效期(天)" width="120" align="center" />
      <el-table-column label="审批状态" width="140" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.approveStatus === 'pending'" type="warning">待审批</el-tag>
          <el-tag v-else-if="scope.row.approveStatus === 'approved'" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.approveStatus === 'rejected'" type="danger">已驳回</el-tag>
          <el-tag v-else type="info">无需审批</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="140" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="enabled"
            inactive-value="disabled"
            @change="handleStatusChange(scope.row)"
            active-text="启用"
            inactive-text="禁用"
            :disabled="scope.row.approveStatus !== 'approved'"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" align="center" />

      <!-- 优化后的操作列（下拉菜单） -->
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" icon="el-icon-edit" @click="openDialog(scope.row)" :disabled="scope.row.approveStatus === 'pending'">
            编辑
          </el-button>
          <el-dropdown @command="(cmd) => handleAction(cmd, scope.row)">
            <el-button size="small" type="text" icon="el-icon-more">更多</el-button>
            <template #dropdown>
              <el-dropdown-item command="benefit" icon="el-icon-s-data">权益配置</el-dropdown-item>
              <el-dropdown-item command="approve" icon="el-icon-check" v-if="scope.row.approveStatus === 'pending'">提交审批</el-dropdown-item>
              <el-dropdown-item
                command="delete"
                icon="el-icon-delete"
                divided
                style="color: #f56c6c"
                :disabled="scope.row.approveStatus === 'pending'"
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

    <!-- 新建/编辑弹窗（补充月/年价格、审批相关） -->
    <el-dialog v-model="dialogVisible" title="会员方案" width="700px" center>
      <el-form :model="form" label-width="80px" :rules="formRules" ref="formRef">
        <el-form-item label="方案名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入会员方案名称" />
        </el-form-item>

        <el-form-item label="方案描述" prop="desc">
          <el-input
            v-model="form.desc"
            type="textarea"
            placeholder="请输入会员方案描述"
            rows="3"
          />
        </el-form-item>

        <el-form-item label="货币类型" prop="currency">
          <el-select v-model="form.currency" placeholder="请选择货币类型">
            <el-option label="人民币" value="CNY" />
            <el-option label="美元" value="USD" />
          </el-select>
        </el-form-item>

        <div class="price-group">
          <el-form-item label="月付价格(元)" prop="priceMonthly">
            <el-input
              v-model="form.priceMonthly"
              type="number"
              placeholder="请输入月付价格"
              min="0"
              step="0.01"
              prefix="¥"
            />
          </el-form-item>
          <el-form-item label="年付价格(元)" prop="priceYearly">
            <el-input
              v-model="form.priceYearly"
              type="number"
              placeholder="请输入年付价格"
              min="0"
              step="0.01"
              prefix="¥"
            />
          </el-form-item>
        </div>

        <el-form-item label="有效期（天）" prop="duration">
          <el-input
            v-model="form.duration"
            type="number"
            placeholder="请输入有效期天数"
            min="1"
            step="1"
          />
        </el-form-item>

        <el-form-item label="初始状态">
          <el-radio-group v-model="form.status">
            <el-radio label="enabled">启用</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="审批备注" v-if="form.id">
          <el-input
            v-model="form.approveRemark"
            type="textarea"
            placeholder="请输入配置变更审批备注"
            rows="2"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存并提交审批</el-button>
      </template>
    </el-dialog>

    <!-- 权益配置弹窗（补充额度精细化配置） -->
    <el-dialog v-model="benefitDialogVisible" title="权益配置" width="700px" center>
      <div class="benefit-tip mb-10">
        配置 <span class="benefit-name">{{ currentBenefitRow?.name || '当前' }}</span> 会员的权益权限
      </div>
      <div v-for="item in benefitList" :key="item.key" class="feature-item">
        <div class="benefit-item-left">
          <el-icon class="benefit-icon"><setting /></el-icon>
          <span class="benefit-name">{{ item.name }}</span>
        </div>
        <div class="benefit-config" v-if="item.type === 'switch'">
          <el-switch v-model="item.enabled" active-text="开启" inactive-text="关闭" />
        </div>
        <div class="benefit-config" v-else-if="item.type === 'input'">
          <el-input
            v-model="item.value"
            type="number"
            :min="item.min || 0"
            :step="1"
            :placeholder="`请输入${item.name}额度`"
            suffix="次/天"
            style="width: 200px"
          />
        </div>
        <div class="benefit-config" v-else-if="item.type === 'unlimited'">
          <el-switch v-model="item.unlimited" active-text="无限额" inactive-text="有限额" @change="handleUnlimitedChange(item)" />
          <el-input
            v-model="item.value"
            type="number"
            :min="item.min || 0"
            :step="1"
            :placeholder="`请输入${item.name}额度`"
            suffix="GB"
            style="width: 180px; margin-left: 10px"
            :disabled="item.unlimited"
          />
        </div>
      </div>

      <template #footer>
        <el-button @click="benefitDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="saveBenefit">保存配置并提交审批</el-button>
      </template>
    </el-dialog>

    <!-- 手动调整用户会员弹窗（US-005核心功能） -->
    <el-dialog v-model="userAdjustDialogVisible" title="手动调整用户会员" width="700px" center>
      <el-form :model="adjustForm" label-width="80px" :rules="adjustRules" ref="adjustFormRef">
        <el-form-item label="用户ID/邮箱" prop="userKey">
          <el-input
            v-model="adjustForm.userKey"
            placeholder="请输入用户ID或邮箱搜索用户"
            clearable
            @blur="searchUser"
          />
          <el-button type="text" size="small" @click="searchUser" class="ml-5">搜索</el-button>
        </el-form-item>

        <el-form-item label="用户信息" v-if="adjustForm.userInfo">
          <el-card shadow="hover" style="width: 100%">
            <div class="user-info-item">ID：{{ adjustForm.userInfo.id }}</div>
            <div class="user-info-item">邮箱：{{ adjustForm.userInfo.email }}</div>
            <div class="user-info-item">当前会员：{{ adjustForm.userInfo.currentPlan || '免费版' }}</div>
            <div class="user-info-item">会员到期：{{ adjustForm.userInfo.planExpire || '永久' }}</div>
          </el-card>
        </el-form-item>

        <el-form-item label="目标会员方案" prop="targetPlan" v-if="adjustForm.userInfo">
          <el-select v-model="adjustForm.targetPlan" placeholder="请选择目标会员方案">
            <el-option
              v-for="plan in list.filter(item => item.approveStatus === 'approved')"
              :key="plan.id"
              :label="plan.name"
              :value="plan.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="有效期调整" prop="expireDays" v-if="adjustForm.userInfo">
          <el-input
            v-model="adjustForm.expireDays"
            type="number"
            placeholder="请输入调整后的有效期天数（如365）"
            min="1"
            step="1"
            suffix="天"
          />
          <el-tag type="info" size="small" class="ml-5">0表示永久有效</el-tag>
        </el-form-item>

        <el-form-item label="调整原因" prop="adjustReason" v-if="adjustForm.userInfo">
          <el-input
            v-model="adjustForm.adjustReason"
            type="textarea"
            placeholder="请输入手动调整原因（必填，用于审计）"
            rows="3"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="userAdjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUserAdjust">确认调整</el-button>
      </template>
    </el-dialog>

    <!-- 操作日志弹窗（审计日志查询） -->
    <el-dialog v-model="logDialogVisible" title="会员管理操作日志" width="800px" center>
      <div class="log-toolbar mb-10">
        <el-input
          v-model="logSearch"
          placeholder="搜索操作人/操作内容/目标ID"
          clearable
          style="width: 300px"
          class="mr-10"
          prefix-icon="el-icon-search"
        />
        <el-date-picker
          v-model="logDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 300px"
          class="mr-10"
        />
        <el-select v-model="logType" placeholder="操作类型" style="width: 150px">
          <el-option label="全部" value="" />
          <el-option label="方案创建" value="create" />
          <el-option label="方案编辑" value="update" />
          <el-option label="状态变更" value="status" />
          <el-option label="权益配置" value="benefit" />
          <el-option label="用户调整" value="user_adjust" />
          <el-option label="删除方案" value="delete" />
        </el-select>
      </div>
      <el-table
        :data="logList"
        v-loading="logLoading"
        border
        stripe
        style="width: 100%"
        :empty-text="logList.length === 0 ? '暂无操作日志' : ''"
      >
        <el-table-column prop="operateTime" label="操作时间" width="180" align="center" />
        <el-table-column prop="operatorName" label="操作人" width="120" align="center" />
        <el-table-column prop="operateType" label="操作类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getLogTypeTag(scope.row.operateType)">{{ scope.row.operateTypeDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetName" label="操作目标" width="180" align="center" />
        <el-table-column prop="operateContent" label="操作内容" min-width="200" />
        <el-table-column label="操作详情" width="100" align="center">
          <template #default="scope">
            <el-button size="small" type="text" @click="openLogDetail(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container mt-10">
        <el-pagination
          v-model:current-page="logCurrentPage"
          v-model:page-size="logPageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="logTotal"
        />
      </div>
    </el-dialog>

    <!-- 日志详情弹窗 -->
    <el-dialog v-model="logDetailDialogVisible" title="操作日志详情" width="600px" center>
      <el-descriptions :column="1" border style="width: 100%">
        <el-descriptions-item label="操作人">{{ currentLog.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog.operateTime }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ currentLog.operateIp || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog.operateTypeDesc }}</el-descriptions-item>
        <el-descriptions-item label="操作目标">{{ currentLog.targetName }}</el-descriptions-item>
        <el-descriptions-item label="操作内容">{{ currentLog.operateContent }}</el-descriptions-item>
        <el-descriptions-item label="操作前数据" v-if="currentLog.beforeData">
          <pre class="log-pre">{{ JSON.stringify(currentLog.beforeData, null, 2) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="操作后数据" v-if="currentLog.afterData">
          <pre class="log-pre">{{ JSON.stringify(currentLog.afterData, null, 2) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="操作备注" v-if="currentLog.remark">{{ currentLog.remark }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="logDetailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 高级筛选 -->
    <el-drawer v-model="advancedVisible" title="高级筛选" size="600px">
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
          <el-form-item label="价格区间">
            <div class="price-range">
              <el-input
                v-model="advancedForm.minPrice"
                type="number"
                placeholder="最低价格"
                prefix="¥"
                style="width: 45%"
              />
              <span class="range-separator">至</span>
              <el-input
                v-model="advancedForm.maxPrice"
                type="number"
                placeholder="最高价格"
                prefix="¥"
                style="width: 45%"
              />
            </div>
          </el-form-item>
          <el-form-item label="有效期范围">
            <div class="duration-range">
              <el-input
                v-model="advancedForm.minDuration"
                type="number"
                placeholder="最少天数"
                suffix="天"
                style="width: 45%"
              />
              <span class="range-separator">至</span>
              <el-input
                v-model="advancedForm.maxDuration"
                type="number"
                placeholder="最多天数"
                suffix="天"
                style="width: 45%"
              />
            </div>
          </el-form-item>
          <el-form-item label="审批状态">
            <el-select v-model="advancedForm.approveStatus" placeholder="请选择审批状态" multiple>
              <el-option label="待审批" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已驳回" value="rejected" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="filter-actions" style="margin-top: 20px; text-align: right">
          <el-button @click="resetAdvancedFilter">重置</el-button>
          <el-button type="primary" class="ml-10">应用筛选</el-button>
        </div>
      </div>
    </el-drawer>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, ElDescriptions } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'

// loading
const loading = ref(false)
const logLoading = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(5)
const logCurrentPage = ref(1)
const logPageSize = ref(10)
const logTotal = ref(0)

// 查询
const searchName = ref('')
const statusFilter = ref('')
const approveFilter = ref('')
const logSearch = ref('')
const logType = ref('')
const logDateRange = ref<Date[]>([])

// 选中
const selectedRows = ref([])

// mock 数据 (补充月/年价格、审批状态)
const list = ref([
  { id: 'M001', name: '基础月度会员', priceMonthly: 19, priceYearly: 199, currency: 'CNY', duration: 30, status: 'enabled', approveStatus: 'approved', createTime: '2026-03-18' },
  { id: 'M002', name: '高级月度会员', priceMonthly: 49, priceYearly: 499, currency: 'CNY', duration: 30, status: 'enabled', approveStatus: 'approved', createTime: '2026-03-17' },
  { id: 'M003', name: '基础季度会员', priceMonthly: 0, priceYearly: 55, currency: 'CNY', duration: 90, status: 'enabled', approveStatus: 'approved', createTime: '2026-03-16' },
  { id: 'M004', name: '高级季度会员', priceMonthly: 0, priceYearly: 139, currency: 'CNY', duration: 90, status: 'enabled', approveStatus: 'approved', createTime: '2026-03-15' },
  { id: 'M005', name: '基础年度会员', priceMonthly: 0, priceYearly: 199, currency: 'CNY', duration: 365, status: 'enabled', approveStatus: 'approved', createTime: '2026-03-14' },
  { id: 'M006', name: '高级年度会员', priceMonthly: 0, priceYearly: 499, currency: 'CNY', duration: 365, status: 'disabled', approveStatus: 'approved', createTime: '2026-03-13' },
  { id: 'M007', name: '终身会员', priceMonthly: 0, priceYearly: 1999, currency: 'CNY', duration: 9999, status: 'disabled', approveStatus: 'pending', createTime: '2026-03-12' }
])

// 操作日志mock
const logList = ref([
  {
    operateTime: '2026-03-18 10:20:30',
    operatorName: '运营小王',
    operateType: 'create',
    operateTypeDesc: '方案创建',
    targetName: '基础月度会员(M001)',
    operateContent: '创建基础月度会员方案，月付19元，年付199元，有效期30天',
    operateIp: '192.168.1.100',
    beforeData: null,
    afterData: { id: 'M001', name: '基础月度会员', priceMonthly: 19, priceYearly: 199 },
    remark: '新上线基础会员方案'
  },
  {
    operateTime: '2026-03-17 14:30:20',
    operatorName: '产品老李',
    operateType: 'user_adjust',
    operateTypeDesc: '用户调整',
    targetName: '用户(10001)',
    operateContent: '将用户10001的会员从免费版调整为高级月度会员，有效期30天',
    operateIp: '192.168.1.101',
    beforeData: { userId: '10001', currentPlan: 'free', expire: '2026-03-17' },
    afterData: { userId: '10001', currentPlan: 'M002', expire: '2026-04-17' },
    remark: '用户活动奖励'
  }
])
const currentLog = ref({})
logTotal.value = logList.value.length

// 过滤后的列表
const filteredList = computed(() => {
  return list.value.filter(item => {
    return (
      (!searchName.value || item.name.includes(searchName.value)) &&
      (!statusFilter.value || item.status === statusFilter.value) &&
      (!approveFilter.value || item.approveStatus === approveFilter.value)
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
  currentPage.value = 1 // 重置到第一页
}

// 处理页码改变
const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

// 弹窗控制
const dialogVisible = ref(false)
const benefitDialogVisible = ref(false)
const advancedVisible = ref(false)
const userAdjustDialogVisible = ref(false)
const logDialogVisible = ref(false)
const logDetailDialogVisible = ref(false)

// 当前权益配置行/当前日志
const currentBenefitRow = ref<any>({})

// 表单ref
const formRef = ref<any>(null)
const adjustFormRef = ref<any>(null)

// 会员方案表单（补充月/年价格、货币、审批备注）
const form = reactive({
  id: '',
  name: '',
  desc: '',
  currency: 'CNY',
  priceMonthly: '',
  priceYearly: '',
  duration: '',
  status: 'enabled',
  approveStatus: 'pending',
  approveRemark: ''
})

// 表单验证规则（补充月/年价格）
const formRules = reactive({
  name: [{ required: true, message: '请输入方案名称', trigger: 'blur' }],
  currency: [{ required: true, message: '请选择货币类型', trigger: 'blur' }],
  priceMonthly: [{ required: true, message: '请输入月付价格', trigger: 'blur' }],
  priceYearly: [{ required: true, message: '请输入年付价格', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入有效期', trigger: 'blur' }]
})

// 高级筛选表单（补充审批状态）
const advancedForm = reactive({
  createTime: [],
  minPrice: '',
  maxPrice: '',
  minDuration: '',
  maxDuration: '',
  approveStatus: []
})

// 权益mock（补充额度类型：开关/输入/无限额，匹配PRD额度配置）
const benefitList = ref([
  { key: 'b1', name: '对话次数提升', type: 'input', enabled: true, value: 1000, min: 0 },
  { key: 'b2', name: '优先响应', type: 'switch', enabled: false, value: 0 },
  { key: 'b3', name: '高级模型权限', type: 'switch', enabled: false, value: 0 },
  { key: 'b4', name: '数据存储容量', type: 'unlimited', enabled: true, unlimited: false, value: 10, min: 1 },
  { key: 'b5', name: 'API调用次数', type: 'input', enabled: true, value: 5000, min: 0 },
  { key: 'b6', name: '团队成员数量', type: 'input', enabled: true, value: 10, min: 1 }
])

// 手动调整用户会员表单（US-005）
const adjustForm = reactive({
  userKey: '', // 用户ID/邮箱
  userInfo: null, // 搜索到的用户信息
  targetPlan: '', // 目标会员方案ID
  expireDays: '', // 调整后有效期天数
  adjustReason: '' // 调整原因
})

// 调整用户会员验证规则
const adjustRules = reactive({
  userKey: [{ required: true, message: '请输入用户ID或邮箱', trigger: 'blur' }],
  targetPlan: [{ required: true, message: '请选择目标会员方案', trigger: 'blur' }],
  expireDays: [{ required: true, message: '请输入有效期天数', trigger: 'blur' }],
  adjustReason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
})

// 打开会员方案编辑/新建弹窗
const openDialog = (row?: any) => {
  if (row) {
    Object.assign(form, row)
  } else {
    form.id = ''
    form.name = ''
    form.desc = ''
    form.currency = 'CNY'
    form.priceMonthly = ''
    form.priceYearly = ''
    form.duration = ''
    form.status = 'enabled'
    form.approveStatus = 'pending'
    form.approveRemark = ''
  }
  dialogVisible.value = true
}

// 保存会员方案并提交审批
const handleSave = () => {
  // 表单验证
  formRef.value.validate((valid: boolean) => {
    if (!valid) return
    // 记录操作日志（埋点）
    const logData = {
      operatorName: '运营小王', // 实际从登录信息获取
      operateType: form.id ? 'update' : 'create',
      operateTypeDesc: form.id ? '方案编辑' : '方案创建',
      targetName: form.name + (form.id ? `(${form.id})` : ''),
      operateContent: form.id ? `编辑${form.name}方案，月付${form.priceMonthly}元，年付${form.priceYearly}元` : `创建${form.name}方案，月付${form.priceMonthly}元，年付${form.priceYearly}元`,
      beforeData: form.id ? list.value.find(item => item.id === form.id) : null,
      afterData: { ...form },
      remark: form.approveRemark || '无'
    }
    recordOperateLog(logData) // 记录日志
    // TODO: 调用新增/编辑会员方案并提交审批接口
    ElMessage.success(form.id ? '编辑成功，已提交审批' : '创建成功，已提交审批')
    dialogVisible.value = false
    // 模拟刷新列表
    if (!form.id) {
      list.value.push({
        ...form,
        id: `M${list.value.length + 100}`,
        createTime: new Date().toLocaleDateString().replace(/\//g, '-')
      })
    }
  })
}

// 状态切换（需审批通过才能修改）
const handleStatusChange = (row: any) => {
  // 记录操作日志
  const logData = {
    operatorName: '运营小王',
    operateType: 'status',
    operateTypeDesc: '状态变更',
    targetName: `${row.name}(${row.id})`,
    operateContent: `将${row.name}状态从${row.status === 'enabled' ? '禁用' : '启用'}改为${row.status === 'enabled' ? '启用' : '禁用'}`,
    beforeData: { ...row, status: row.status === 'enabled' ? 'disabled' : 'enabled' },
    afterData: { ...row },
    remark: '运营调整状态'
  }
  recordOperateLog(logData)
  // TODO: 调用会员状态更新接口
  ElMessage.success(`状态更新为 ${row.status === 'enabled' ? '启用' : '禁用'}，已记录审计日志`)
}

// 删除会员方案
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    '确认删除该会员方案吗？删除后将无法恢复',
    '删除确认',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 记录操作日志
    const logData = {
      operatorName: '运营小王',
      operateType: 'delete',
      operateTypeDesc: '删除方案',
      targetName: `${row.name}(${row.id})`,
      operateContent: `删除${row.name}会员方案`,
      beforeData: { ...row },
      afterData: null,
      remark: '产品下线'
    }
    recordOperateLog(logData)
    // TODO: 调用删除接口
    list.value = list.value.filter(item => item.id !== row.id)
    ElMessage.success('删除成功，已记录审计日志')
  })
}

// 批量选择
const handleSelectionChange = (rows: any) => {
  selectedRows.value = rows
  // TODO: 批量操作接口
}

// 打开权益配置弹窗
const openBenefitDialog = (row: any) => {
  currentBenefitRow.value = row
  // TODO: 调用获取会员权益接口，初始化benefitList
  benefitDialogVisible.value = true
}

// 保存权益配置并提交审批
const saveBenefit = () => {
  // 记录操作日志
  const logData = {
    operatorName: '运营小王',
    operateType: 'benefit',
    operateTypeDesc: '权益配置',
    targetName: `${currentBenefitRow.value.name}(${currentBenefitRow.value.id})`,
    operateContent: `配置${currentBenefitRow.value.name}会员权益，共${benefitList.value.filter(item => item.enabled).length}项权益开启`,
    beforeData: { ...currentBenefitRow.value, benefits: benefitList.value.map(item => ({ ...item })) },
    afterData: { ...currentBenefitRow.value, benefits: benefitList.value.map(item => ({ ...item })) },
    remark: '权益优化调整'
  }
  recordOperateLog(logData)
  // TODO: 保存权益配置并提交审批接口
  ElMessage.success('权益配置已保存，已提交审批并记录审计日志')
  benefitDialogVisible.value = false
}

// 无限额开关变更
const handleUnlimitedChange = (item: any) => {
  if (item.unlimited) {
    item.value = 0
  }
}

// 提交审批
const handleApproveSubmit = (row: any) => {
  // TODO: 调用提交审批接口
  row.approveStatus = 'pending'
  ElMessage.success('已提交审批，请等待审核')
  // 记录操作日志
  const logData = {
    operatorName: '运营小王',
    operateType: 'approve_submit',
    operateTypeDesc: '提交审批',
    targetName: `${row.name}(${row.id})`,
    operateContent: `提交${row.name}会员方案审批`,
    beforeData: { ...row, approveStatus: row.approveStatus },
    afterData: { ...row, approveStatus: 'pending' },
    remark: '配置变更提交审批'
  }
  recordOperateLog(logData)
}

// 高级筛选
const openAdvancedFilter = () => {
  // TODO: 高级筛选接口
  advancedVisible.value = true
}

// 重置高级筛选
const resetAdvancedFilter = () => {
  advancedForm.createTime = []
  advancedForm.minPrice = ''
  advancedForm.maxPrice = ''
  advancedForm.minDuration = ''
  advancedForm.maxDuration = ''
  advancedForm.approveStatus = []
}

// 打开操作日志
const openLog = () => {
  // TODO: 调用审计日志查询接口，初始化logList
  logDialogVisible.value = true
}

// 打开日志详情
const openLogDetail = (row: any) => {
  currentLog.value = row
  logDetailDialogVisible.value = true
}

// 获取日志类型标签
const getLogTypeTag = (type: string) => {
  switch (type) {
    case 'create': return 'success'
    case 'update': return 'primary'
    case 'status': return 'warning'
    case 'benefit': return 'info'
    case 'user_adjust': return 'purple'
    case 'delete': return 'danger'
    default: return 'default'
  }
}

// 打开手动调整用户会员弹窗
const openUserAdjustDialog = () => {
  // 重置表单
  adjustForm.userKey = ''
  adjustForm.userInfo = null
  adjustForm.targetPlan = ''
  adjustForm.expireDays = ''
  adjustForm.adjustReason = ''
  userAdjustDialogVisible.value = true
}

// 搜索用户
const searchUser = () => {
  if (!adjustForm.userKey) {
    ElMessage.warning('请输入用户ID或邮箱')
    return
  }
  // TODO: 调用用户搜索接口
  adjustForm.userInfo = {
    id: '10001',
    email: 'user@example.com',
    currentPlan: '基础月度会员(M001)',
    planExpire: '2026-04-18'
  }
  ElMessage.success('用户搜索成功')
}

// 确认调整用户会员（US-005核心）
const handleUserAdjust = () => {
  adjustFormRef.value.validate((valid: boolean) => {
    if (!valid) return
    // 记录操作日志（审计要求）
    const targetPlan = list.value.find(item => item.id === adjustForm.targetPlan)
    const logData = {
      operatorName: '运营小王',
      operateType: 'user_adjust',
      operateTypeDesc: '用户调整',
      targetName: `用户(${adjustForm.userInfo.id})`,
      operateContent: `将用户${adjustForm.userInfo.id}的会员从${adjustForm.userInfo.currentPlan}调整为${targetPlan.name}，有效期${adjustForm.expireDays}天`,
      beforeData: { ...adjustForm.userInfo },
      afterData: {
        ...adjustForm.userInfo,
        currentPlan: targetPlan.name,
        planExpire: adjustForm.expireDays === 0 ? '永久' : new Date(Date.now() + adjustForm.expireDays * 24 * 60 * 60 * 1000).toLocaleDateString().replace(/\//g, '-')
      },
      remark: adjustForm.adjustReason
    }
    recordOperateLog(logData)
    // TODO: 调用手动调整用户会员接口（/api/v1/users/:id/subscription/adjust）
    ElMessage.success('用户会员调整成功，已实时生效并记录审计日志')
    userAdjustDialogVisible.value = false
    // 模拟添加日志
    logList.value.unshift(logData)
    logTotal.value = logList.value.length
  })
}

// 下拉菜单操作处理
const handleAction = (cmd: string, row: any) => {
  if (cmd === 'benefit') {
    openBenefitDialog(row)
  } else if (cmd === 'delete') {
    handleDelete(row)
  } else if (cmd === 'approve') {
    handleApproveSubmit(row)
  }
}

// 记录操作日志（通用方法，埋点核心）
const recordOperateLog = (logData: any) => {
  // 补充公共日志字段
  const log = {
    operateTime: new Date().toLocaleString().replace(/\//g, '-'),
    operateIp: '192.168.1.100', // 实际从请求头获取
    ...logData
  }
  // TODO: 调用审计日志记录接口（/api/v1/audit-logs）
  logList.value.unshift(log)
  logTotal.value = logList.value.length
}

// 监听搜索和过滤，重置页码
watch([searchName, statusFilter, approveFilter], () => {
  currentPage.value = 1
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

/* 数据概览卡片 */
.stats-card {
  display: flex;
  gap: 24px;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
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

/* 表格样式 */
.price-text {
  color: #e64340;
  font-weight: 500;
}

/* 权益配置样式 */
.benefit-tip {
  padding: 12px;
  background-color: #f9fafb;
  border-radius: 6px;
  font-size: 14px;
}

.benefit-name {
  color: #409eff;
  font-weight: 500;
}

.feature-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 8px;
  background-color: #f9fafb;
  border-radius: 6px;
}

.benefit-item-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.benefit-icon {
  color: #409eff;
}

.benefit-config {
  display: flex;
  align-items: center;
}

/* 高级筛选样式 */
.advanced-filter-content {
  padding: 16px;
}

.price-range, .duration-range {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.range-separator {
  margin: 0 10px;
  color: #999;
}

/* 价格组样式 */
.price-group {
  display: flex;
  gap: 20px;
}

/* 用户信息样式 */
.user-info-item {
  margin-bottom: 8px;
  font-size: 14px;
}

/* 日志样式 */
.log-toolbar {
  display: flex;
  align-items: center;
}

.log-pre {
  font-size: 12px;
  color: #6b7280;
  max-height: 200px;
  overflow-y: auto;
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
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 表格样式优化 */
:deep(.el-table) {
  --el-table-header-text-color: #4b5563;
  --el-table-row-hover-bg-color: #f9fafb;
}

:deep(.el-table th) {
  background-color: #f9fafb;
  font-weight: 500;
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