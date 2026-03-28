<template>
  <el-card class="page-container">
    <div class="header">
      <div class="title">产品管理</div>
      <el-tag type="danger">P0</el-tag>
    </div>

    <div class="toolbar">
      <el-input v-model="searchName" placeholder="搜索产品名称/代码" clearable style="width: 240px" @keyup.enter="handleSearch" />
      <el-select v-model="statusFilter" placeholder="状态" style="width: 140px" @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="启用" value="active" />
        <el-option label="禁用" value="inactive" />
        <el-option label="已废弃" value="deprecated" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="openDialog()">新建产品</el-button>
      <el-button type="danger" :disabled="selectedRows.length === 0" @click="batchDelete">批量删除</el-button>
      <el-button type="info" @click="advancedFilterVisible = true">高级筛选</el-button>
      <el-button type="warning" @click="openLogDialog">操作日志</el-button>
    </div>

    <el-table :data="productList" v-loading="loading" border stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="60" align="center" />
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="name" label="产品名称" min-width="180" />
      <el-table-column prop="code" label="产品代码" width="140" align="center" />
      <el-table-column label="图标" width="90" align="center">
        <template #default="{ row }"><el-image :src="row.iconUrl || defaultIcon" style="width:36px;height:36px;border-radius:4px" fit="cover" /></template>
      </el-table-column>
      <el-table-column prop="currentVersion" label="当前版本" width="120" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }"><el-tag :type="getStatusTag(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="创建时间" width="180" align="center"><template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template></el-table-column>
      <el-table-column label="更新时间" width="180" align="center"><template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template></el-table-column>
      <el-table-column label="操作" width="420" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" @click="toggleStatus(row)">{{ row.status === 'active' ? '禁用' : '启用' }}</el-button>
          <el-button size="small" @click="openVersionDialog(row)">版本管理</el-button>
          <el-button size="small" @click="openFeatureDialog(row)">功能配置</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[8,16,24,32]"
        layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <el-dialog v-model="dialogVisible" title="产品信息" width="700px" center>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="产品名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="产品代码" prop="code"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="产品描述"><el-input v-model="form.description" type="textarea" rows="3" /></el-form-item>
        <el-form-item label="产品图标">
          <el-upload action="#" :auto-upload="false" :on-change="handleIconUpload" :file-list="iconFileList" list-type="picture-card" accept="image/png,image/jpeg,image/jpg,image/webp">
            <i class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
          <div class="form-tip">前端转 Base64 后直接存入 iconUrl</div>
        </el-form-item>
        <el-form-item label="当前版本"><el-input v-model="form.currentVersion" /></el-form-item>
        <el-form-item label="下载地址配置">
          <el-card shadow="hover">
            <div class="download-item"><span class="download-label">Windows：</span><el-input v-model="form.downloadUrls.windows" /></div>
            <div class="download-item mt-10"><span class="download-label">Mac：</span><el-input v-model="form.downloadUrls.mac" /></div>
            <div class="download-item mt-10"><span class="download-label">Linux：</span><el-input v-model="form.downloadUrls.linux" /></div>
          </el-card>
        </el-form-item>
        <el-form-item label="产品状态" prop="status">
          <el-select v-model="form.status">
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
            <el-option label="已废弃" value="deprecated" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingProduct" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="featureDialogVisible" title="功能配置" width="700px" center>
      <div class="feature-tip">配置 {{ currentRow?.name || '-' }} 的功能开关及会员权益关联</div>
      <el-table :data="featureList" border stripe style="margin-top: 10px">
        <el-table-column prop="name" label="功能名称" width="180" />
        <el-table-column prop="desc" label="功能描述" min-width="200" />
        <el-table-column label="启用状态" width="110" align="center"><template #default="{ row }"><el-switch v-model="row.enabled" /></template></el-table-column>
        <el-table-column label="会员关联" width="180" align="center">
          <template #default="{ row }">
            <el-select v-model="row.planCodes" multiple>
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
        <el-button type="primary" :loading="savingFeature" @click="saveFeature">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="versionDialogVisible" title="版本管理" width="980px" center>
      <div class="version-header">
        <span>产品：{{ currentRow?.name || '-' }}（{{ currentRow?.code || '-' }}）</span>
        <el-button type="primary" size="small" style="margin-left: 10px" @click="openAddVersionDialog">发布新版本</el-button>
      </div>
      <el-table :data="versionList" border stripe style="margin-top: 10px">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="version" label="版本号" width="110" align="center" />
        <el-table-column prop="versionName" label="版本名称" width="120" align="center" />
        <el-table-column prop="changelog" label="更新日志" min-width="220" show-overflow-tooltip />
        <el-table-column label="下载地址" width="100" align="center"><template #default="{ row }"><el-button size="small" link type="primary" @click="viewVersionDownload(row)">查看</el-button></template></el-table-column>
        <el-table-column label="强制更新" width="100" align="center"><template #default="{ row }"><el-tag :type="row.forceUpdate ? 'danger' : 'info'" size="small">{{ row.forceUpdate ? '是' : '否' }}</el-tag></template></el-table-column>
        <el-table-column label="状态" width="100" align="center"><template #default="{ row }"><el-tag :type="row.status === 'published' ? 'success' : row.status === 'deprecated' ? 'danger' : 'warning'" size="small">{{ row.status === 'published' ? '已发布' : row.status === 'deprecated' ? '已废弃' : '草稿' }}</el-tag></template></el-table-column>
        <el-table-column label="发布时间" width="180" align="center"><template #default="{ row }">{{ formatDateTime(row.publishedAt) }}</template></el-table-column>
        <el-table-column label="操作" width="240" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="editVersion(row)" :disabled="row.status === 'published'">编辑</el-button>
            <el-button size="small" type="success" @click="publishVersion(row)" :disabled="row.status === 'published'">发布</el-button>
            <el-button size="small" type="warning" @click="deprecateVersion(row)" :disabled="row.status !== 'published'">废弃</el-button>
            <el-button size="small" type="danger" @click="removeVersion(row)" :disabled="row.status === 'published'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer><el-button @click="versionDialogVisible = false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="addVersionDialogVisible" :title="versionMode === 'create' ? '发布新版本' : '编辑版本'" width="720px" center>
      <el-form ref="versionFormRef" :model="versionForm" :rules="versionRules" label-width="110px">
        <el-form-item label="版本号" prop="version"><el-input v-model="versionForm.version" /></el-form-item>
        <el-form-item label="版本名称"><el-input v-model="versionForm.versionName" /></el-form-item>
        <el-form-item label="更新日志" prop="releaseNotes"><el-input v-model="versionForm.releaseNotes" type="textarea" rows="4" /></el-form-item>
        <el-form-item label="最低兼容版本"><el-input v-model="versionForm.minRequiredVersion" /></el-form-item>
        <el-form-item label="强制更新"><el-switch v-model="versionForm.isForceUpdate" /></el-form-item>
        <el-form-item label="下载地址配置">
          <el-card shadow="hover">
            <div class="download-item"><span class="download-label">Windows：</span><el-input v-model="versionForm.downloadUrls.windows" /></div>
            <div class="download-item mt-10"><span class="download-label">Mac：</span><el-input v-model="versionForm.downloadUrls.mac" /></div>
            <div class="download-item mt-10"><span class="download-label">Linux：</span><el-input v-model="versionForm.downloadUrls.linux" /></div>
          </el-card>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVersionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingVersion" @click="saveVersion">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="advancedFilterVisible" title="高级筛选" width="520px" center>
      <el-form :model="advancedForm" label-width="100px">
        <el-form-item label="创建时间"><el-date-picker v-model="advancedForm.createTimeRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%" /></el-form-item>
        <el-form-item label="当前版本"><el-input v-model="advancedForm.currentVersion" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetAdvancedFilter">重置</el-button>
        <el-button type="primary" @click="confirmAdvancedFilter">确认筛选</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logDialogVisible" title="产品管理操作日志" width="920px" center>
      <div class="toolbar">
        <el-input v-model="logSearch" placeholder="搜索操作人/操作内容/产品名称" clearable style="width: 300px" @keyup.enter="handleLogSearch" />
        <el-date-picker v-model="logDateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 300px" />
        <el-button type="primary" @click="handleLogSearch">查询</el-button>
      </div>
      <el-table :data="logList" border stripe v-loading="logLoading">
        <el-table-column label="操作时间" width="180" align="center"><template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template></el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120" align="center" />
        <el-table-column prop="productName" label="操作产品" width="180" />
        <el-table-column prop="action" label="操作类型" width="120" align="center"><template #default="{ row }"><el-tag :type="getLogTypeTag(row.action)" size="small">{{ row.action }}</el-tag></template></el-table-column>
        <el-table-column prop="content" label="操作内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="操作IP" width="140" align="center" />
      </el-table>
      <div class="pagination-container">
        <el-pagination v-model:current-page="logPage" v-model:page-size="logPageSize" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" :total="logTotal" @size-change="handleLogSizeChange" @current-change="handleLogPageChange" />
      </div>
      <template #footer><el-button @click="logDialogVisible = false">关闭</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import {
  batchDeleteProducts, createProduct, createProductVersion, deleteProduct, deleteProductVersion, deprecateProductVersion,
  fetchProductFeatures, fetchProductList, fetchProductOperationLogs, fetchProductVersionList, publishProductVersion,
  updateProduct, updateProductFeatures, updateProductStatus, updateProductVersion,
  type ProductDetailDTO, type ProductFeatureDTO, type ProductOperationLogDTO, type ProductStatus, type ProductVersionDetailDTO
} from '../api/products'
import { getErrorMessage } from '../utils/api'

