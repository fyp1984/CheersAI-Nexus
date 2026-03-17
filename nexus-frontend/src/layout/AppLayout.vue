<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/modules/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)

const menus = [
  { index: '/dashboard', label: '数据分析' },
  { index: '/product/list', label: '产品管理' },
  { index: '/pricing/config', label: '功能定价' },
  { index: '/user/list', label: '用户管理' },
  { index: '/member/plans', label: '会员管理' },
  { index: '/feedback/list', label: '用户反馈' },
  { index: '/audit/logs', label: '审计日志' },
  { index: '/notice/list', label: '公告系统' },
  { index: '/system/config', label: '系统配置' }
]

function handleLogout() {
  authStore.clearToken()
  router.push('/login')
}
</script>

<template>
  <el-container class="app-layout">
    <el-aside width="220px" class="layout-aside">
      <div class="logo">CheersAI Nexus</div>
      <el-menu :default-active="activeMenu" router>
        <el-menu-item v-for="menu in menus" :key="menu.index" :index="menu.index">
          {{ menu.label }}
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <span class="header-title">运营管理平台</span>
        <el-button type="primary" plain size="small" @click="handleLogout">退出</el-button>
      </el-header>
      <el-main class="layout-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  position: relative;
  --layout-header-height: 60px;
}

.app-layout::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: 220px;
  width: 1px;
  background: #dcdfe6;
  z-index: 3;
  pointer-events: none;
}

.layout-aside {
  background: #fff;
}

.logo {
  height: var(--layout-header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: var(--nexus-primary);
  border-bottom: 1px solid #dcdfe6;
}

.layout-header {
  height: var(--layout-header-height);
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
}

:deep(.el-menu) {
  border-right: none;
}

.header-title {
  font-weight: 600;
}

.layout-main {
  background: #f6f8fb;
}
</style>
