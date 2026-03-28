<script setup lang="ts">
import { onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { register as registerApi, sendCode as sendCodeApi } from '../api/auth'
import { getErrorMessage } from '../utils/api'

const router = useRouter()
const formRef = ref<FormInstance>()
const registerType = ref<'email' | 'mobile'>('email')
const loading = ref(false)
const codeSending = ref(false)
const codeCountdown = ref(0)

const form = reactive({
  username: '',
  email: '',
  mobile: '',
  verifyCode: '',
  password: '',
  confirmPassword: '',
  inviteCode: '',
  agree: true
})

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const phonePattern = /^1[3-9]\d{9}$/
const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,20}$/
const verifyCodePattern = /^\d{6}$/

const rules: FormRules<typeof form> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 32, message: '用户名长度需为2-32位', trigger: 'blur' }
  ],
  email: [
    {
      validator: (_rule, value: string, callback) => {
        if (registerType.value !== 'email') {
          callback()
          return
        }
        const text = (value || '').trim()
        if (!text) {
          callback(new Error('请输入邮箱'))
          return
        }
        if (!emailPattern.test(text)) {
          callback(new Error('邮箱格式不正确'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  mobile: [
    {
      validator: (_rule, value: string, callback) => {
        if (registerType.value !== 'mobile') {
          callback()
          return
        }
        const text = (value || '').trim()
        if (!text) {
          callback(new Error('请输入手机号'))
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
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (!verifyCodePattern.test((value || '').trim())) {
          callback(new Error('验证码格式不正确'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (!passwordPattern.test(value || '')) {
          callback(new Error('密码需为8-20位且包含大小写字母和数字'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入登录密码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  agree: [
    {
      validator: (_rule, value: boolean, callback) => {
        if (!value) {
          callback(new Error('请先同意服务协议与隐私政策'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ]
}

let countdownTimer: number | null = null

function clearCountdownTimer() {
  if (countdownTimer !== null) {
    window.clearInterval(countdownTimer)
    countdownTimer = null
  }
}

function startCountdown() {
  clearCountdownTimer()
  codeCountdown.value = 60
  countdownTimer = window.setInterval(() => {
    codeCountdown.value -= 1
    if (codeCountdown.value <= 0) {
      clearCountdownTimer()
    }
  }, 1000)
}

watch(registerType, () => {
  form.verifyCode = ''
  formRef.value?.clearValidate(['email', 'mobile', 'verifyCode'])
})

onBeforeUnmount(() => {
  clearCountdownTimer()
})

async function sendCode() {
  if (codeSending.value || codeCountdown.value > 0) return

  const identityField = registerType.value === 'email' ? 'email' : 'mobile'
  try {
    await formRef.value?.validateField(identityField)
  } catch {
    return
  }

  codeSending.value = true
  try {
    await sendCodeApi({
      purpose: 'register',
      email: registerType.value === 'email' ? form.email.trim() : undefined,
      phone: registerType.value === 'mobile' ? form.mobile.trim() : undefined
    })
    ElMessage.success('验证码已发送')
    startCountdown()
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '验证码发送失败'))
  } finally {
    codeSending.value = false
  }
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  loading.value = true
  try {
    await registerApi({
      username: form.username.trim(),
      password: form.password.trim(),
      code: form.verifyCode.trim(),
      email: registerType.value === 'email' ? form.email.trim() : undefined,
      phone: registerType.value === 'mobile' ? form.mobile.trim() : undefined
    })
    ElMessage.success('注册成功，请登录')
    await router.push('/login')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '注册失败，请稍后重试'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="register-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <div class="header-title">注册账号</div>
            <div class="header-subtitle">统一账号接入 CheersAI 全产品</div>
          </div>
          <el-tag type="danger">P0</el-tag>
        </div>
      </template>

      <el-tabs v-model="registerType" class="mb-16">
        <el-tab-pane label="邮箱注册" name="email" />
        <el-tab-pane label="手机号注册" name="mobile" />
      </el-tabs>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleRegister">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" clearable />
        </el-form-item>

        <el-form-item v-if="registerType === 'email'" label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入常用邮箱" clearable />
        </el-form-item>
        <el-form-item v-else label="手机号" prop="mobile">
          <el-input v-model="form.mobile" placeholder="请输入手机号" clearable />
        </el-form-item>

        <el-form-item :label="registerType === 'email' ? '邮箱验证码' : '短信验证码'" prop="verifyCode">
          <div class="inline-row">
            <el-input v-model="form.verifyCode" placeholder="请输入 6 位验证码" />
            <el-button :loading="codeSending" :disabled="codeCountdown > 0" @click="sendCode">
              {{ codeCountdown > 0 ? `${codeCountdown}s 后重试` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="登录密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="8-20 位且包含大小写字母和数字" />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入登录密码" />
        </el-form-item>

        <el-form-item label="邀请码（可选）">
          <el-input v-model="form.inviteCode" placeholder="用于内测或企业邀请场景" clearable />
        </el-form-item>

        <el-form-item prop="agree">
          <el-checkbox v-model="form.agree">已阅读并同意《服务协议》《隐私政策》</el-checkbox>
        </el-form-item>
        <el-button native-type="submit" type="primary" class="submit-btn" :loading="loading">注册</el-button>
      </el-form>

      <div class="bottom-actions">
        <el-button link type="primary" @click="router.push('/login')">返回登录</el-button>
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

.register-card {
  width: min(620px, 100%);
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

.inline-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}

.bottom-actions {
  margin-top: 16px;
  text-align: right;
}

.mb-16 {
  margin-bottom: 16px;
}
</style>