interface DownloadUrls { windows: string; mac: string; linux: string }
interface ProductView extends ProductDetailDTO { downloadUrlsObj: DownloadUrls; settingsObj: Record<string, unknown> }
interface VersionView extends ProductVersionDetailDTO { downloadUrlsObj: DownloadUrls }

const defaultIcon = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTI4IiBoZWlnaHQ9IjEyOCIgdmlld0JveD0iMCAwIDEyOCAxMjgiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjEyOCIgaGVpZ2h0PSIxMjgiIHJ4PSIxNiIgZmlsbD0iI0U1RTdFQiIvPjxwYXRoIGQ9Ik02NCA0NUM1Ni44MjY5IDQ1IDUxIDUwLjgyNjkgNTEgNThDNTEgNjUuMTczMSA1Ni44MjY5IDcxIDY0IDcxQzcxLjE3MzEgNzEgNzcgNjUuMTczMSA3NyA1OEM3NyA1MC44MjY5IDcxLjE3MzEgNDUgNjQgNDVaIiBmaWxsPSIjOUNBM0FGIi8+PHBhdGggZD0iTTQ0IDkyQzQ0IDgxLjUwNjYgNTIuNTA2NiA3MyA2MyA3M0g2NUM3NS40OTM0IDczIDg0IDgxLjUwNjYgODQgOTJIODRINDRaIiBmaWxsPSIjOUNBM0FGIi8+PC9zdmc+'

