<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/modules/auth'

const router = useRouter()
const authStore = useAuthStore()

const loginType = ref<'account' | 'email'>('account')
const loading = ref(false)
const smsSending = ref(false)
const smsCountdown = ref(0)

const accountForm = reactive({
  account: '',
  password: '',
  captcha: '',
  remember: true
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
  setTimeout(() => {
    loading.value = false
    authStore.setToken('mock-nexus-token')
    ElMessage.success('登录成功')
    router.push('/dashboard')
  }, 500)
}

function sendEmailCode() {
  if (!emailForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
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

        <el-tabs v-model="loginType" class="mb-16">
          <el-tab-pane label="账号密码" name="account" />
          <el-tab-pane label="邮箱验证码" name="email" />
        </el-tabs>

        <el-form v-if="loginType === 'account'" label-position="top">
          <el-form-item label="账号">
            <el-input v-model="accountForm.account" placeholder="请输入账号/手机号/邮箱" clearable />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="accountForm.password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-form-item label="图形验证码">
            <div class="inline-row">
              <el-input v-model="accountForm.captcha" placeholder="请输入验证码" />
              <div class="captcha-mock">A7K9</div>
            </div>
          </el-form-item>
          <div class="inline-row between mb-16">
            <el-checkbox v-model="accountForm.remember">7 天内免登录</el-checkbox>
            <el-button link type="primary">忘记密码</el-button>
          </div>
        </el-form>

        <el-form v-else label-position="top">
          <el-form-item label="邮箱">
            <el-input v-model="emailForm.email" placeholder="请输入邮箱" clearable />
          </el-form-item>
          <el-form-item label="邮箱验证码">
            <div class="inline-row">
              <el-input v-model="emailForm.code" placeholder="请输入 6 位验证码" />
              <el-button :loading="smsSending" :disabled="smsCountdown > 0" @click="sendEmailCode">
                {{ smsCountdown > 0 ? `${smsCountdown}s 后重试` : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-checkbox v-model="emailForm.agree" class="mb-16">
            已阅读并同意《服务协议》《隐私政策》
          </el-checkbox>
        </el-form>

        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">登录</el-button>

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

.bottom-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.mb-16 {
  margin-bottom: 16px;
}
</style>
