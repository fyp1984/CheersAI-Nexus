<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, sendCode } from '../api/auth'
import { getErrorMessage } from '../utils/api'

const router = useRouter()
const registerType = ref<'email' | 'phone'>('email')
const loading = ref(false)
const codeSending = ref(false)
const codeCountdown = ref(0)

const form = reactive({
  username: '',
  email: '',
  phone: '',
  code: '',
  password: '',
  confirmPassword: '',
  agree: true
})

async function handleSendCode() {
  const target = registerType.value === 'email' ? form.email.trim() : form.phone.trim()
  if (!target) {
    ElMessage.warning(registerType.value === 'email' ? '请先输入邮箱' : '请先输入手机号')
    return
  }

  if (codeSending.value || codeCountdown.value > 0) {
    return
  }

  codeSending.value = true
  try {
    await sendCode(
      registerType.value === 'email'
        ? { email: target, purpose: 'register' }
        : { phone: target, purpose: 'register' }
    )
    ElMessage.success('验证码已发送')
    codeCountdown.value = 60
    const timer = setInterval(() => {
      codeCountdown.value -= 1
      if (codeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '验证码发送失败'))
  } finally {
    codeSending.value = false
  }
}

async function handleRegister() {
  if (!form.username || !form.password || !form.confirmPassword || !form.code) {
    ElMessage.warning('请完整填写注册信息')
    return
  }

  if (registerType.value === 'email' && !form.email.trim()) {
    ElMessage.warning('请输入邮箱')
    return
  }

  if (registerType.value === 'phone' && !form.phone.trim()) {
    ElMessage.warning('请输入手机号')
    return
  }

  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次输入密码不一致')
    return
  }

  if (!form.agree) {
    ElMessage.warning('请先同意服务协议与隐私政策')
    return
  }

  loading.value = true
  try {
    await register({
      username: form.username.trim(),
      password: form.password,
      code: form.code.trim(),
      ...(registerType.value === 'email'
        ? { email: form.email.trim() }
        : { phone: form.phone.trim() })
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '注册失败'))
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
            <div class="header-subtitle">按要求提示完成邮箱/手机号注册</div>
          </div>
        </div>
      </template>

      <el-tabs v-model="registerType" class="mb-16">
        <el-tab-pane label="邮箱注册" name="email" />
        <el-tab-pane label="手机号注册" name="phone" />
      </el-tabs>

      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" clearable />
        </el-form-item>

        <el-form-item :label="registerType === 'email' ? '邮箱' : '手机号'">
          <el-input
            v-if="registerType === 'email'"
            v-model="form.email"
            placeholder="请输入邮箱"
            clearable
          />
          <el-input v-else v-model="form.phone" placeholder="请输入手机号" clearable />
        </el-form-item>

        <el-form-item :label="registerType === 'email' ? '邮箱验证码' : '短信验证码'">
          <div class="inline-row">
            <el-input v-model="form.code" placeholder="请输入 6 位验证码" />
            <el-button :loading="codeSending" :disabled="codeCountdown > 0" @click="handleSendCode">
              {{ codeCountdown > 0 ? `${codeCountdown}s 后重试` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="登录密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>

        <el-checkbox v-model="form.agree" class="mb-16">已阅读并同意《服务协议》《隐私政策》</el-checkbox>

        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">注册</el-button>
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
  background: linear-gradient(145deg, #edf5ff 0%, #f7f9fc 40%, #eef2f9 100%);
  padding: 20px;
}

.register-card {
  width: min(620px, 100%);
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

.submit-btn {
  width: 100%;
}

.bottom-actions {
  margin-top: 12px;
  text-align: right;
}

.mb-16 {
  margin-bottom: 16px;
}
</style>
