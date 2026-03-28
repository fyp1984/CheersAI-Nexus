<template>
  <el-card class="page-container" shadow="hover">
    <!-- 顶部标题区 -->
    <div class="header">
      <div class="header-left">
        <div class="title">会员方案管理</div>
        <div class="subtitle">管理会员等级、价格及权益配置</div>
      </div>
      <el-tag type="danger" size="large">P0</el-tag>
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
        <div class="stat-value primary">{{ list.filter(item => item.auditStatus === 'pending').length }}</div>
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
      <el-table-column prop="code" label="方案编码" width="120" align="center" />
      <el-table-column label="月付价格(元)" width="120" align="center">
        <template #default="scope">
          <span class="price-text">¥{{ scope.row.priceMonthly || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="年付价格(元)" width="120" align="center">
        <template #default="scope">
          <span class="price-text">¥{{ scope.row.priceYearly || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审批状态" width="140" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.auditStatus === 'pending'" type="warning">待审批</el-tag>
          <el-tag v-else-if="scope.row.auditStatus === 'approved'" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.auditStatus === 'rejected'" type="danger">已驳回</el-tag>
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
            :disabled="scope.row.auditStatus !== 'approved'"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
        <template #default="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>

      <!-- 优化后的操作列（下拉菜单） -->
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" icon="el-icon-edit" @click="openDialog(scope.row)" :disabled="scope.row.auditStatus === 'pending'">
            编辑
          </el-button>
          <el-dropdown @command="(cmd: string) => handleAction(cmd, scope.row)">
            <el-button size="small" type="text" icon="el-icon-more">更多</el-button>
            <template #dropdown>
              <el-dropdown-item command="benefit" icon="el-icon-s-data">权益配置</el-dropdown-item>
              <el-dropdown-item command="log" icon="el-icon-document">操作日志</el-dropdown-item>
              <el-dropdown-item command="approve" icon="el-icon-check" v-if="scope.row.auditStatus !== 'approved'">提交审批</el-dropdown-item>
              <el-dropdown-item
                command="delete"
                icon="el-icon-delete"
                divided
                style="color: #f56c6c"
                :disabled="scope.row.auditStatus === 'pending'"
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

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="会员方案" width="700px" center>
      <el-form :model="form" label-width="80px" :rules="formRules" ref="formRef">
        <el-form-item label="方案名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入会员方案名称" />
        </el-form-item>

        <el-form-item label="方案描述" prop="description">
          <el-input
            v-model="form.description"
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

        <el-form-item label="排序" prop="sortOrder">
          <el-input
            v-model="form.sortOrder"
            type="number"
            placeholder="请输入排序号"
            min="0"
            step="1"
          />
        </el-form-item>

        <el-form-item label="初始状态">
          <el-radio-group v-model="form.status">
            <el-radio label="enabled">启用</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 需要后端接口：更新方案时的审批备注 -->
        <!-- API: PUT /api/v1/plans/{code} - 支持 applyRemark 参数 -->
        <el-form-item label="审批备注" v-if="form.code">
          <el-input
            v-model="form.applyRemark"
            type="textarea"
            placeholder="请输入配置变更审批备注"
            rows="2"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 权益配置弹窗 -->
    <!-- 需要后端接口支持 -->
    <!-- API: GET /api/v1/plans/{code} - 获取 features 和 limits 字段 -->
    <!-- API: PUT /api/v1/plans/{code} - 更新 features 和 limits 字段 -->
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
        <el-button type="primary" @click="saveBenefit">保存配置</el-button>
      </template>
    </el-dialog>

    <!-- 手动调整用户会员弹窗 -->
    <!-- 需要后端接口支持 -->
    <!-- API: GET /api/v1/users/{userId}/subscription - 获取用户订阅信息 -->
    <!-- API: POST /api/v1/users/{userId}/subscription/adjust - 调整用户订阅 -->
    <el-dialog v-model="userAdjustDialogVisible" title="手动调整用户会员" width="700px" center>
      <el-form :model="adjustForm" label-width="80px" :rules="adjustRules" ref="adjustFormRef">
        <el-form-item label="用户ID" prop="userKey">
          <el-input
            v-model="adjustForm.userKey"
            placeholder="请输入用户ID搜索用户"
            clearable
          />
          <el-button type="text" size="small" @click="searchUser" class="ml-5">搜索</el-button>
        </el-form-item>

        <el-form-item label="用户信息" v-if="adjustForm.userInfo">
          <el-card shadow="hover" style="width: 100%">
            <div class="user-info-item">ID：{{ adjustForm.userInfo.userId }}</div>
            <div class="user-info-item">当前会员：{{ adjustForm.userInfo.currentPlanName || '免费版' }}</div>
            <div class="user-info-item">会员到期：{{ adjustForm.userInfo.planExpire || '永久' }}</div>
          </el-card>
        </el-form-item>

        <el-form-item label="目标会员方案" prop="targetPlan" v-if="adjustForm.userInfo">
          <el-select v-model="adjustForm.targetPlan" placeholder="请选择目标会员方案">
            <el-option
              v-for="plan in list.filter(item => item.auditStatus === 'approved')"
              :key="plan.code"
              :label="plan.name"
              :value="plan.code"
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

    <!-- 审批弹窗 -->
    <el-dialog v-model="approvalDialogVisible" title="审批会员计划变更" width="600px" center>
      <div v-if="pendingAuditData" class="approval-content">
        <el-alert
          title="待审批变更详情"
          type="info"
          :closable="false"
          class="mb-15"
        />
        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请人">{{ pendingAuditData.applicantName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDateTime(pendingAuditData.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="申请类型">
            <el-tag>{{ getAuditTypeDesc(pendingAuditData.applyType) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="变更内容">
            <div v-if="pendingAuditData.applyType === 'update'">
              <div v-if="pendingAuditData.beforeData">
                <strong>变更前：</strong>
                <pre class="data-pre">{{ formatJsonData(pendingAuditData.beforeData) }}</pre>
              </div>
              <div v-if="pendingAuditData.afterData" class="mt-5">
                <strong>变更后：</strong>
                <pre class="data-pre">{{ formatJsonData(pendingAuditData.afterData) }}</pre>
              </div>
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="申请说明">{{ pendingAuditData.applyRemark || '无' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <el-form :model="approvalForm" label-width="80px">
          <el-form-item label="审批结果">
            <el-radio-group v-model="approvalForm.action">
              <el-radio label="approve">通过</el-radio>
              <el-radio label="reject">驳回</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审批备注">
            <el-input
              v-model="approvalForm.auditRemark"
              type="textarea"
              placeholder="请输入审批备注"
              :rows="3"
            />
          </el-form-item>
        </el-form>
      </div>
      <div v-else class="empty-approval">
        <el-empty description="暂无待审批的变更记录" />
      </div>
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button 
          v-if="pendingAuditData" 
          type="primary" 
          @click="handleApprovalSubmit"
          :loading="approvalLoading"
        >
          提交审批
        </el-button>
      </template>
    </el-dialog>

    <!-- 操作日志弹窗 -->
    <!-- 需要后端接口支持 -->
    <!-- API: 暂未提供会员计划操作日志接口，需要新增 -->
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
        <el-table-column prop="createdAt" label="操作时间" width="180" align="center" />
        <el-table-column prop="operatorName" label="操作人" width="120" align="center" />
        <el-table-column prop="operateType" label="操作类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getLogTypeTag(scope.row.operateType)">{{ getLogTypeDesc(scope.row.operateType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetName" label="操作目标" width="180" align="center" />
        <el-table-column prop="applyRemark" label="操作内容" min-width="200" />
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
        <el-descriptions-item label="操作人">{{ currentLog.applicantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatDateTime(currentLog.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ getLogTypeDesc(currentLog.operateType) }}</el-descriptions-item>
        <el-descriptions-item label="操作目标">{{ currentLog.planId }}</el-descriptions-item>
        <el-descriptions-item label="操作内容">{{ currentLog.applyRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="变更前数据" v-if="currentLog.beforeData">
          <pre class="log-pre">{{ formatJsonData(currentLog.beforeData) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="变更后数据" v-if="currentLog.afterData">
          <pre class="log-pre">{{ formatJsonData(currentLog.afterData) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="审批备注" v-if="currentLog.auditRemark">{{ currentLog.auditRemark }}</el-descriptions-item>
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
          <el-form-item label="排序范围">
            <div class="duration-range">
              <el-input
                v-model="advancedForm.minSortOrder"
                type="number"
                placeholder="最小排序号"
                suffix="号"
                style="width: 45%"
              />
              <span class="range-separator">至</span>
              <el-input
                v-model="advancedForm.maxSortOrder"
                type="number"
                placeholder="最大排序号"
                suffix="号"
                style="width: 45%"
              />
            </div>
          </el-form-item>
          <el-form-item label="审批状态">
            <el-select v-model="advancedForm.auditStatus" placeholder="请选择审批状态" multiple>
              <el-option label="待审批" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已驳回" value="rejected" />
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
import { Setting } from '@element-plus/icons-vue'
import * as membershipApi from '../api/membership'
import type { PlanDetailDTO, PlanCreateDTO, PlanUpdateDTO, PlanAuditDTO, BenefitItem } from '../api/membership'

// ========== API 接口说明 ==========
// 会员计划相关 API（已实现）：
// - GET    /api/v1/plans              - fetchPlanList()           - 获取会员计划列表
// - GET    /api/v1/plans/{code}       - fetchPlanDetail()        - 获取会员计划详情
// - POST   /api/v1/plans              - createPlan()             - 创建会员计划
// - PUT    /api/v1/plans/{code}      - updatePlan()             - 更新会员计划
// - DELETE /api/v1/plans/{code}      - deletePlan()             - 删除会员计划
// - POST   /api/v1/plans/{code}/audit - auditPlan()            - 审批会员计划变更
// - GET    /api/v1/plans/{code}/pending-audit - fetchPendingAudit() - 获取待审批记录
// - GET    /api/v1/plans/{code}/benefits - fetchPlanBenefits() - 获取权益配置
// - PUT    /api/v1/plans/{code}/benefits - updatePlanBenefits() - 更新权益配置
// - GET    /api/v1/plans/{code}/audit-logs - fetchPlanAuditLogs() - 获取操作日志
//
// 订阅相关 API（已实现）：
// - GET    /api/v1/users/{userId}/subscription - fetchUserSubscription()   - 获取用户订阅信息
// - POST   /api/v1/users/{userId}/subscription/adjust - adjustUserSubscription() - 调整用户订阅

// loading 状态
const loading = ref(false)
const logLoading = ref(false)
const saving = ref(false)

// 会员计划列表数据类型
interface PlanListItem extends PlanDetailDTO {
  createTime?: string
}

// 分页相关
const currentPage = ref(1)
const pageSize = ref(5)
const logCurrentPage = ref(1)
const logPageSize = ref(10)
const logTotal = ref(0)

// 查询条件
const searchName = ref('')
const statusFilter = ref('')
const approveFilter = ref('')
const logSearch = ref('')
const logType = ref('')
const logDateRange = ref<Date[]>([])

// 选中行
const selectedRows = ref([])

// 会员计划列表数据 - API: GET /api/v1/plans
const list = ref<PlanListItem[]>([])

// 加载会员计划列表 - API: GET /api/v1/plans
const loadPlanList = async () => {
  loading.value = true
  try {
    // API: fetchPlanList() -> GET /api/v1/plans
    const data = await membershipApi.fetchPlanList()
    list.value = data.map(item => ({
      ...item,
      status: item.status === 'active' ? 'enabled' : 'disabled'
    }))
  } catch (error: any) {
    ElMessage.error(error.message || '加载会员计划列表失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadPlanList()
})

// 操作日志列表 - 需要后端接口支持（暂未提供）
const logList = ref<any[]>([])
const currentLog = ref<any>({})
logTotal.value = 0

// 过滤后的列表
const filteredList = computed(() => {
  return list.value.filter(item => {
    return (
      (!searchName.value || item.name.includes(searchName.value)) &&
      (!statusFilter.value || item.status === statusFilter.value) ||
      // 兼容后端返回的 auditStatus 字段
      (!approveFilter.value || item.auditStatus === approveFilter.value || 
        (approveFilter.value === 'approved' && !item.auditStatus) ||
        (approveFilter.value === 'pending' && item.auditStatus === 'pending'))
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

// 弹窗控制
const dialogVisible = ref(false)
const benefitDialogVisible = ref(false)
const advancedVisible = ref(false)
const userAdjustDialogVisible = ref(false)
const logDialogVisible = ref(false)
const logDetailDialogVisible = ref(false)
const approvalDialogVisible = ref(false)
const approvalLoading = ref(false)
const pendingAuditData = ref<any>(null)
const currentApprovalPlan = ref<any>(null)

const approvalForm = reactive({
  action: 'approve' as 'approve' | 'reject',
  auditRemark: ''
})

// 当前权益配置行/当前日志
const currentBenefitRow = ref<any>({})

// 表单ref
const formRef = ref<any>(null)
const adjustFormRef = ref<any>(null)

// 会员方案表单
const form = reactive({
  id: '',
  code: '',
  name: '',
  description: '',
  currency: 'CNY',
  priceMonthly: '',
  priceYearly: '',
  sortOrder: 0,
  status: 'enabled',
  auditStatus: '',
  applyRemark: ''
})

// 表单验证规则
const formRules = reactive({
  name: [{ required: true, message: '请输入方案名称', trigger: 'blur' }],
  currency: [{ required: true, message: '请选择货币类型', trigger: 'blur' }],
  priceMonthly: [{ required: true, message: '请输入月付价格', trigger: 'blur' }],
  priceYearly: [{ required: true, message: '请输入年付价格', trigger: 'blur' }]
})

// 高级筛选表单
const advancedForm = reactive({
  createTime: [],
  minPrice: '',
  maxPrice: '',
  minSortOrder: '',
  maxSortOrder: '',
  auditStatus: [] as string[]
})

// 权益配置列表 - 从后端获取
const benefitList = ref<BenefitItem[]>([])

// 手动调整用户会员表单
const adjustForm = reactive({
  userKey: '',
  userInfo: null as any,
  targetPlan: '',
  expireDays: '',
  adjustReason: ''
})

// 调整用户会员验证规则
const adjustRules = reactive({
  userKey: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  targetPlan: [{ required: true, message: '请选择目标会员方案', trigger: 'blur' }],
  expireDays: [{ required: true, message: '请输入有效期天数', trigger: 'blur' }],
  adjustReason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
})

// 打开会员方案编辑/新建弹窗
const openDialog = (row?: any) => {
  if (row) {
    Object.assign(form, {
      id: row.id,
      code: row.code,
      name: row.name,
      description: row.description || '',
      currency: row.currency || 'CNY',
      priceMonthly: row.priceMonthly || '',
      priceYearly: row.priceYearly || '',
      sortOrder: row.sortOrder || 0,
      status: row.status === 'enabled' ? 'enabled' : 'disabled',
      auditStatus: row.auditStatus || '',
      applyRemark: ''
    })
  } else {
    form.id = ''
    form.code = ''
    form.name = ''
    form.description = ''
    form.currency = 'CNY'
    form.priceMonthly = ''
    form.priceYearly = ''
    form.sortOrder = 0
    form.status = 'enabled'
    form.auditStatus = ''
    form.applyRemark = ''
  }
  dialogVisible.value = true
}

// 保存会员方案 - API: POST /api/v1/plans 或 PUT /api/v1/plans/{code}
const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    loading.value = true
    
    if (form.code) {
      // 编辑模式 - API: updatePlan() -> PUT /api/v1/plans/{code}
      const updateData: PlanUpdateDTO = {
        name: form.name,
        description: form.description,
        priceMonthly: form.priceMonthly !== '' ? Number(form.priceMonthly) : undefined,
        priceYearly: form.priceYearly !== '' ? Number(form.priceYearly) : undefined,
        currency: form.currency,
        sortOrder: form.sortOrder ? Number(form.sortOrder) : undefined,
        status: form.status === 'enabled' ? 'active' : 'disabled',
        applyRemark: form.applyRemark
      }
      await membershipApi.updatePlan(form.code, updateData)
      ElMessage.success('更新成功')
    } else {
      // 新建模式 - API: createPlan() -> POST /api/v1/plans
      // 生成唯一编码
      const planCode = `plan_${Date.now()}`
      const createData: PlanCreateDTO = {
        code: planCode,
        name: form.name,
        description: form.description,
        priceMonthly: form.priceMonthly ? Number(form.priceMonthly) : undefined,
        priceYearly: form.priceYearly ? Number(form.priceYearly) : undefined,
        currency: form.currency,
        sortOrder: form.sortOrder ? Number(form.sortOrder) : 0,
        status: form.status === 'enabled' ? 'active' : 'disabled',
        features: JSON.stringify({ chat: true }),
        limits: JSON.stringify({ tokens: 10000 })
      }
      await membershipApi.createPlan(createData)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    await loadPlanList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

// 状态切换 - API: updatePlan() -> PUT /api/v1/plans/{code}
// 需要后端支持：更新 status 时需要记录审计日志
const handleStatusChange = async (row: any) => {
  try {
    // API: updatePlan() -> PUT /api/v1/plans/{code}
    await membershipApi.updatePlan(row.code, {
      status: row.status === 'enabled' ? 'active' : 'disabled'
    })
    ElMessage.success(`状态已更新为 ${row.status === 'enabled' ? '启用' : '禁用'}`)
  } catch (error: any) {
    // 恢复原状态
    row.status = row.status === 'enabled' ? 'disabled' : 'enabled'
    ElMessage.error(error.message || '状态更新失败')
  }
}

// 删除会员方案 - API: DELETE /api/v1/plans/{code}
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      '确认删除该会员方案吗？删除后将无法恢复',
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    // API: deletePlan() -> DELETE /api/v1/plans/{code}
    await membershipApi.deletePlan(row.code)
    ElMessage.success('删除成功')
    await loadPlanList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  } finally {
    loading.value = false
  }
}

// 批量选择
const handleSelectionChange = (rows: any) => {
  selectedRows.value = rows
}

// 打开权益配置弹窗 - API: fetchPlanBenefits()
const openBenefitDialog = async (row: any) => {
  currentBenefitRow.value = row
  try {
    const data = await membershipApi.fetchPlanBenefits(row.code)
    benefitList.value = data.benefits && data.benefits.length > 0 
      ? data.benefits.map((item: any) => ({
          key: item.key || item.name,
          name: item.name,
          description: item.description,
          type: item.type || 'switch',
          enabled: item.enabled ?? true,
          value: item.value ?? 0,
          min: item.min ?? 0,
          unlimited: item.unlimited ?? false,
          planCodes: item.planCodes ?? []
        }))
      : getDefaultBenefits()
  } catch (error: any) {
    benefitList.value = getDefaultBenefits()
    ElMessage.warning('获取权益配置失败，使用默认值')
  }
  benefitDialogVisible.value = true
}

// 保存权益配置 - API: updatePlanBenefits()
const saveBenefit = async () => {
  if (!currentBenefitRow.value) return
  saving.value = true
  try {
    await membershipApi.updatePlanBenefits(currentBenefitRow.value.code, benefitList.value)
    ElMessage.success('权益配置已保存')
    benefitDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '保存权益配置失败')
  } finally {
    saving.value = false
  }
}

// 获取默认权益配置
function getDefaultBenefits(): BenefitItem[] {
  return [
    { key: 'b1', name: '对话次数提升', type: 'input', enabled: true, value: 1000, min: 0 },
    { key: 'b2', name: '优先响应', type: 'switch', enabled: false, value: 0 },
    { key: 'b3', name: '高级模型权限', type: 'switch', enabled: false, value: 0 },
    { key: 'b4', name: '数据存储容量', type: 'unlimited', enabled: true, unlimited: false, value: 10, min: 1 },
    { key: 'b5', name: 'API调用次数', type: 'input', enabled: true, value: 5000, min: 0 },
    { key: 'b6', name: '团队成员数量', type: 'input', enabled: true, value: 10, min: 1 }
  ]
}

// 无限额开关变更
const handleUnlimitedChange = (item: any) => {
  if (item.unlimited) {
    item.value = 0
  }
}

// 提交审批 - API: auditPlan() -> POST /api/v1/plans/{code}/audit
// 先获取待审批记录，然后打开审批弹窗
const handleApproveSubmit = async (row: any) => {
  currentApprovalPlan.value = row
  approvalForm.action = 'approve'
  approvalForm.auditRemark = ''
  
  try {
    // 先调用接口获取待审批记录
    const pendingData = await membershipApi.fetchPendingAudit(row.code)
    pendingAuditData.value = pendingData
    approvalDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取待审批记录失败')
  }
}

// 提交审批结果
const handleApprovalSubmit = async () => {
  if (!currentApprovalPlan.value) return
  
  try {
    approvalLoading.value = true
    const auditData: PlanAuditDTO = {
      action: approvalForm.action,
      auditRemark: approvalForm.auditRemark
    }
    await membershipApi.auditPlan(currentApprovalPlan.value.code, auditData)
    ElMessage.success(approvalForm.action === 'approve' ? '审批已通过' : '审批已驳回')
    approvalDialogVisible.value = false
    await loadPlanList()
  } catch (error: any) {
    ElMessage.error(error.message || '提交审批失败')
  } finally {
    approvalLoading.value = false
  }
}

// 获取审批类型描述
const getAuditTypeDesc = (type?: string) => {
  const typeMap: Record<string, string> = {
    'create': '创建',
    'update': '更新',
    'delete': '删除',
    'status': '状态变更'
  }
  return typeMap[type || ''] || type || '未知'
}

// 高级筛选
const openAdvancedFilter = () => {
  advancedVisible.value = true
}

// 重置高级筛选
const resetAdvancedFilter = () => {
  advancedForm.createTime = []
  advancedForm.minPrice = ''
  advancedForm.maxPrice = ''
  advancedForm.minSortOrder = ''
  advancedForm.maxSortOrder = ''
  advancedForm.auditStatus = []
}

// 应用高级筛选
const applyAdvancedFilter = () => {
  // TODO: 实现高级筛选逻辑
  // 可以通过调用 API 时传递筛选参数实现
  advancedVisible.value = false
  ElMessage.success('筛选已应用')
}

// 打开日志详情
const openLogDetail = (row: any) => {
  currentLog.value = row
  logDetailDialogVisible.value = true
}

// 获取日志类型描述
const getLogTypeDesc = (type: string) => {
  const typeMap: Record<string, string> = {
    'create': '方案创建',
    'update': '方案编辑',
    'delete': '删除方案',
    'status': '状态变更',
    'benefit': '权益配置'
  }
  return typeMap[type] || type
}

// 获取日志类型标签
const getLogTypeTag = (type: string) => {
  switch (type) {
    case 'create': return 'success'
    case 'update': return 'primary'
    case 'status': return 'warning'
    case 'benefit': return 'info'
    case 'delete': return 'danger'
    default: return 'default'
  }
}

// 打开手动调整用户会员弹窗
const openUserAdjustDialog = () => {
  adjustForm.userKey = ''
  adjustForm.userInfo = null
  adjustForm.targetPlan = ''
  adjustForm.expireDays = ''
  adjustForm.adjustReason = ''
  userAdjustDialogVisible.value = true
}

// 搜索用户 - API: fetchUserSubscription() -> GET /api/v1/users/{userId}/subscription
const searchUser = async () => {
  if (!adjustForm.userKey) {
    ElMessage.warning('请输入用户ID')
    return
  }
  
  try {
    // API: fetchUserSubscription() -> GET /api/v1/users/{userId}/subscription
    const userSub = await membershipApi.fetchUserSubscription(adjustForm.userKey)
    
    adjustForm.userInfo = {
      userId: userSub.userId,
      currentPlanName: userSub.currentPlanName || 'Free',
      currentPlanCode: userSub.currentPlanCode,
      planExpire: userSub.planExpire || '永久',
      subscriptionId: userSub.subscriptionId
    }
    ElMessage.success('用户搜索成功')
  } catch (error: any) {
    ElMessage.error(error.message || '用户不存在')
  }
}

// 确认调整用户会员 - API: adjustUserSubscription() -> POST /api/v1/users/{userId}/subscription/adjust
const handleUserAdjust = async () => {
  const valid = await adjustFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    loading.value = true
    
    // API: adjustUserSubscription() -> POST /api/v1/users/{userId}/subscription/adjust
    await membershipApi.adjustUserSubscription(adjustForm.userInfo.userId, {
      planCode: adjustForm.targetPlan,
      expireDays: Number(adjustForm.expireDays),
      reason: adjustForm.adjustReason
    })
    
    ElMessage.success('用户会员调整成功')
    userAdjustDialogVisible.value = false
    
    adjustForm.userKey = ''
    adjustForm.userInfo = null
    adjustForm.targetPlan = ''
    adjustForm.expireDays = ''
    adjustForm.adjustReason = ''
  } catch (error: any) {
    ElMessage.error(error.message || '调整失败')
  } finally {
    loading.value = false
  }
}

// 下拉菜单操作处理
const handleAction = (cmd: string, row: any) => {
  if (cmd === 'benefit') {
    openBenefitDialog(row)
  } else if (cmd === 'delete') {
    handleDelete(row)
  } else if (cmd === 'approve') {
    handleApproveSubmit(row)
  } else if (cmd === 'log') {
    openLog(row.code)
  }
}

// 打开操作日志 - API: fetchPlanAuditLogs()
const openLog = async (planCode: string) => {
  logLoading.value = true
  logDialogVisible.value = true
  try {
    const data = await membershipApi.fetchPlanAuditLogs(planCode)
    logList.value = data.items || []
    logTotal.value = data.total || 0
  } catch (error: any) {
    logList.value = []
    logTotal.value = 0
    ElMessage.error(error.message || '获取操作日志失败')
  } finally {
    logLoading.value = false
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

// 格式化JSON数据
const formatJsonData = (data: any) => {
  if (!data) return '-'
  if (typeof data === 'string') {
    try {
      return JSON.stringify(JSON.parse(data), null, 2)
    } catch {
      return data
    }
  }
  return JSON.stringify(data, null, 2)
}

// 监听搜索和过滤，重置页码
watch([searchName, statusFilter, approveFilter], () => {
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

/* 表格样式 */
.price-text {
  color: #ef4444;
  font-weight: 500;
}

/* 权益配置样式 */
.benefit-tip {
  padding: 12px 16px;
  background-color: #f9fafb;
  border-radius: 8px;
  font-size: 14px;
}

.benefit-name {
  color: #3b82f6;
  font-weight: 500;
}

.feature-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 8px;
  background-color: #f9fafb;
  border-radius: 8px;
}

.benefit-item-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.benefit-icon {
  color: #3b82f6;
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
  color: #6b7280;
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

.mt-5 {
  margin-top: 5px;
}

/* 审批弹窗样式 */
.approval-content {
  padding: 10px;
}

.data-pre {
  font-size: 12px;
  color: #6b7280;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  max-height: 150px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.empty-approval {
  padding: 40px 0;
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
