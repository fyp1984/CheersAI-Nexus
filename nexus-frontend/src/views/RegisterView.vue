<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = reactive({
  username: '',
  account: '',
  password: '',
  confirmPassword: ''
})

function handleRegister() {
  // 框架阶段：后续替换成真实注册接口
  router.push('/login')
}
</script>

<template>
  <div class="register-page">
    <el-card class="register-card" shadow="never">
      <template #header>
        <h2>CheersAI Nexus 注册</h2>
      </template>
      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="账号">
          <el-input v-model="form.account" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入密码"
          />
        </el-form-item>
        <el-button type="primary" class="submit-btn" @click="handleRegister">注册</el-button>
      </el-form>
      <div class="actions">
        <el-button link type="primary" @click="router.push('/login')">返回登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #e6f4ff 0%, #f5f8ff 100%);
  padding: 16px;
}

.register-card {
  width: min(100%, 460px);
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
