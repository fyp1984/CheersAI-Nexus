<template>
  <el-card class="page-container">
    <!-- 顶部 -->
    <div class="header">
      <div class="title">产品管理</div>
      <el-tag type="danger">P0</el-tag>
    </div>

    <!-- 操作栏（补充高级筛选、批量操作启用） -->
    <div class="toolbar">
      <el-input
        v-model="searchName"
        placeholder="搜索产品名称/代码"
        clearable
        class="mr-10"
        style="width: 240px"
      />

      <el-select v-model="statusFilter" placeholder="状态" style="width: 140px" class="mr-10">
        <el-option label="全部" value="" />
        <el-option label="启用" value="active" />
        <el-option label="禁用" value="inactive" />
        <el-option label="已废弃" value="deprecated" />
      </el-select>

      <el-button type="primary" @click="openDialog()">➕ 新建产品</el-button>

      <!-- 启用 P1 功能 -->
      <el-button type="danger" @click="batchDelete" icon="el-icon-delete" :disabled="selectedRows.length === 0" class="ml-10">批量删除</el-button>
      <el-button type="info" @click="openAdvancedFilter" icon="el-icon-s-tools" class="ml-10">高级筛选</el-button>
      <el-button type="warning" @click="openLogDialog" icon="el-icon-document" class="ml-10">操作日志</el-button>
    </div>

    <!-- 表格（补充产品代码、图标预览、状态标签） -->
    <el-table 
      :data="paginatedList" 
      v-loading="loading" 
      style="width: 100%"
      @selection-change="handleSelectionChange"
      border
      stripe
    >
      <el-table-column type="selection" width="60" align="center" />
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="name" label="产品名称" min-width="180" />
      <el-table-column prop="code" label="产品代码" width="140" align="center" />
      <el-table-column label="产品图标" width="80" align="center">
        <template #default="scope">
          <el-image
            :src="scope.row.iconUrl || defaultIcon"
            style="width: 36px; height: 36px; border-radius: 4px"
            fit="cover"
          />
        </template>
      </el-table-column>
      <el-table-column prop="currentVersion" label="当前版本" width="120" align="center" />
      <el-table-column label="状态" width="120" align="center">
        <template #default="scope">
          <el-tag 
            :type="getStatusTag(scope.row.status)"
            size="small"
          >
            {{ 
              scope.row.status === 'active' ? '启用' : 
              scope.row.status === 'inactive' ? '禁用' : '已废弃' 
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" align="center" />

      <el-table-column label="操作" width="320" align="center">
        <template #default="scope">
          <el-button size="small" @click="openDialog(scope.row)">编辑</el-button>
          <el-button size="small" @click="openVersionDialog(scope.row)">版本管理</el-button>
          <el-button size="small" @click="openFeatureDialog(scope.row)">功能配置</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页器 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[8, 16, 24, 32]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredList.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新建/编辑弹窗（补充核心字段） -->
    <el-dialog v-model="dialogVisible" title="产品信息" width="700px" center>
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="产品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入产品名称（如：CheersAI Desktop）" />
        </el-form-item>
        <el-form-item label="产品代码" prop="code">
          <el-input v-model="form.code" placeholder="请输入唯一产品代码（如：desktop）" />
          <div class="form-tip">产品唯一标识，用于对接 SSO 和 API 网关</div>
        </el-form-item>
        <el-form-item label="产品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入产品描述"
            rows="3"
          />
        </el-form-item>
        <el-form-item label="产品图标" prop="iconUrl">
          <el-upload
            class="avatar-uploader"
            action="#"
            :auto-upload="false"
            :on-change="handleIconUpload"
            :file-list="iconFileList"
            list-type="picture-card"
          >
            <i class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
          <div class="form-tip">支持 PNG/JPG 格式，建议尺寸 128x128px</div>
        </el-form-item>
        <el-form-item label="当前版本" prop="currentVersion">
          <el-input v-model="form.currentVersion" placeholder="请输入当前版本号（如：1.0.0）" />
        </el-form-item>
        <el-form-item label="下载地址配置">
          <el-card shadow="hover" class="download-config-card">
            <div class="download-item">
              <span class="download-label">Windows：</span>
              <el-input v-model="form.downloadUrls.windows" placeholder="Windows 平台下载链接" class="ml-10" />
            </div>
            <div class="download-item mt-10">
              <span class="download-label">Mac：</span>
              <el-input v-model="form.downloadUrls.mac" placeholder="Mac 平台下载链接" class="ml-10" />
            </div>
            <div class="download-item mt-10">
              <span class="download-label">Linux：</span>
              <el-input v-model="form.downloadUrls.linux" placeholder="Linux 平台下载链接" class="ml-10" />
            </div>
          </el-card>
        </el-form-item>
        <el-form-item label="产品状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择产品状态">
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
            <el-option label="已废弃" value="deprecated" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- 功能配置弹窗（补充功能描述、会员权益关联） -->
    <el-dialog v-model="featureDialogVisible" title="功能配置" width="600px" center>
      <div class="feature-tip mb-10">
        配置 <span class="feature-product">{{ currentRow.name }}</span> 的功能开关及会员权益关联
      </div>
      <el-table :data="featureList" border stripe style="width: 100%">
        <el-table-column prop="name" label="功能名称" width="180" />
        <el-table-column prop="desc" label="功能描述" min-width="200" />
        <el-table-column label="启用状态" width="120" align="center">
          <template #default="scope">
            <el-switch v-model="scope.row.enabled" />
          </template>
        </el-table-column>
        <el-table-column label="会员权益关联" width="180" align="center">
          <template #default="scope">
            <el-select v-model="scope.row.planCodes" placeholder="选择关联会员" multiple>
              <el-option label="免费版" value="free" />
              <el-option label="专业版" value="pro" />
              <el-option label="团队版" value="team" />
              <el-option label="企业版" value="enterprise" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="featureDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="saveFeature">保存</el-button>
      </template>
    </el-dialog>

    <!-- 版本管理弹窗（PRD P0 核心功能） -->
    <el-dialog v-model="versionDialogVisible" title="版本管理" width="800px" center>
      <div class="version-header mb-10">
        <span class="version-product">产品：{{ currentRow.name }}（{{ currentRow.code }}）</span>
        <el-button type="primary" @click="openAddVersionDialog" size="small" class="ml-10">发布新版本</el-button>
      </div>
      <el-table :data="versionList" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="version" label="版本号" width="120" align="center" />
        <el-table-column prop="releaseNotes" label="更新日志" min-width="250" />
        <el-table-column label="下载地址" width="200">
          <template #default="scope">
            <el-button size="small" type="text" @click="viewVersionDownload(scope.row)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="isForceUpdate" label="强制更新" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isForceUpdate ? 'danger' : 'default'" size="small">
              {{ scope.row.isForceUpdate ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'published' ? 'success' : 'warning'" size="small">
              {{ scope.row.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishedAt" label="发布时间" width="180" align="center" />
        <el-table-column label="操作" width="160" align="center">
          <template #default="scope">
            <el-button size="small" @click="editVersion(scope.row)" :disabled="scope.row.status === 'published'">编辑</el-button>
            <el-button size="small" type="success" @click="publishVersion(scope.row)" :disabled="scope.row.status === 'published'">发布</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="versionDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑版本弹窗 -->
    <el-dialog v-model="addVersionDialogVisible" title="发布新版本" width="700px" center>
      <el-form :model="versionForm" :rules="versionRules" ref="versionFormRef" label-width="100px">
        <el-form-item label="版本号" prop="version">
          <el-input v-model="versionForm.version" placeholder="请输入版本号（如：1.1.0）" />
          <div class="form-tip">遵循语义化版本规范：主版本.次版本.修订号</div>
        </el-form-item>
        <el-form-item label="更新日志" prop="releaseNotes">
          <el-input
            v-model="versionForm.releaseNotes"
            type="textarea"
            placeholder="请输入更新日志（支持 Markdown 格式）"
            rows="4"
          />
        </el-form-item>
        <el-form-item label="最低兼容版本" prop="minRequiredVersion">
          <el-input v-model="versionForm.minRequiredVersion" placeholder="请输入最低兼容版本（如：1.0.0）" />
          <div class="form-tip">低于该版本的用户将收到升级提示</div>
        </el-form-item>
        <el-form-item label="强制更新" prop="isForceUpdate">
          <el-switch v-model="versionForm.isForceUpdate" />
          <div class="form-tip">开启后用户必须升级才能使用</div>
        </el-form-item>
        <el-form-item label="下载地址配置">
          <el-card shadow="hover" class="download-config-card">
            <div class="download-item">
              <span class="download-label">Windows：</span>
              <el-input v-model="versionForm.downloadUrls.windows" placeholder="Windows 平台下载链接" class="ml-10" />
            </div>
            <div class="download-item mt-10">
              <span class="download-label">Mac：</span>
              <el-input v-model="versionForm.downloadUrls.mac" placeholder="Mac 平台下载链接" class="ml-10" />
            </div>
            <div class="download-item mt-10">
              <span class="download-label">Linux：</span>
              <el-input v-model="versionForm.downloadUrls.linux" placeholder="Linux 平台下载链接" class="ml-10" />
            </div>
          </el-card>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addVersionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveVersion">保存</el-button>
      </template>
    </el-dialog>

    <!-- 高级筛选弹窗 -->
    <el-dialog v-model="advancedFilterVisible" title="高级筛选" width="500px" center>
      <el-form :model="advancedForm" label-width="100px">
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="advancedForm.createTimeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="当前版本">
          <el-input v-model="advancedForm.currentVersion" placeholder="请输入版本号" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="resetAdvancedFilter">重置</el-button>
        <el-button type="primary" @click="confirmAdvancedFilter">确认筛选</el-button>
      </template>
    </el-dialog>

    <!-- 操作日志弹窗 -->
    <el-dialog v-model="logDialogVisible" title="产品管理操作日志" width="800px" center>
      <div class="log-toolbar mb-10">
        <el-input
          v-model="logSearch"
          placeholder="搜索操作人/操作内容/产品名称"
          clearable
          style="width: 300px"
          class="mr-10"
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
      </div>
      <el-table :data="logList" border stripe style="width: 100%">
        <el-table-column prop="operateTime" label="操作时间" width="180" align="center" />
        <el-table-column prop="operator" label="操作人" width="120" align="center" />
        <el-table-column prop="productName" label="操作产品" width="180" />
        <el-table-column prop="action" label="操作类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getLogTypeTag(scope.row.action)" size="small">
              {{ scope.row.action }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="操作内容" min-width="200" />
        <el-table-column prop="ip" label="操作IP" width="140" align="center" />
      </el-table>

      <template #footer>
        <el-button @click="logDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessageBox, ElMessage, ElImage, ElUpload } from 'element-plus'

// 默认图标
const defaultIcon = 'https://picsum.photos/128/128?random=0'

// loading
const loading = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(8)

// 搜索与筛选
const searchName = ref('')
const statusFilter = ref("")
const advancedForm = ref({
  createTimeRange: [] as Date[],
  currentVersion: ''
})

// 选中行（批量操作）
const selectedRows = ref<any[]>([])

// 监听搜索和过滤，重置页码
watch([searchName, statusFilter], () => {
  currentPage.value = 1
})

// mock 数据（补充产品代码、图标、下载地址、状态等核心字段）
const list = ref([
  { 
    id: 'P001', 
    name: 'CheersAI Desktop', 
    code: 'desktop',
    description: '一站式 Agent 办公客户端',
    iconUrl: 'https://picsum.photos/128/128?random=1',
    currentVersion: '1.0.0',
    downloadUrls: {
      windows: 'https://download.cheersai.com/desktop/win/latest.exe',
      mac: 'https://download.cheersai.com/desktop/mac/latest.dmg',
      linux: 'https://download.cheersai.com/desktop/linux/latest.deb'
    },
    status: 'active', 
    createTime: '2026-03-18',
    updatedAt: '2026-03-18 10:30'
  },
  { 
    id: 'P002', 
    name: 'CheersAI Vault', 
    code: 'vault',
    description: '企业级数据安全存储',
    iconUrl: 'https://picsum.photos/128/128?random=2',
    currentVersion: '0.9.0',
    downloadUrls: {
      windows: '',
      mac: '',
      linux: ''
    },
    status: 'inactive', 
    createTime: '2026-03-17',
    updatedAt: '2026-03-17 14:20'
  },
  { 
    id: 'P003', 
    name: 'CheersAI Writer', 
    code: 'writer',
    description: 'AI 写作助手',
    iconUrl: 'https://picsum.photos/128/128?random=3',
    currentVersion: '0.8.0',
    downloadUrls: {
      windows: '',
      mac: '',
      linux: ''
    },
    status: 'inactive', 
    createTime: '2026-03-16',
    updatedAt: '2026-03-16 09:15'
  },
  { 
    id: 'P004', 
    name: 'CheersAI SSO', 
    code: 'sso',
    description: '统一身份认证服务',
    iconUrl: 'https://picsum.photos/128/128?random=4',
    currentVersion: '1.0.0',
    downloadUrls: {
      windows: '',
      mac: '',
      linux: ''
    },
    status: 'active', 
    createTime: '2026-03-15',
    updatedAt: '2026-03-15 16:40'
  },
  { 
    id: 'P005', 
    name: '旧版AI工具', 
    code: 'old_tool',
    description: '早期版本AI工具（已下线）',
    iconUrl: '',
    currentVersion: '2.5.0',
    downloadUrls: {
      windows: '',
      mac: '',
      linux: ''
    },
    status: 'deprecated', 
    createTime: '2025-12-01',
    updatedAt: '2026-03-01 11:20'
  }
])

// 过滤后的列表
const filteredList = computed(() => {
  return list.value.filter(item => {
    // 基础筛选
    const keywordMatch = !searchName.value || 
      item.name.includes(searchName.value) || 
      item.code.includes(searchName.value)
    const statusMatch = !statusFilter.value || item.status === statusFilter.value

    // 高级筛选
    const dateMatch = !advancedForm.value.createTimeRange.length || (
      new Date(item.createTime) >= new Date(advancedForm.value.createTimeRange[0]) &&
      new Date(item.createTime) <= new Date(advancedForm.value.createTimeRange[1])
    )
    const versionMatch = !advancedForm.value.currentVersion || 
      item.currentVersion.includes(advancedForm.value.currentVersion)

    return keywordMatch && statusMatch && dateMatch && versionMatch
  })
})

/**
 * 分页后的列表
 * 
 * TODO: 预留后端接口位置
 * 如果后续接入后端分页，请在此处调用后端接口，并传入参数：
 * page: currentPage.value
 * size: pageSize.value
 * search: searchName.value
 * status: statusFilter.value
 * startDate: advancedForm.value.createTimeRange[0]
 * endDate: advancedForm.value.createTimeRange[1]
 * currentVersion: advancedForm.value.currentVersion
 */
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
const featureDialogVisible = ref(false)
const versionDialogVisible = ref(false)
const addVersionDialogVisible = ref(false)
const advancedFilterVisible = ref(false)
const logDialogVisible = ref(false)

// 当前选中行
const currentRow = ref<any>({})

// 产品表单（补充核心字段）
const formRef = ref<any>(null)
const form = reactive({
  id: '',
  name: '',
  code: '',
  description: '',
  iconUrl: '',
  currentVersion: '',
  downloadUrls: {
    windows: '',
    mac: '',
    linux: ''
  },
  status: 'active'
})
const formRules = reactive({
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入产品代码', trigger: 'blur' },
    { pattern: /^[a-z0-9_]+$/, message: '产品代码仅支持小写字母、数字和下划线', trigger: 'blur' }
  ],
  currentVersion: [{ required: true, message: '请输入当前版本号', trigger: 'blur' }],
  status: [{ required: true, message: '请选择产品状态', trigger: 'blur' }]
})

// 图标上传文件列表
const iconFileList = ref<any[]>([])

// 功能列表 mock（补充描述和会员关联）
const featureList = ref([
  { key: 'f1', name: 'AI 对话功能', desc: '基础Agent对话交互能力', enabled: true, planCodes: ['free', 'pro', 'team', 'enterprise'] },
  { key: 'f2', name: '知识库管理', desc: '自定义知识库上传和管理', enabled: false, planCodes: ['pro', 'team', 'enterprise'] },
  { key: 'f3', name: '批量导出', desc: '结果批量导出为文档', enabled: false, planCodes: ['team', 'enterprise'] },
  { key: 'f4', name: '团队协作', desc: '多成员共享资源和权限', enabled: false, planCodes: ['team', 'enterprise'] },
  { key: 'f5', name: '私有化部署', desc: '本地服务器部署能力', enabled: false, planCodes: ['enterprise'] }
])

// 版本列表 mock（PRD 版本管理字段）
const versionList = ref([
  {
    id: 'V001',
    productId: 'P001',
    version: '1.0.0',
    releaseNotes: '- 初始版本发布\n- 支持基础Agent对话\n- 支持知识库上传',
    downloadUrls: {
      windows: 'https://download.cheersai.com/desktop/win/1.0.0.exe',
      mac: 'https://download.cheersai.com/desktop/mac/1.0.0.dmg',
      linux: 'https://download.cheersai.com/desktop/linux/1.0.0.deb'
    },
    minRequiredVersion: '0.0.0',
    isForceUpdate: false,
    status: 'published',
    publishedAt: '2026-03-18 10:00'
  },
  {
    id: 'V002',
    productId: 'P001',
    version: '1.1.0',
    releaseNotes: '- 优化对话响应速度\n- 新增批量导出功能\n- 修复已知bug',
    downloadUrls: {
      windows: 'https://download.cheersai.com/desktop/win/1.1.0.exe',
      mac: 'https://download.cheersai.com/desktop/mac/1.1.0.dmg',
      linux: 'https://download.cheersai.com/desktop/linux/1.1.0.deb'
    },
    minRequiredVersion: '1.0.0',
    isForceUpdate: false,
    status: 'draft',
    publishedAt: ''
  }
])

// 版本表单
const versionFormRef = ref<any>(null)
const versionForm = reactive({
  id: '',
  productId: '',
  version: '',
  releaseNotes: '',
  downloadUrls: {
    windows: '',
    mac: '',
    linux: ''
  },
  minRequiredVersion: '',
  isForceUpdate: false,
  status: 'draft'
})
const versionRules = reactive({
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  releaseNotes: [{ required: true, message: '请输入更新日志', trigger: 'blur' }],
  minRequiredVersion: [{ required: true, message: '请输入最低兼容版本', trigger: 'blur' }]
})

// 操作日志 mock
const logList = ref([
  {
    operateTime: '2026-03-18 10:30',
    operator: '产品老李',
    productName: 'CheersAI Desktop',
    action: '创建产品',
    content: '创建产品：名称=CheersAI Desktop，代码=desktop，版本=1.0.0',
    ip: '192.168.1.100'
  },
  {
    operateTime: '2026-03-18 11:15',
    operator: '运营小王',
    productName: 'CheersAI Desktop',
    action: '发布版本',
    content: '发布版本1.0.0，更新日志：初始版本发布',
    ip: '192.168.1.101'
  },
  {
    operateTime: '2026-03-17 14:20',
    operator: '产品老李',
    productName: 'CheersAI Vault',
    action: '修改状态',
    content: '将产品状态从启用改为禁用',
    ip: '192.168.1.102'
  }
])

// 日志搜索
const logSearch = ref('')
const logDateRange = ref<Date[]>([])

// 打开新增/编辑产品
const openDialog = (row?: any) => {
  if (row) {
    Object.assign(form, row)
    // 初始化图标文件列表
    iconFileList.value = row.iconUrl ? [{ url: row.iconUrl }] : []
  } else {
    form.id = ''
    form.name = ''
    form.code = ''
    form.description = ''
    form.iconUrl = ''
    form.currentVersion = ''
    form.downloadUrls = { windows: '', mac: '', linux: '' }
    form.status = 'active'
    iconFileList.value = []
  }
  currentRow.value = row || {}
  dialogVisible.value = true
}

// 保存产品
const handleSave = () => {
  formRef.value.validate((valid: boolean) => {
    if (!valid) return

    // 检查产品代码唯一性
    const codeExists = list.value.some(item => item.code === form.code && item.id !== form.id)
    if (codeExists) {
      ElMessage.error('产品代码已存在，请更换')
      return
    }

    // 构造产品数据
    const productData = {
      ...form,
      updatedAt: new Date().toLocaleString().replace(/\//g, '-')
    }

    // 新增产品
    if (!form.id) {
      productData.id = `P${list.value.length + 100}`
      productData.createTime = new Date().toLocaleDateString().replace(/\//g, '-')
      list.value.push(productData)
      ElMessage.success('产品创建成功')
    } else {
      // 编辑产品
      const index = list.value.findIndex(item => item.id === form.id)
      if (index !== -1) {
        list.value[index] = productData
      }
      ElMessage.success('产品更新成功')
    }

    // TODO: 调用新增/编辑产品接口（/api/v1/products）
    dialogVisible.value = false

    // 记录审计日志
    recordAuditLog(form.id ? '编辑产品' : '创建产品', productData)
  })
}

// 图标上传处理
const handleIconUpload = (uploadFile: any) => {
  // 模拟上传成功，实际项目中需调用文件上传接口
  const fileUrl = `https://picsum.photos/128/128?random=${Math.random()}`
  form.iconUrl = fileUrl
  iconFileList.value = [{ url: fileUrl }]
  ElMessage.success('图标上传成功')

  // TODO: 调用文件上传接口（MinIO），获取 icon_url
}

// 状态切换（表格内开关）
const handleStatusChange = (row: any) => {
  // TODO: 调用状态切换接口（/api/v1/products/:id/status）
  ElMessage.success(`产品${row.name}状态已切换为 ${row.status === 'active' ? '启用' : '禁用'}`)

  // 记录审计日志
  recordAuditLog('修改产品状态', row)
}

// 删除产品
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确认删除产品${row.name}吗？删除后不可恢复`, '提示', {
    type: 'warning'
  }).then(() => {
    // TODO: 调用删除接口（/api/v1/products/:id）
    list.value = list.value.filter(item => item.id !== row.id)
    ElMessage.success('产品删除成功')

    // 记录审计日志
    recordAuditLog('删除产品', row)
  })
}

// 批量删除
const batchDelete = () => {
  ElMessageBox.confirm(`确认删除选中的${selectedRows.value.length}个产品吗？删除后不可恢复`, '批量删除确认', {
    type: 'warning'
  }).then(() => {
    const ids = selectedRows.value.map(item => item.id)
    // TODO: 调用批量删除接口（/api/v1/products/batch-delete）
    list.value = list.value.filter(item => !ids.includes(item.id))
    selectedRows.value = []
    ElMessage.success('批量删除成功')

    // 记录审计日志
    selectedRows.value.forEach(row => recordAuditLog('批量删除产品', row))
  })
}

// 功能配置
const openFeatureDialog = (row: any) => {
  currentRow.value = row
  // TODO: 调用获取产品功能列表接口（/api/v1/products/:id/features）
  featureDialogVisible.value = true
}

const saveFeature = () => {
  // TODO: 保存功能配置接口（/api/v1/products/:id/features）
  ElMessage.success('功能配置已保存')
  featureDialogVisible.value = false

  // 记录审计日志
  recordAuditLog('配置产品功能', currentRow.value)
}

// 版本管理
const openVersionDialog = (row: any) => {
  currentRow.value = row
  // TODO: 调用获取产品版本列表接口（/api/v1/products/:id/versions）
  versionDialogVisible.value = true
}

// 打开新增版本
const openAddVersionDialog = () => {
  versionForm.id = ''
  versionForm.productId = currentRow.value.id
  versionForm.version = ''
  versionForm.releaseNotes = ''
  versionForm.downloadUrls = { windows: '', mac: '', linux: '' }
  versionForm.minRequiredVersion = currentRow.value.currentVersion || '0.0.0'
  versionForm.isForceUpdate = false
  versionForm.status = 'draft'
  addVersionDialogVisible.value = true
}

// 编辑版本
const editVersion = (row: any) => {
  Object.assign(versionForm, row)
  addVersionDialogVisible.value = true
}

// 保存版本
const saveVersion = () => {
  versionFormRef.value.validate((valid: boolean) => {
    if (!valid) return

    // 检查版本号唯一性
    const versionExists = versionList.value.some(item => 
      item.productId === versionForm.productId && 
      item.version === versionForm.version && 
      item.id !== versionForm.id
    )
    if (versionExists) {
      ElMessage.error('该版本号已存在，请更换')
      return
    }

    if (!versionForm.id) {
      // 新增版本
      versionForm.id = `V${versionList.value.length + 100}`
      versionList.value.push({ ...versionForm })
      ElMessage.success('版本创建成功')
    } else {
      // 编辑版本
      const index = versionList.value.findIndex(item => item.id === versionForm.id)
      if (index !== -1) {
        versionList.value[index] = { ...versionForm }
      }
      ElMessage.success('版本更新成功')
    }

    // TODO: 调用新增/编辑版本接口（/api/v1/products/:id/versions）
    addVersionDialogVisible.value = false

    // 记录审计日志
    recordAuditLog(versionForm.id ? '编辑版本' : '创建版本', versionForm)
  })
}

// 发布版本
const publishVersion = (row: any) => {
  row.status = 'published'
  row.publishedAt = new Date().toLocaleString().replace(/\//g, '-')
  
  // 更新产品当前版本
  const productIndex = list.value.findIndex(item => item.id === row.productId)
  if (productIndex !== -1) {
    list.value[productIndex].currentVersion = row.version
    list.value[productIndex].updatedAt = new Date().toLocaleString().replace(/\//g, '-')
  }

  // TODO: 调用版本发布接口（/api/v1/products/:id/versions/:version/publish）
  ElMessage.success('版本发布成功')

  // 记录审计日志
  recordAuditLog('发布版本', row)
}

// 查看版本下载地址
const viewVersionDownload = (row: any) => {
  ElMessageBox.alert(`
    Windows：${row.downloadUrls.windows || '暂无'}</br>
    Mac：${row.downloadUrls.mac || '暂无'}</br>
    Linux：${row.downloadUrls.linux || '暂无'}
  `, '版本下载地址', {
    dangerouslyUseHTMLString: true
  })
}

// 高级筛选
const openAdvancedFilter = () => {
  advancedFilterVisible.value = true
}

// 重置高级筛选
const resetAdvancedFilter = () => {
  advancedForm.value = {
    createTimeRange: [],
    currentVersion: ''
  }
}

// 确认高级筛选
const confirmAdvancedFilter = () => {
  advancedFilterVisible.value = false
  currentPage.value = 1 // 重置页码
}

// 打开操作日志
const openLogDialog = () => {
  logDialogVisible.value = true
}

// 批量选择事件
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

// 辅助函数：获取状态标签颜色
const getStatusTag = (status: string) => {
  switch (status) {
    case 'active': return 'success'
    case 'inactive': return 'warning'
    case 'deprecated': return 'danger'
    default: return 'default'
  }
}

// 辅助函数：获取日志类型标签颜色
const getLogTypeTag = (action: string) => {
  switch (action) {
    case '创建产品': return 'success'
    case '编辑产品': return 'primary'
    case '删除产品': return 'danger'
    case '发布版本': return 'info'
    case '修改状态': return 'warning'
    default: return 'default'
  }
}

// 记录审计日志（全域合规要求）
const recordAuditLog = (action: string, target: any) => {
  const log = {
    operateTime: new Date().toLocaleString().replace(/\//g, '-'),
    operator: '产品老李', // 实际从登录信息获取
    productName: target.name || target.productName,
    action,
    content: JSON.stringify(target),
    ip: '192.168.1.100' // 实际从请求头获取
  }
  logList.value.unshift(log)

  // TODO: 调用审计日志记录接口（/api/v1/audit-logs）
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title {
  font-size: 20px;
  font-weight: bold;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.mr-10 {
  margin-right: 10px;
}

.ml-10 {
  margin-left: 10px;
}

.mt-10 {
  margin-top: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 表单提示 */
.form-tip {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

/* 下载地址配置卡片 */
.download-config-card {
  padding: 16px;
}

.download-item {
  display: flex;
  align-items: center;
}

.download-label {
  width: 80px;
  font-weight: 500;
  color: #4b5563;
}

/* 功能配置样式 */
.feature-tip {
  padding: 8px 12px;
  background-color: #f3f4f6;
  border-radius: 4px;
  font-size: 14px;
}

.feature-product {
  color: #409eff;
  font-weight: 500;
}

/* 版本管理样式 */
.version-header {
  display: flex;
  align-items: center;
}

.version-product {
  font-weight: 500;
  color: #1f2937;
}

/* 图标上传样式 */
:deep(.avatar-uploader) {
  width: 128px;
  height: 128px;
}

:deep(.avatar-uploader-icon) {
  font-size: 28px;
  color: #409eff;
  width: 128px;
  height: 128px;
  line-height: 128px;
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

/* 弹窗样式优化 */
:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px 20px;
}
</style>