const loading = ref(false)
const savingProduct = ref(false)
const savingFeature = ref(false)
const savingVersion = ref(false)
const logLoading = ref(false)

const productList = ref<ProductView[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(8)
const searchName = ref('')
const statusFilter = ref<ProductStatus | ''>('')
const advancedForm = reactive({ createTimeRange: [] as Date[], currentVersion: '' })
const selectedRows = ref<ProductView[]>([])

const dialogVisible = ref(false)
const featureDialogVisible = ref(false)
const versionDialogVisible = ref(false)
const addVersionDialogVisible = ref(false)
const advancedFilterVisible = ref(false)
const logDialogVisible = ref(false)
const currentRow = ref<ProductView | null>(null)

const formRef = ref<FormInstance>()
const form = reactive({
  id: '', name: '', code: '', description: '', iconUrl: '', currentVersion: '',
  downloadUrls: emptyDownloadUrls(), status: 'active' as ProductStatus, settingsObj: {} as Record<string, unknown>
})
const formRules: FormRules<typeof form> = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入产品代码', trigger: 'blur' }, { pattern: /^[a-z0-9_]+$/, message: '产品代码仅支持小写字母、数字和下划线', trigger: 'blur' }],
  status: [{ required: true, message: '请选择产品状态', trigger: 'change' }]
}
const iconFileList = ref<Array<{ name?: string; url: string }>>([])

const featureList = ref<ProductFeatureDTO[]>([])

const versionList = ref<VersionView[]>([])
const versionMode = ref<'create' | 'edit'>('create')
const versionFormRef = ref<FormInstance>()
const versionForm = reactive({
  id: '', version: '', versionName: '', releaseNotes: '', downloadUrls: emptyDownloadUrls(), minRequiredVersion: '', isForceUpdate: false
})
const versionRules: FormRules<typeof versionForm> = {
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  releaseNotes: [{ required: true, message: '请输入更新日志', trigger: 'blur' }]
}

const logList = ref<ProductOperationLogDTO[]>([])
const logSearch = ref('')
const logDateRange = ref<Date[]>([])
const logPage = ref(1)
const logPageSize = ref(10)
const logTotal = ref(0)

