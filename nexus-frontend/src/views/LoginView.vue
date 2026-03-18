<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/modules/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  account: '',
  password: ''
})

function handleLogin() {
  authStore.setToken('mock-nexus-token')
  router.push('/dashboard')
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card" shadow="never">
      <template #header>
        <h2>CheersAI Nexus 登录</h2>
      </template>
      <el-form label-position="top">
        <el-form-item label="账号">
          <el-input v-model="form.account" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" class="submit-btn" @click="handleLogin">登录</el-button>
      </el-form>
      <div class="actions">
        <el-button link type="primary" @click="router.push('/register')">去注册</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #e6f4ff 0%, #f5f8ff 100%);
  padding: 16px;
}

.login-card {
  width: min(100%, 420px);
}

h2 {
  margin: 0;
}

.submit-btn {
  width: 100%;
}

.actions {
  margin-top: 8px;
  text-align: right;
}
</style>
