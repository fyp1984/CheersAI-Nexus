<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { DEV_LOGIN_IDENTITY, DEV_LOGIN_PASSWORD } from '../constants/auth'
import { useAuthStore } from '../store/modules/auth'
import { getErrorMessage } from '../utils/api'

const router = useRouter()
const authStore = useAuthStore()

const loginType = ref<'account' | 'email'>('account')
const loading = ref(false)
const form = reactive({
  identity: DEV_LOGIN_IDENTITY,
  password: DEV_LOGIN_PASSWORD
})

const emailForm = reactive({
  email: '',
  code: '',
  agree: true
})

function handleLogin() {
  if (loginType.value === 'account') {
    if (!accountForm.account || !accountForm.password || !accountForm.captcha) {
      ElMessage.warning('请填写账号登录所需信息')
      return
    }
  } else {
    if (!emailForm.email || !emailForm.code) {
      ElMessage.warning('请填写邮箱登录所需信息')
      return
    }
    if (!emailForm.agree) {
      ElMessage.warning('请先同意服务协议与隐私政策')
      return
    }
  }

  loading.value = true
  try {
    const authData = await login(payload)
    authStore.setAuth({
      accessToken: authData.accessToken,
      refreshToken: authData.refreshToken,
      user: authData.user
    })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '登录失败'))
  } finally {
    loading.value = false
  }
  if (smsCountdown.value > 0 || smsSending.value) return

  smsSending.value = true
  setTimeout(() => {
    smsSending.value = false
    smsCountdown.value = 60
    ElMessage.success('验证码已发送（模拟）')
    const timer = setInterval(() => {
      smsCountdown.value -= 1
      if (smsCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  }, 400)
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <div class="header-title">登录</div>
            <div class="header-subtitle">进入 CheersAI Nexus 运营管理平台</div>
          </div>
          <el-tag type="danger">P0</el-tag>
        </div>
      </template>

      <el-form label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="邮箱或手机号">
          <el-input v-model="form.identity" placeholder="请输入邮箱或手机号" clearable />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">登录</el-button>

      <div class="dev-hint">开发环境已预置测试账号和密码，启动后点击登录即可直接进入页面检查。</div>

      <div class="bottom-actions">
        <el-button link type="primary" @click="router.push('/register')">注册账号</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, #edf5ff 0%, #f7f9fc 40%, #eef2f9 100%);
  padding: 20px;
}

.auth-card {
  width: min(460px, 100%);
  border-radius: 14px;
  border: 1px solid #e8edf5;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.header-subtitle {
  margin-top: 4px;
  color: #6b7280;
  font-size: 13px;
}

.inline-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.inline-row.between {
  justify-content: space-between;
}

.captcha-mock {
  min-width: 96px;
  height: 40px;
  border-radius: 8px;
  border: 1px dashed #c7d2fe;
  color: #1d4ed8;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fbff;
}

.submit-btn {
  width: 100%;
}

.dev-hint {
  margin-top: 12px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.6;
}

.bottom-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.mb-16 {
  margin-bottom: 16px;
}
</style>