async function loadProducts() {
  loading.value = true
  try {
    const res = await fetchProductList({
      keyword: searchName.value.trim() || undefined,
      status: statusFilter.value || undefined,
      currentVersion: advancedForm.currentVersion.trim() || undefined,
      startTime: advancedForm.createTimeRange[0]?.toISOString(),
      endTime: advancedForm.createTimeRange[1]?.toISOString(),
      page: currentPage.value,
      pageSize: pageSize.value
    })
    productList.value = (res.items || []).map((item) => ({ ...item, downloadUrlsObj: parseDownloadUrls(item.downloadUrls), settingsObj: parseSettings(item.settings) }))
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '产品列表加载失败'))
  } finally {
    loading.value = false
  }
}

function handleSearch() { currentPage.value = 1; loadProducts() }
function handleSizeChange(size: number) { pageSize.value = size; currentPage.value = 1; loadProducts() }
function handleCurrentChange(page: number) { currentPage.value = page; loadProducts() }
function handleSelectionChange(rows: ProductView[]) { selectedRows.value = rows }

function openDialog(row?: ProductView) {
  if (!row) {
    form.id = ''; form.name = ''; form.code = ''; form.description = ''; form.iconUrl = ''; form.currentVersion = ''
    form.downloadUrls = emptyDownloadUrls(); form.status = 'active'; form.settingsObj = {}; iconFileList.value = []; currentRow.value = null
  } else {
    form.id = row.id; form.name = row.name; form.code = row.code; form.description = row.description || ''; form.iconUrl = row.iconUrl || ''
    form.currentVersion = row.currentVersion || ''; form.downloadUrls = { ...row.downloadUrlsObj }; form.status = row.status; form.settingsObj = { ...row.settingsObj }
    iconFileList.value = form.iconUrl ? [{ url: form.iconUrl }] : []; currentRow.value = row
  }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  savingProduct.value = true
  const payload = {
    name: form.name.trim(), code: form.code.trim(), description: form.description || undefined, iconUrl: form.iconUrl || undefined,
    status: form.status, currentVersion: form.currentVersion || undefined, downloadUrls: safeStringify(form.downloadUrls), settings: safeStringify(form.settingsObj)
  }
  try {
    if (form.id) { await updateProduct(form.id, payload); ElMessage.success('产品更新成功') }
    else { await createProduct(payload); ElMessage.success('产品创建成功') }
    dialogVisible.value = false
    await loadProducts()
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '产品保存失败'))
  } finally {
    savingProduct.value = false
  }
}

function handleIconUpload(file: UploadFile) {
  if (!file.raw) return
  const reader = new FileReader()
  reader.onload = () => {
    const url = String(reader.result || '')
    form.iconUrl = url
    iconFileList.value = [{ name: file.name, url }]
    ElMessage.success('图标已转为 Base64')
  }
  reader.readAsDataURL(file.raw)
}

async function handleDelete(row: ProductView) {
  try {
    await ElMessageBox.confirm(`确认删除产品 ${row.name} 吗？`, '删除确认', { type: 'warning' })
    await deleteProduct(row.id)
    ElMessage.success('产品删除成功')
    if (productList.value.length === 1 && currentPage.value > 1) currentPage.value -= 1
    await loadProducts()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(getErrorMessage(error, '产品删除失败'))
  }
}

