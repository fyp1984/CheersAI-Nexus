<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { login } from '../api/auth'
import { DEV_LOGIN_IDENTITY, DEV_LOGIN_PASSWORD } from '../constants/auth'
import { useAuthStore } from '../store/modules/auth'
import { getErrorMessage } from '../utils/api'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  identity: DEV_LOGIN_IDENTITY,
  password: DEV_LOGIN_PASSWORD
})

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const phonePattern = /^1[3-9]\d{9}$/
const loginPasswordPattern = /^.{6,32}$/

const rules: FormRules<typeof form> = {
  identity: [
    { required: true, message: '请输入邮箱或手机号', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        const text = (value || '').trim()
        if (!text) {
          callback(new Error('请输入邮箱或手机号'))
          return
        }
        if (text.includes('@')) {
          if (!emailPattern.test(text)) {
            callback(new Error('邮箱格式不正确'))
            return
          }
          callback()
          return
        }
        if (!phonePattern.test(text)) {
          callback(new Error('手机号格式不正确'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (!loginPasswordPattern.test(value || '')) {
          callback(new Error('密码格式不正确，请输入6-32位'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  const identity = form.identity.trim()
  const payload: { email?: string; phone?: string; password: string } = {
    password: form.password.trim()
  }
  if (emailPattern.test(identity)) {
    payload.email = identity
  } else {
    payload.phone = identity
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
    await router.push('/dashboard')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '登录失败，请稍后重试'))
  } finally {
    loading.value = false
  }
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

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="邮箱或手机号" prop="identity">
          <el-input v-model="form.identity" placeholder="请输入邮箱或手机号" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button native-type="submit" type="primary" class="submit-btn" :loading="loading">登录</el-button>
      </el-form>

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
  background: linear-gradient(180deg, #111827 0%, #1f2937 100%);
  padding: 20px;
}

.auth-card {
  width: min(460px, 100%);
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 20px 25px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.header-subtitle {
  margin-top: 4px;
  color: #6b7280;
  font-size: 14px;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}

.dev-hint {
  margin-top: 16px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
}

.bottom-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
