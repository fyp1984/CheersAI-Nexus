<template>
  <el-card class="page-container" shadow="hover">
    <div class="header">
      <div class="header-left">
        <div class="title">系统配置</div>
        <div class="subtitle">账号体系配置：注册策略、登录安全、会话与风控</div>
      </div>
      <el-tag type="danger" size="large">P0</el-tag>
    </div>

    <div class="stats-card mb-20">
      <div class="stat-item">
        <div class="stat-label">当前登录策略</div>
        <div class="stat-value">{{ securityForm.loginMode }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">注册方式</div>
        <div class="stat-value primary">{{ registerForm.registerMethods.length }} 种</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">IP 白名单</div>
        <div class="stat-value warning">{{ ipWhitelist.length }} 条</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">双因子认证</div>
        <div class="stat-value" :class="securityForm.enable2FA ? 'success' : 'warning'">
          {{ securityForm.enable2FA ? '已开启' : '未开启' }}
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="注册策略" name="register">
        <el-form label-width="140px" class="config-form">
          <el-form-item label="允许注册方式">
            <el-checkbox-group v-model="registerForm.registerMethods">
              <el-checkbox label="邮箱" />
              <el-checkbox label="手机号" />
              <el-checkbox label="企业邀请码" />
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="是否强制邮箱验证">
            <el-switch v-model="registerForm.forceEmailVerify" />
          </el-form-item>
          <el-form-item label="是否启用邀请码">
            <el-switch v-model="registerForm.needInviteCode" />
          </el-form-item>
          <el-form-item label="默认会员方案">
            <el-select v-model="registerForm.defaultMemberPlan" style="width: 220px">
              <el-option label="免费版" value="免费版" />
              <el-option label="专业版" value="专业版" />
              <el-option label="团队版" value="团队版" />
            </el-select>
          </el-form-item>
          <el-form-item label="新用户自动激活">
            <el-switch v-model="registerForm.autoActivate" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="登录与安全" name="security">
        <el-form label-width="160px" class="config-form">
          <el-form-item label="登录方式">
            <el-radio-group v-model="securityForm.loginMode">
              <el-radio label="账号密码" />
              <el-radio label="邮箱验证码" />
              <el-radio label="混合模式" />
            </el-radio-group>
          </el-form-item>
          <el-form-item label="登录验证码开关">
            <el-switch v-model="securityForm.enableCaptcha" />
          </el-form-item>
          <el-form-item label="连续失败锁定阈值">
            <el-input-number v-model="securityForm.failLockThreshold" :min="3" :max="20" />
            <span class="inline-tip">次</span>
          </el-form-item>
          <el-form-item label="锁定时长">
            <el-input-number v-model="securityForm.lockMinutes" :min="5" :max="180" />
            <span class="inline-tip">分钟</span>
          </el-form-item>
          <el-form-item label="启用双因子认证">
            <el-switch v-model="securityForm.enable2FA" />
          </el-form-item>
          <el-form-item label="密码复杂度策略">
            <el-checkbox-group v-model="securityForm.passwordPolicy">
              <el-checkbox label="至少 8 位" />
              <el-checkbox label="包含大写字母" />
              <el-checkbox label="包含数字" />
              <el-checkbox label="包含特殊字符" />
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="Token 与会话" name="token">
        <el-form label-width="180px" class="config-form">
          <el-form-item label="Access Token 有效期（小时）">
            <el-input-number v-model="tokenForm.accessTokenHours" :min="1" :max="24" />
          </el-form-item>
          <el-form-item label="Refresh Token 有效期（天）">
            <el-input-number v-model="tokenForm.refreshTokenDays" :min="1" :max="30" />
          </el-form-item>
          <el-form-item label="允许并发会话数">
            <el-input-number v-model="tokenForm.maxSessionCount" :min="1" :max="20" />
          </el-form-item>
          <el-form-item label="异地登录提醒">
            <el-switch v-model="tokenForm.notifyLoginFromNewIp" />
          </el-form-item>
          <el-form-item label="空闲会话超时（分钟）">
            <el-input-number v-model="tokenForm.idleTimeoutMinutes" :min="5" :max="480" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="IP 白名单" name="ip">
        <div class="toolbar mb-16">
          <el-input v-model="newIp" placeholder="输入 IPv4，如 192.168.1.100" style="width: 280px" class="mr-10" />
          <el-button type="primary" @click="handleAddIp">新增白名单</el-button>
        </div>
        <el-table :data="ipWhitelist" border stripe style="width: 100%">
          <el-table-column type="index" label="序号" width="70" />
          <el-table-column prop="ip" label="IP 地址" min-width="220" />
          <el-table-column prop="remark" label="备注" min-width="260" />
          <el-table-column prop="createTime" label="添加时间" width="180" align="center" />
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button type="danger" size="small" @click="removeIp(scope.row.ip)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <div class="footer-actions">
      <el-button @click="resetConfig">重置</el-button>
      <el-button type="primary" :loading="saving" @click="saveConfig">保存配置</el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as systemConfigApi from '../api/system-config'
import type { SystemConfigDTO } from '../api/system-config'

const loading = ref(false)
const saving = ref(false)
const activeTab = ref('register')
const newIp = ref('')

const registerForm = reactive({
  registerMethods: ['邮箱', '手机号'],
  forceEmailVerify: true,
  needInviteCode: false,
  defaultMemberPlan: '免费版',
  autoActivate: true
})

const securityForm = reactive({
  loginMode: '混合模式',
  enableCaptcha: true,
  failLockThreshold: 5,
  lockMinutes: 30,
  enable2FA: false,
  passwordPolicy: ['至少 8 位', '包含数字', '包含特殊字符']
})

const tokenForm = reactive({
  accessTokenHours: 2,
  refreshTokenDays: 7,
  maxSessionCount: 3,
  notifyLoginFromNewIp: true,
  idleTimeoutMinutes: 120
})

const ipWhitelist = ref<Array<{ ip: string; remark: string; createTime: string }>>([])

// 加载系统配置
async function loadConfig() {
  loading.value = true
  try {
    const data = await systemConfigApi.fetchSystemConfig()
    
    // 设置注册配置
    if (data.register) {
      registerForm.registerMethods = data.register.registerMethods || ['邮箱', '手机号']
      registerForm.forceEmailVerify = data.register.forceEmailVerify ?? true
      registerForm.needInviteCode = data.register.needInviteCode ?? false
      registerForm.defaultMemberPlan = data.register.defaultMemberPlan || '免费版'
      registerForm.autoActivate = data.register.autoActivate ?? true
    }
    
    // 设置安全配置
    if (data.security) {
      securityForm.loginMode = data.security.loginMode || '混合模式'
      securityForm.enableCaptcha = data.security.enableCaptcha ?? true
      securityForm.failLockThreshold = data.security.failLockThreshold ?? 5
      securityForm.lockMinutes = data.security.lockMinutes ?? 30
      securityForm.enable2FA = data.security.enable2FA ?? false
      securityForm.passwordPolicy = data.security.passwordPolicy || ['至少 8 位', '包含数字', '包含特殊字符']
    }
    
    // 设置Token配置
    if (data.token) {
      tokenForm.accessTokenHours = data.token.accessTokenHours ?? 2
      tokenForm.refreshTokenDays = data.token.refreshTokenDays ?? 7
      tokenForm.maxSessionCount = data.token.maxSessionCount ?? 3
      tokenForm.notifyLoginFromNewIp = data.token.notifyLoginFromNewIp ?? true
      tokenForm.idleTimeoutMinutes = data.token.idleTimeoutMinutes ?? 120
    }
    
    // 设置IP白名单
    ipWhitelist.value = data.ipWhitelist || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载系统配置失败')
  } finally {
    loading.value = false
  }
}

async function handleAddIp() {
  const ip = newIp.value.trim()
  const ipReg = /^(25[0-5]|2[0-4]\d|[01]?\d?\d)(\.(25[0-5]|2[0-4]\d|[01]?\d?\d)){3}$/
  if (!ipReg.test(ip)) {
    ElMessage.warning('请输入合法 IPv4 地址')
    return
  }
  if (ipWhitelist.value.some((item) => item.ip === ip)) {
    ElMessage.warning('该 IP 已存在')
    return
  }
  
  try {
    await systemConfigApi.addIpWhitelist(ip, '手动添加')
    ipWhitelist.value.unshift({
      ip,
      remark: '手动添加',
      createTime: new Date().toLocaleString().replace(/\//g, '-')
    })
    newIp.value = ''
    ElMessage.success('IP 白名单添加成功')
  } catch (error: any) {
    ElMessage.error(error.message || '添加IP失败')
  }
}

async function removeIp(ip: string) {
  try {
    await systemConfigApi.removeIpWhitelist(ip)
    ipWhitelist.value = ipWhitelist.value.filter((item) => item.ip !== ip)
    ElMessage.success('已删除白名单 IP')
  } catch (error: any) {
    ElMessage.error(error.message || '删除IP失败')
  }
}

function resetConfig() {
  registerForm.registerMethods = ['邮箱', '手机号']
  registerForm.forceEmailVerify = true
  registerForm.needInviteCode = false
  registerForm.defaultMemberPlan = '免费版'
  registerForm.autoActivate = true

  securityForm.loginMode = '混合模式'
  securityForm.enableCaptcha = true
  securityForm.failLockThreshold = 5
  securityForm.lockMinutes = 30
  securityForm.enable2FA = false
  securityForm.passwordPolicy = ['至少 8 位', '包含数字', '包含特殊字符']

  tokenForm.accessTokenHours = 2
  tokenForm.refreshTokenDays = 7
  tokenForm.maxSessionCount = 3
  tokenForm.notifyLoginFromNewIp = true
  tokenForm.idleTimeoutMinutes = 120

  ElMessage.success('已恢复默认配置')
}

async function saveConfig() {
  saving.value = true
  try {
    const configData: SystemConfigDTO = {
      register: { ...registerForm },
      security: { ...securityForm },
      token: { ...tokenForm },
      ipWhitelist: [...ipWhitelist.value]
    }
    await systemConfigApi.saveSystemConfig(configData)
    ElMessage.success('系统配置保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存系统配置失败')
  } finally {
    saving.value = false
  }
}

// 页面加载时获取配置
onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

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

.config-form {
  max-width: 760px;
  padding: 8px 0;
}

.inline-tip {
  margin-left: 12px;
  color: #6b7280;
  font-size: 14px;
}

.toolbar {
  display: flex;
  align-items: center;
}

.footer-actions {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.mr-10 {
  margin-right: 10px;
}

.mb-16 {
  margin-bottom: 16px;
}

.mb-20 {
  margin-bottom: 20px;
}

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
</style>