async function batchDelete() {
  if (!selectedRows.value.length) return
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 个产品吗？`, '批量删除确认', { type: 'warning' })
    await batchDeleteProducts({ ids: selectedRows.value.map((x) => x.id) })
    selectedRows.value = []
    ElMessage.success('批量删除成功')
    await loadProducts()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(getErrorMessage(error, '批量删除失败'))
  }
}

async function toggleStatus(row: ProductView) {
  const target: ProductStatus = row.status === 'active' ? 'inactive' : 'active'
  try {
    await updateProductStatus(row.id, target)
    ElMessage.success(`状态已更新为${getStatusLabel(target)}`)
    await loadProducts()
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '状态更新失败'))
  }
}

async function openFeatureDialog(row: ProductView) {
  currentRow.value = row
  featureDialogVisible.value = true
  try {
    const res = await fetchProductFeatures(row.id)
    featureList.value = res.features?.length ? res.features : defaultFeatures()
  } catch (error) {
    featureList.value = defaultFeatures()
    ElMessage.error(getErrorMessage(error, '功能配置加载失败'))
  }
}

async function saveFeature() {
  if (!currentRow.value) return
  savingFeature.value = true
  try {
    await updateProductFeatures(currentRow.value.id, { features: featureList.value })
    ElMessage.success('功能配置已保存')
    featureDialogVisible.value = false
    await loadProducts()
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '功能配置保存失败'))
  } finally {
    savingFeature.value = false
  }
}

async function openVersionDialog(row: ProductView) { currentRow.value = row; versionDialogVisible.value = true; await loadVersions(row.id) }
async function loadVersions(productId: string) {
  try {
    const items = await fetchProductVersionList(productId)
    versionList.value = items.map((x) => ({ ...x, forceUpdate: !!x.forceUpdate, downloadUrlsObj: parseDownloadUrls(x.downloadUrls) }))
  } catch (error) {
    versionList.value = []
    ElMessage.error(getErrorMessage(error, '版本列表加载失败'))
  }
}

function openAddVersionDialog() {
  if (!currentRow.value) return
  versionMode.value = 'create'
  versionForm.id = ''; versionForm.version = ''; versionForm.versionName = ''; versionForm.releaseNotes = ''
  versionForm.downloadUrls = emptyDownloadUrls(); versionForm.minRequiredVersion = currentRow.value.currentVersion || '0.0.0'; versionForm.isForceUpdate = false
  addVersionDialogVisible.value = true
}

function editVersion(row: VersionView) {
  versionMode.value = 'edit'
  versionForm.id = row.id; versionForm.version = row.version; versionForm.versionName = row.versionName || ''; versionForm.releaseNotes = row.changelog || ''
  versionForm.downloadUrls = { ...row.downloadUrlsObj }; versionForm.minRequiredVersion = row.minVersion || ''; versionForm.isForceUpdate = !!row.forceUpdate
  addVersionDialogVisible.value = true
}

async function saveVersion() {
  if (!currentRow.value) return
  const valid = await versionFormRef.value?.validate().catch(() => false)
  if (!valid) return
  savingVersion.value = true
  const payload = {
    version: versionForm.version.trim(), versionName: versionForm.versionName.trim() || undefined, changelog: versionForm.releaseNotes,
    downloadUrls: safeStringify(versionForm.downloadUrls), releaseNote: versionForm.releaseNotes, forceUpdate: versionForm.isForceUpdate,
    minVersion: versionForm.minRequiredVersion.trim() || undefined
  }
  try {
    if (versionMode.value === 'create') { await createProductVersion(currentRow.value.id, payload); ElMessage.success('版本创建成功') }
    else { await updateProductVersion(currentRow.value.id, versionForm.id, payload); ElMessage.success('版本更新成功') }
    addVersionDialogVisible.value = false
    await loadVersions(currentRow.value.id)
    await loadProducts()
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '版本保存失败'))
  } finally {
    savingVersion.value = false
  }
}

async function publishVersion(row: VersionView) {
  if (!currentRow.value) return
  try { await publishProductVersion(currentRow.value.id, row.id); ElMessage.success('版本发布成功'); await loadVersions(currentRow.value.id); await loadProducts() }
  catch (error) { ElMessage.error(getErrorMessage(error, '版本发布失败')) }
}

async function deprecateVersion(row: VersionView) {
  if (!currentRow.value) return
  try {
    await ElMessageBox.confirm(`确认废弃版本 ${row.version} 吗？`, '废弃确认', { type: 'warning' })
    await deprecateProductVersion(currentRow.value.id, row.id)
    ElMessage.success('版本已废弃')
    await loadVersions(currentRow.value.id); await loadProducts()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(getErrorMessage(error, '版本废弃失败'))
  }
}

async function removeVersion(row: VersionView) {
  if (!currentRow.value) return
  try {
    await ElMessageBox.confirm(`确认删除版本 ${row.version} 吗？`, '删除确认', { type: 'warning' })
    await deleteProductVersion(currentRow.value.id, row.id)
    ElMessage.success('版本删除成功')
    await loadVersions(currentRow.value.id)
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(getErrorMessage(error, '版本删除失败'))
  }
}

function viewVersionDownload(row: VersionView) {
  ElMessageBox.alert(`Windows：${row.downloadUrlsObj.windows || '暂无'}<br/>Mac：${row.downloadUrlsObj.mac || '暂无'}<br/>Linux：${row.downloadUrlsObj.linux || '暂无'}`, '版本下载地址', {
    dangerouslyUseHTMLString: true
  })
}

function resetAdvancedFilter() { advancedForm.createTimeRange = []; advancedForm.currentVersion = '' }
function confirmAdvancedFilter() { advancedFilterVisible.value = false; currentPage.value = 1; loadProducts() }

async function openLogDialog() { logDialogVisible.value = true; logPage.value = 1; await loadLogs() }
async function loadLogs() {
  logLoading.value = true
  try {
    const res = await fetchProductOperationLogs({
      keyword: logSearch.value.trim() || undefined, startTime: logDateRange.value[0]?.toISOString(), endTime: logDateRange.value[1]?.toISOString(),
      page: logPage.value, pageSize: logPageSize.value
    })
    logList.value = res.items || []
    logTotal.value = res.total || 0
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '操作日志加载失败'))
  } finally {
    logLoading.value = false
  }
}
function handleLogSearch() { logPage.value = 1; loadLogs() }
function handleLogSizeChange(size: number) { logPageSize.value = size; logPage.value = 1; loadLogs() }
function handleLogPageChange(page: number) { logPage.value = page; loadLogs() }

function parseDownloadUrls(v?: string): DownloadUrls {
  if (!v) return emptyDownloadUrls()
  try { const p = JSON.parse(v) as Partial<DownloadUrls>; return { windows: p.windows || '', mac: p.mac || '', linux: p.linux || '' } }
  catch { return emptyDownloadUrls() }
}
function parseSettings(v?: string): Record<string, unknown> { if (!v) return {}; try { const p = JSON.parse(v); return p && typeof p === 'object' ? (p as Record<string, unknown>) : {} } catch { return {} } }
function safeStringify(v: unknown) { try { return JSON.stringify(v ?? {}) } catch { return '{}' } }
function emptyDownloadUrls(): DownloadUrls { return { windows: '', mac: '', linux: '' } }
function defaultFeatures(): ProductFeatureDTO[] {
  return [
    { key: 'f1', name: 'AI 对话功能', desc: '基础 Agent 对话交互能力', enabled: true, planCodes: ['free', 'pro', 'team', 'enterprise'] },
    { key: 'f2', name: '知识库管理', desc: '自定义知识库上传和管理', enabled: false, planCodes: ['pro', 'team', 'enterprise'] },
    { key: 'f3', name: '批量导出', desc: '结果批量导出为文档', enabled: false, planCodes: ['team', 'enterprise'] },
    { key: 'f4', name: '团队协作', desc: '多成员共享资源和权限', enabled: false, planCodes: ['team', 'enterprise'] },
    { key: 'f5', name: '私有化部署', desc: '本地服务器部署能力', enabled: false, planCodes: ['enterprise'] }
  ]
}
function formatDateTime(v?: string) {
  if (!v) return '-'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return v
  const y = d.getFullYear(); const m = String(d.getMonth() + 1).padStart(2, '0'); const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0'); const mm = String(d.getMinutes()).padStart(2, '0'); const s = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${mm}:${s}`
}
function getStatusTag(status: ProductStatus) { if (status === 'active') return 'success'; if (status === 'inactive') return 'warning'; return 'danger' }
function getStatusLabel(status: ProductStatus) { if (status === 'active') return '启用'; if (status === 'inactive') return '禁用'; return '已废弃' }
function getLogTypeTag(action: string) { if (action.includes('删除')) return 'danger'; if (action.includes('创建')) return 'success'; if (action.includes('发布')) return 'primary'; if (action.includes('状态')) return 'warning'; return 'info' }

onMounted(() => { loadProducts() })
</script>

<style scoped>
.page-container { padding: 0; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.title { font-size: 20px; font-weight: 600; color: #111827; }
.toolbar { margin-bottom: 20px; display: flex; align-items: center; flex-wrap: wrap; gap: 12px; }
.pagination-container { margin-top: 24px; display: flex; justify-content: flex-end; }
.download-item { display: flex; align-items: center; }
.download-label { width: 80px; font-weight: 500; color: #4b5563; }
.feature-tip { padding: 12px 16px; background-color: #f9fafb; border: 1px solid #e5e7eb; border-radius: 8px; font-size: 14px; }
.version-header { display: flex; align-items: center; }
.form-tip { font-size: 12px; color: #6b7280; margin-top: 4px; }
.mt-10 { margin-top: 10px; }
:deep(.avatar-uploader-icon) { font-size: 28px; color: #3b82f6; width: 128px; height: 128px; line-height: 128px; }
:deep(.el-table th) { background-color: #f9fafb; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.05em; font-size: 11px; }
</style>
