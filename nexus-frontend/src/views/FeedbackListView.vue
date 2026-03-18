<template>
  <el-card class="page-container" shadow="hover">
    <!-- 顶部 -->
    <div class="header">
      <div class="header-left">
        <div class="title">用户反馈管理</div>
        <div class="subtitle">处理用户提交的各类反馈与建议</div>
      </div>
      <el-tag type="danger" size="large">P0 紧急</el-tag>
    </div>

    <!-- 数据概览（调整到操作栏上方） -->
    <div class="stats-card mb-20">
      <div class="stat-item">
        <div class="stat-label">总反馈数</div>
        <div class="stat-value">{{ list.length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">待处理</div>
        <div class="stat-value warning">{{ list.filter(item => item.status === 'pending').length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">处理中</div>
        <div class="stat-value primary">{{ list.filter(item => item.status === 'processing').length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">已解决</div>
        <div class="stat-value success">{{ list.filter(item => item.status === 'resolved').length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">已关闭</div>
        <div class="stat-value default">{{ list.filter(item => item.status === 'closed').length }}</div>
      </div>
    </div>

    <!-- 操作栏（补充产品筛选、类型筛选、批量操作启用） -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户/内容/标题"
          clearable
          style="width: 280px"
          class="mr-10"
          prefix-icon="el-icon-search"
        />

        <el-select v-model="productFilter" placeholder="来源产品" style="width: 160px" class="mr-10">
          <el-option label="全部" value="" />
          <el-option label="CheersAI Desktop" value="desktop" />
          <el-option label="CheersAI Vault" value="vault" />
          <el-option label="CheersAI Writer" value="writer" />
        </el-select>

        <el-select v-model="typeFilter" placeholder="反馈类型" style="width: 160px" class="mr-10">
          <el-option label="全部" value="" />
          <el-option label="Bug反馈" value="bug" />
          <el-option label="功能建议" value="feature" />
          <el-option label="咨询问题" value="question" />
          <el-option label="其他反馈" value="other" />
        </el-select>

        <el-select v-model="statusFilter" placeholder="处理状态" style="width: 160px" class="mr-10">
          <el-option label="全部" value="" />
          <el-option label="待处理" value="pending" />
          <el-option label="处理中" value="processing" />
          <el-option label="已解决" value="resolved" />
          <el-option label="已关闭" value="closed" />
        </el-select>

        <el-button type="primary" @click="fetchList" icon="el-icon-refresh">查询</el-button>
      </div>

      <div class="toolbar-right">
        <!-- 启用批量操作（P0核心功能） -->
        <el-button type="success" @click="batchMarkResolved" icon="el-icon-check" :disabled="selectedRows.length === 0" class="mr-5">
          批量标记已处理
        </el-button>
        <el-button type="info" @click="openAdvancedFilter" icon="el-icon-s-tools" class="mr-5">高级筛选</el-button>
        <el-button type="warning" @click="exportData" icon="el-icon-download">导出数据</el-button>
      </div>
    </div>

    <!-- 表格（补充产品、标题、优先级、处理人列） -->
    <el-table
      :data="filteredList"
      v-loading="loading"
      style="width: 100%"
      border
      stripe
      :empty-text="filteredList.length === 0 ? '暂无匹配的反馈数据' : ''"
      @row-click="handleRowClick"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="60" align="center" />
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="productName" label="来源产品" width="160" align="center" />
      <el-table-column prop="title" label="反馈标题" min-width="180" />
      <el-table-column prop="user" label="反馈用户" width="120" align="center" />
      <el-table-column prop="content" label="反馈内容" min-width="300">
        <template #default="scope">
          <div class="content-cell" :title="scope.row.content">{{ scope.row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="反馈类型" width="120" align="center">
        <template #default="scope">
          <el-tag size="small" :type="getTypeTag(scope.row.type)">
            {{ scope.row.type === 'bug' ? 'Bug反馈' : scope.row.type === 'feature' ? '功能建议' : scope.row.type === 'question' ? '咨询问题' : '其他反馈' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="120" align="center">
        <template #default="scope">
          <el-tag size="small" :type="getPriorityTag(scope.row.priority)">
            {{ scope.row.priority === 'low' ? '低' : scope.row.priority === 'medium' ? '中' : scope.row.priority === 'high' ? '高' : '紧急' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="assignee" label="处理人" width="120" align="center" />
      <el-table-column prop="createTime" label="提交时间" width="180" align="center" />

      <el-table-column label="处理状态" width="140" align="center">
        <template #default="scope">
          <el-tag 
            size="small"
            :type="getStatusTag(scope.row.status)"
          >
            {{ 
              scope.row.status === 'pending' ? '待处理' : 
              scope.row.status === 'processing' ? '处理中' : 
              scope.row.status === 'resolved' ? '已解决' : '已关闭' 
            }}
          </el-tag>
        </template>
      </el-table-column>

      <!-- 优化后的操作列（补充分配处理人、标记处理中/已关闭） -->
      <el-table-column label="操作" width="220" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" icon="el-icon-edit" @click="openReply(scope.row)">
            回复
          </el-button>
          <el-dropdown @command="(cmd) => handleAction(cmd, scope.row)">
            <el-button size="small" type="text" icon="el-icon-more">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="detail" icon="el-icon-view">查看详情</el-dropdown-item>
                <el-dropdown-item command="assign" icon="el-icon-user" :disabled="scope.row.status === 'resolved' || scope.row.status === 'closed'">分配处理人</el-dropdown-item>
                <el-dropdown-item command="processing" icon="el-icon-loading" :disabled="scope.row.status === 'processing' || scope.row.status === 'resolved' || scope.row.status === 'closed'">标记处理中</el-dropdown-item>
                <el-dropdown-item command="resolve" icon="el-icon-check-circle" :disabled="scope.row.status === 'resolved' || scope.row.status === 'closed'">标记已解决</el-dropdown-item>
                <el-dropdown-item command="close" icon="el-icon-close" :disabled="scope.row.status === 'closed'">标记已关闭</el-dropdown-item>
              </el-dropdown-menu>
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
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredList.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 详情弹窗（补充标题、产品、优先级、附件、处理记录） -->
    <el-dialog v-model="detailVisible" title="反馈详情" width="700px" center>
      <div class="detail-content">
        <div class="detail-item">
          <span class="detail-label">反馈标题：</span>
          <span class="detail-value">{{ currentRow.title }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">来源产品：</span>
          <span class="detail-value">{{ currentRow.productName }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">反馈用户：</span>
          <span class="detail-value">{{ currentRow.user }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">反馈类型：</span>
          <span class="detail-value">
            <el-tag size="small" :type="getTypeTag(currentRow.type)">
              {{ currentRow.type === 'bug' ? 'Bug反馈' : currentRow.type === 'feature' ? '功能建议' : currentRow.type === 'question' ? '咨询问题' : '其他反馈' }}
            </el-tag>
          </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">优先级：</span>
          <span class="detail-value">
            <el-tag size="small" :type="getPriorityTag(currentRow.priority)">
              {{ currentRow.priority === 'low' ? '低' : currentRow.priority === 'medium' ? '中' : currentRow.priority === 'high' ? '高' : '紧急' }}
            </el-tag>
          </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">处理人：</span>
          <span class="detail-value">{{ currentRow.assignee || '未分配' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">提交时间：</span>
          <span class="detail-value">{{ currentRow.createTime }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">处理状态：</span>
          <span class="detail-value">
            <el-tag 
              size="small"
              :type="getStatusTag(currentRow.status)"
            >
              {{ 
                currentRow.status === 'pending' ? '待处理' : 
                currentRow.status === 'processing' ? '处理中' : 
                currentRow.status === 'resolved' ? '已解决' : '已关闭' 
              }}
            </el-tag>
          </span>
        </div>
        <div class="detail-item content-item">
          <span class="detail-label">反馈内容：</span>
          <div class="detail-value content-text">{{ currentRow.content }}</div>
        </div>
        <!-- 附件展示 -->
        <div class="detail-item" v-if="currentRow.attachments && currentRow.attachments.length > 0">
          <span class="detail-label">相关附件：</span>
          <div class="detail-value">
            <div class="attachment-list">
              <el-image
                v-for="(img, idx) in currentRow.attachments"
                :key="idx"
                :src="img"
                :preview-src-list="currentRow.attachments"
                style="width: 100px; height: 100px; margin-right: 10px; cursor: pointer"
                fit="cover"
              />
            </div>
          </div>
        </div>
        <!-- 处理记录 -->
        <div class="detail-item" v-if="currentRow.processRecords && currentRow.processRecords.length > 0">
          <span class="detail-label">处理记录：</span>
          <div class="detail-value">
            <el-timeline>
              <el-timeline-item
                v-for="(record, idx) in currentRow.processRecords"
                :key="idx"
                :timestamp="record.time"
                :type="record.type === 'assign' ? 'primary' : record.type === 'status' ? 'success' : record.type === 'reply' ? 'info' : 'warning'"
                :icon="record.type === 'assign' ? 'el-icon-user' : record.type === 'status' ? 'el-icon-check' : record.type === 'reply' ? 'el-icon-message' : 'el-icon-edit'"
              >
                <div class="timeline-content">
                  <div class="timeline-operator">{{ record.operator }}：</div>
                  <div class="timeline-desc">{{ record.desc }}</div>
                  <div v-if="record.remark" class="timeline-remark">备注：{{ record.remark }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button 
          type="primary" 
          @click="handleDetailToReply"
          v-if="currentRow.status === 'pending' || currentRow.status === 'processing'"
          class="ml-10"
        >
          去回复
        </el-button>
        <el-button 
          type="info" 
          @click="openAssignDialog(currentRow)"
          v-if="currentRow.status === 'pending' || currentRow.status === 'processing'"
          class="ml-10"
        >
          分配处理人
        </el-button>
      </template>
    </el-dialog>

    <!-- 回复弹窗（补充处理备注、回复渠道选择） -->
    <el-dialog v-model="replyVisible" title="回复用户" width="700px" center>
      <div class="reply-tip mb-10">
        回复对象：<el-tag size="small">{{ currentRow.user }}</el-tag>
        <span class="ml-10">反馈标题：{{ currentRow.title }}</span>
      </div>
      <el-form :model="replyForm" label-width="80px" :rules="replyRules" ref="replyFormRef">
        <el-form-item label="回复渠道" prop="channel">
          <el-select v-model="replyForm.channel" placeholder="请选择回复渠道">
            <el-option label="站内信" value="inbox" />
            <el-option label="邮件" value="email" />
            <el-option label="双渠道" value="both" />
          </el-select>
        </el-form-item>
        <el-form-item label="回复内容" prop="content">
          <el-input
            type="textarea"
            v-model="replyForm.content"
            placeholder="请输入回复内容（将发送给用户）"
            rows="6"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="处理备注" prop="remark">
          <el-input
            type="textarea"
            v-model="replyForm.remark"
            placeholder="请输入内部处理备注（仅团队可见）"
            rows="3"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="处理状态">
          <el-radio-group v-model="replyForm.status">
            <el-radio label="processing">标记为处理中</el-radio>
            <el-radio label="resolved">标记为已解决</el-radio>
            <el-radio label="pending">保持待处理</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" icon="el-icon-send">发送回复</el-button>
      </template>
    </el-dialog>

    <!-- 分配处理人弹窗 -->
    <el-dialog v-model="assignVisible" title="分配处理人" width="500px" center>
      <el-form :model="assignForm" label-width="80px" :rules="assignRules" ref="assignFormRef">
        <el-form-item label="处理人" prop="assignee">
          <el-select v-model="assignForm.assignee" placeholder="请选择处理人">
            <el-option label="运营小王" value="operator_xiaowang" />
            <el-option label="产品老李" value="pm_laoli" />
            <el-option label="技术小张" value="dev_xiaozhang" />
            <el-option label="客服小刘" value="support_xiaoliu" />
          </el-select>
        </el-form-item>
        <el-form-item label="分配备注" prop="remark">
          <el-input
            type="textarea"
            v-model="assignForm.remark"
            placeholder="请输入分配备注（如：需技术排查）"
            rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign" icon="el-icon-check">确认分配</el-button>
      </template>
    </el-dialog>

    <!-- 高级筛选弹窗 -->
    <el-dialog v-model="advancedFilterVisible" title="高级筛选" width="600px" center>
      <el-form :model="advancedForm" label-width="100px">
        <el-form-item label="提交时间">
          <el-date-picker
            v-model="advancedForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="advancedForm.priority" placeholder="请选择优先级" multiple>
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
            <el-option label="紧急" value="urgent" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理人">
          <el-select v-model="advancedForm.assignee" placeholder="请选择处理人" multiple>
            <el-option label="运营小王" value="operator_xiaowang" />
            <el-option label="产品老李" value="pm_laoli" />
            <el-option label="技术小张" value="dev_xiaozhang" />
            <el-option label="客服小刘" value="support_xiaoliu" />
            <el-option label="未分配" value="" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="resetAdvancedFilter">重置</el-button>
        <el-button type="primary" @click="confirmAdvancedFilter">确认筛选</el-button>
      </template>
    </el-dialog>

    <!-- 关联工单弹窗 -->
    <el-dialog v-model="ticketVisible" title="关联工单" width="500px" center>
      <el-form :model="ticketForm" label-width="80px" :rules="ticketRules" ref="ticketFormRef">
        <el-form-item label="工单编号" prop="ticketNo">
          <el-input
            v-model="ticketForm.ticketNo"
            placeholder="请输入外部工单编号（如JIRA编号）"
            clearable
          />
        </el-form-item>
        <el-form-item label="工单链接" prop="ticketUrl">
          <el-input
            v-model="ticketForm.ticketUrl"
            placeholder="请输入工单链接"
            clearable
          />
        </el-form-item>
        <el-form-item label="关联备注" prop="remark">
          <el-input
            type="textarea"
            v-model="ticketForm.remark"
            placeholder="请输入关联备注"
            rows="2"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="ticketVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTicket" icon="el-icon-link">确认关联</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, ElTimeline, ElTimelineItem, ElImage } from 'element-plus'

// loading
const loading = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 查询条件（补充产品、类型、高级筛选条件）
const searchKeyword = ref('')
const productFilter = ref('')
const typeFilter = ref('')
const statusFilter = ref('')
const advancedForm = ref({
  dateRange: [] as Date[],
  priority: [] as string[],
  assignee: [] as string[]
})

// 选中行（批量操作）
const selectedRows = ref<any[]>([])

// mock 数据（补充产品、标题、优先级、处理人、附件、处理记录）
const list = ref([
  {
    id: 'F001',
    productId: 'desktop',
    productName: 'CheersAI Desktop',
    title: '页面加载速度慢',
    user: '张三',
    content: '页面加载太慢，尤其是在网络状况不好的时候，首页加载需要5秒以上，希望能优化一下加载速度和资源加载策略',
    type: 'bug',
    priority: 'high',
    status: 'pending',
    assignee: '',
    attachments: [
      'https://picsum.photos/800/600?random=1',
      'https://picsum.photos/800/600?random=2'
    ],
    createTime: '2026-03-18 10:00',
    processRecords: []
  },
  {
    id: 'F002',
    productId: 'writer',
    productName: 'CheersAI Writer',
    title: '建议增加夜间模式',
    user: '李四',
    content: '建议增加夜间模式，晚上使用的时候太亮了，对眼睛不太友好，希望能尽快上线这个功能',
    type: 'feature',
    priority: 'medium',
    status: 'resolved',
    assignee: '产品老李',
    attachments: [],
    createTime: '2026-03-17 14:20',
    processRecords: [
      {
        operator: '客服小刘',
        type: 'assign',
        desc: '分配给产品老李处理',
        remark: '功能建议需产品评估',
        time: '2026-03-17 15:00'
      },
      {
        operator: '产品老李',
        type: 'status',
        desc: '标记为已解决',
        remark: '已纳入V2.3版本规划',
        time: '2026-03-18 09:30'
      },
      {
        operator: '客服小刘',
        type: 'reply',
        desc: '回复用户：已收到您的建议，夜间模式将在V2.3版本上线，预计4月中旬发布',
        remark: '用户表示满意',
        time: '2026-03-18 10:15'
      }
    ]
  },
  {
    id: 'F003',
    productId: 'vault',
    productName: 'CheersAI Vault',
    title: '数据导出失败',
    user: '王五',
    content: '尝试导出存储的数据时，点击导出按钮后无响应，控制台报错403，已截图上传',
    type: 'bug',
    priority: 'urgent',
    status: 'processing',
    assignee: '技术小张',
    attachments: [
      'https://picsum.photos/800/600?random=3'
    ],
    createTime: '2026-03-18 11:30',
    processRecords: [
      {
        operator: '运营小王',
        type: 'assign',
        desc: '分配给技术小张处理',
        remark: '紧急Bug，需优先修复',
        time: '2026-03-18 11:45'
      },
      {
        operator: '技术小张',
        type: 'status',
        desc: '标记为处理中',
        remark: '正在排查权限问题',
        time: '2026-03-18 14:20'
      }
    ]
  }
])

// 过滤（补充产品、类型、高级筛选条件）
const filteredList = computed(() => {
  return list.value.filter(item => {
    // 基础筛选
    const keywordMatch = !searchKeyword.value || 
      item.user.includes(searchKeyword.value) || 
      item.content.includes(searchKeyword.value) || 
      item.title.includes(searchKeyword.value)
    const productMatch = !productFilter.value || item.productId === productFilter.value
    const typeMatch = !typeFilter.value || item.type === typeFilter.value
    const statusMatch = !statusFilter.value || item.status === statusFilter.value

    // 高级筛选
    const dateMatch = !advancedForm.value.dateRange.length || (
      new Date(item.createTime) >= new Date(advancedForm.value.dateRange[0]) &&
      new Date(item.createTime) <= new Date(advancedForm.value.dateRange[1])
    )
    const priorityMatch = !advancedForm.value.priority.length || advancedForm.value.priority.includes(item.priority)
    const assigneeMatch = !advancedForm.value.assignee.length || (
      advancedForm.value.assignee.includes('') ? !item.assignee : 
      advancedForm.value.assignee.includes(item.assignee)
    )

    return keywordMatch && productMatch && typeMatch && statusMatch && dateMatch && priorityMatch && assigneeMatch
  })
})

// 分页后列表
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

// 获取列表
const fetchList = () => {
  loading.value = true

  // TODO: 调用后端接口获取反馈列表（/api/v1/feedbacks）
  // 参数：productId、type、status、keyword、startDate、endDate、priority、assignee、page、pageSize
  setTimeout(() => {
    loading.value = false
  }, 500)
}

// 当前选中行
const currentRow = ref<any>({})

// 弹窗控制
const detailVisible = ref(false)
const replyVisible = ref(false)
const assignVisible = ref(false)
const advancedFilterVisible = ref(false)
const ticketVisible = ref(false)

// 回复表单（补充渠道、备注、状态选择）
const replyFormRef = ref<any>(null)
const replyForm = ref({
  channel: 'inbox',
  content: '',
  remark: '',
  status: 'resolved'
})
const replyRules = ref({
  channel: [{ required: true, message: '请选择回复渠道', trigger: 'blur' }],
  content: [{ required: true, message: '请输入回复内容', trigger: 'blur' }],
  remark: [{ required: false, message: '请输入处理备注', trigger: 'blur' }]
})

// 分配处理人表单
const assignFormRef = ref<any>(null)
const assignForm = ref({
  assignee: '',
  remark: ''
})
const assignRules = ref({
  assignee: [{ required: true, message: '请选择处理人', trigger: 'blur' }],
  remark: [{ required: true, message: '请输入分配备注', trigger: 'blur' }]
})

// 关联工单表单
const ticketFormRef = ref<any>(null)
const ticketForm = ref({
  ticketNo: '',
  ticketUrl: '',
  remark: ''
})
const ticketRules = ref({
  ticketNo: [{ required: true, message: '请输入工单编号', trigger: 'blur' }],
  ticketUrl: [{ required: false, message: '请输入工单链接', trigger: 'blur' }],
  remark: [{ required: false, message: '请输入关联备注', trigger: 'blur' }]
})

// 打开详情
const openDetail = (row: any) => {
  currentRow.value = row
  detailVisible.value = true
}

// 打开回复
const openReply = (row: any) => {
  currentRow.value = row
  replyForm.value = {
    channel: 'inbox',
    content: '',
    remark: '',
    status: row.status === 'pending' ? 'resolved' : row.status
  }
  replyVisible.value = true
}

// 打开分配处理人
const openAssignDialog = (row: any) => {
  currentRow.value = row
  assignForm.value = {
    assignee: row.assignee || '',
    remark: ''
  }
  assignVisible.value = true
}

// 打开高级筛选
const openAdvancedFilter = () => {
  advancedFilterVisible.value = true
}

// 打开关联工单
const openTicketDialog = (row: any) => {
  currentRow.value = row
  ticketForm.value = {
    ticketNo: '',
    ticketUrl: '',
    remark: ''
  }
  ticketVisible.value = true
}

// 提交回复（US-009核心功能）
const submitReply = () => {
  replyFormRef.value.validate((valid: boolean) => {
    if (!valid) return

    // 构造处理记录
    const processRecord = {
      operator: '客服小刘', // 实际从登录信息获取
      type: 'reply',
      desc: `回复用户：${replyForm.value.content}`,
      remark: replyForm.value.remark || '无',
      time: new Date().toLocaleString().replace(/\//g, '-')
    }

    // 更新当前行状态和处理记录
    currentRow.value.status = replyForm.value.status
    if (!currentRow.value.processRecords) currentRow.value.processRecords = []
    currentRow.value.processRecords.push(processRecord)

    // 同步更新列表数据
    const index = list.value.findIndex(item => item.id === currentRow.value.id)
    if (index !== -1) {
      list.value[index] = { ...currentRow.value }
    }

    // TODO: 调用回复接口（/api/v1/feedbacks/:id/reply）
    // 参数：channel、content、remark、status
    ElMessage.success('回复已发送并保存处理记录')
    replyVisible.value = false

    // 记录审计日志
    recordAuditLog('reply', currentRow.value)
  })
}

// 提交分配处理人（US-008核心功能）
const submitAssign = () => {
  assignFormRef.value.validate((valid: boolean) => {
    const assigneeName = assignForm.value.assignee === 'operator_xiaowang' ? '运营小王' :
      assignForm.value.assignee === 'pm_laoli' ? '产品老李' :
      assignForm.value.assignee === 'dev_xiaozhang' ? '技术小张' : '客服小刘'

    if (!valid) return

    // 构造处理记录
    const processRecord = {
      operator: '客服小刘', // 实际从登录信息获取
      type: 'assign',
      desc: `分配给${assigneeName}处理`,
      remark: assignForm.value.remark,
      time: new Date().toLocaleString().replace(/\//g, '-')
    }

    // 更新当前行处理人和处理记录
    currentRow.value.assignee = assigneeName
    if (!currentRow.value.processRecords) currentRow.value.processRecords = []
    currentRow.value.processRecords.push(processRecord)

    // 同步更新列表数据
    const index = list.value.findIndex(item => item.id === currentRow.value.id)
    if (index !== -1) {
      list.value[index] = { ...currentRow.value }
    }

    // TODO: 调用分配处理人接口（/api/v1/feedbacks/:id/assign）
    // 参数：assigneeId、remark
    ElMessage.success(`已成功分配给${assigneeName}`)
    assignVisible.value = false

    // 记录审计日志
    recordAuditLog('assign', currentRow.value)
  })
}

// 提交关联工单（US-009核心功能）
const submitTicket = () => {
  ticketFormRef.value.validate((valid: boolean) => {
    if (!valid) return

    // 构造处理记录
    const processRecord = {
      operator: '客服小刘', // 实际从登录信息获取
      type: 'ticket',
      desc: `关联工单：${ticketForm.value.ticketNo}${ticketForm.value.ticketUrl ? `（链接：${ticketForm.value.ticketUrl}）` : ''}`,
      remark: ticketForm.value.remark || '无',
      time: new Date().toLocaleString().replace(/\//g, '-')
    }

    // 更新当前行处理记录
    if (!currentRow.value.processRecords) currentRow.value.processRecords = []
    currentRow.value.processRecords.push(processRecord)

    // 同步更新列表数据
    const index = list.value.findIndex(item => item.id === currentRow.value.id)
    if (index !== -1) {
      list.value[index] = { ...currentRow.value }
    }

    // TODO: 调用关联工单接口（/api/v1/feedbacks/:id/ticket）
    // 参数：ticketNo、ticketUrl、remark
    ElMessage.success('工单关联成功')
    ticketVisible.value = false

    // 记录审计日志
    recordAuditLog('ticket', currentRow.value)
  })
}

// 标记状态变更（待处理/处理中/已解决/已关闭）
const markStatus = (row: any, status: string) => {
  const statusMap = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已解决',
    closed: '已关闭'
  }
  const operator = '客服小刘' // 实际从登录信息获取

  // 构造处理记录
  const processRecord = {
    operator,
    type: 'status',
    desc: `标记为${statusMap[status]}`,
    remark: status === 'resolved' ? '问题已解决' : status === 'processing' ? '正在处理中' : status === 'closed' ? '反馈已关闭' : '待进一步处理',
    time: new Date().toLocaleString().replace(/\//g, '-')
  }

  // 更新当前行状态和处理记录
  row.status = status
  if (!row.processRecords) row.processRecords = []
  row.processRecords.push(processRecord)

  // TODO: 调用状态更新接口（/api/v1/feedbacks/:id）
  // 参数：status、remark
  ElMessage.success(`已标记为${statusMap[status]}`)

  // 记录审计日志
  recordAuditLog('status', row)
}

// 批量标记已处理
const batchMarkResolved = () => {
  ElMessageBox.confirm(
    `确认将选中的${selectedRows.value.length}条反馈标记为已解决？`,
    '批量操作确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    selectedRows.value.forEach(row => {
      markStatus(row, 'resolved')
    })
    selectedRows.value = []
  })
}

// 导出数据（US-008验收标准）
const exportData = () => {
  loading.value = true

  // TODO: 调用导出接口（/api/v1/feedbacks/export）
  // 参数：当前所有筛选条件
  setTimeout(() => {
    loading.value = false
    ElMessage.success('导出成功，已生成CSV文件')
  }, 800)
}

// 行点击事件
const handleRowClick = (row: any) => {
  openDetail(row)
}

// 批量选择事件
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

// 下拉菜单操作处理
const handleAction = (cmd: string, row: any) => {
  switch (cmd) {
    case 'detail':
      openDetail(row)
      break
    case 'assign':
      openAssignDialog(row)
      break
    case 'processing':
      markStatus(row, 'processing')
      break
    case 'resolve':
      markStatus(row, 'resolved')
      break
    case 'close':
      markStatus(row, 'closed')
      break
    case 'ticket':
      openTicketDialog(row)
      break
  }
}

// 处理分页大小改变
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

// 处理页码改变
const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

// 重置高级筛选
const resetAdvancedFilter = () => {
  advancedForm.value = {
    dateRange: [],
    priority: [],
    assignee: []
  }
}

// 确认高级筛选
const confirmAdvancedFilter = () => {
  advancedFilterVisible.value = false
  fetchList() // 重新查询
}

// 从详情跳转到回复
const handleDetailToReply = () => {
  detailVisible.value = false
  openReply(currentRow.value)
}

// 辅助函数：获取类型标签颜色
const getTypeTag = (type: string) => {
  switch (type) {
    case 'bug': return 'danger'
    case 'feature': return 'success'
    case 'question': return 'info'
    case 'other': return 'default'
    default: return 'default'
  }
}

// 辅助函数：获取优先级标签颜色
const getPriorityTag = (priority: string) => {
  switch (priority) {
    case 'low': return 'default'
    case 'medium': return 'warning'
    case 'high': return 'primary'
    case 'urgent': return 'danger'
    default: return 'default'
  }
}

// 辅助函数：获取状态标签颜色
const getStatusTag = (status: string) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'processing': return 'primary'
    case 'resolved': return 'success'
    case 'closed': return 'default'
    default: return 'default'
  }
}

// 记录审计日志（全域合规要求）
const recordAuditLog = (actionType: string, row: any) => {
  const actionMap = {
    reply: '回复反馈',
    assign: '分配处理人',
    status: '变更处理状态',
    ticket: '关联工单'
  }
  // TODO: 调用审计日志记录接口（/api/v1/audit-logs）
  console.log('记录审计日志：', {
    operator: '客服小刘',
    action: actionMap[actionType],
    target: `反馈${row.id}（${row.title}）`,
    time: new Date().toLocaleString(),
    ip: '192.168.1.100' // 实际从请求头获取
  })
}

// 监听筛选条件变化，重置页码
watch([searchKeyword, productFilter, typeFilter, statusFilter], () => {
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

.stat-value.warning {
  color: #f59e0b;
}

.stat-value.success {
  color: #10b981;
}

.stat-value.primary {
  color: #409eff;
}

.stat-value.default {
  color: #6b7280;
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

/* 表格内容单元格 */
.content-cell {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 300px;
}

/* 详情弹窗样式 */
.detail-content {
  padding: 8px 0;
}

.detail-item {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.detail-label {
  width: 80px;
  font-weight: 500;
  color: #4b5563;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #1f2937;
}

.content-item {
  align-items: flex-start;
}

.content-text {
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

/* 附件列表样式 */
.attachment-list {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

/* 处理记录时间线样式 */
:deep(.el-timeline) {
  margin-top: 8px;
}

.timeline-content {
  padding: 8px 0;
}

.timeline-operator {
  font-weight: 500;
  color: #4b5563;
}

.timeline-desc {
  margin: 4px 0;
  color: #1f2937;
}

.timeline-remark {
  font-size: 12px;
  color: #6b7280;
  background-color: #f9fafb;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
}

/* 回复弹窗提示 */
.reply-tip {
  padding: 8px 12px;
  background-color: #f3f4f6;
  border-radius: 4px;
  font-size: 14px;
  color: #4b5563;
}

/* 通用间距类 */
.mr-5 {
  margin-right: 5px;
}

.mr-10 {
  margin-right: 10px;
}

.mb-10 {
  margin-bottom: 10px;
}

.mb-20 {
  margin-bottom: 20px;
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

/* 操作列下拉菜单样式优化 */
:deep(.el-dropdown-link) {
  cursor: pointer;
  color: #409eff;
}

/* 分页容器 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>