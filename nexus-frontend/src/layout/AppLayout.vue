<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { logout } from '../api/auth'
import { useAuthStore } from '../store/modules/auth'
import LogoPng from '../assets/logo.png'

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
  { index: '/subscription/management', label: '订阅管理' },
  { index: '/feedback/list', label: '用户反馈' },
  { index: '/audit/logs', label: '审计日志' },
  { index: '/notice/list', label: '公告系统' },
  { index: '/system/config', label: '系统配置' }
]

async function handleLogout() {
  authStore.clearToken()
  await router.replace('/login')
  try {
    await logout()
  } catch {
    ElMessage.warning('退出请求失败，已清理本地登录态')
  }
}
</script>

<template>
  <el-container class="app-layout">
    <el-aside width="256px" class="layout-aside">
      <div class="logo-area">
        <div class="logo-badge">
          <img :src="LogoPng" alt="CheersAI Nexus" class="logo-img" />
        </div>
        <div class="logo-text">
          <span class="logo-name">CheersAI Nexus</span>
          <span class="logo-slogan">安全可控的AI协作平台</span>
        </div>
      </div>
      <el-menu :default-active="activeMenu" router class="sidebar-menu">
        <el-menu-item v-for="menu in menus" :key="menu.index" :index="menu.index">
          {{ menu.label }}
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <div class="user-info">
          <div class="user-avatar">管</div>
          <div class="user-details">
            <span class="user-email">admin@cheersai.com</span>
            <span class="user-role">管理员</span>
          </div>
        </div>
      </div>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <span class="system-status">
            <span class="status-dot"></span>
            系统运行正常
          </span>
        </div>
        <div class="header-right">
          <el-button type="primary" plain size="small" @click="handleLogout">退出</el-button>
        </div>
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
}

.layout-aside {
  background: linear-gradient(180deg, #111827 0%, #1f2937 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  width: 256px !important;
}

.logo-area {
  height: 108px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-badge {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.logo-img {
  width: 48px;
  height: 48px;
  object-fit: contain;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-name {
  color: #ffffff;
  font-weight: 700;
  font-size: 16px;
  line-height: 1.2;
}

.logo-slogan {
  color: rgba(255, 255, 255, 0.6);
  font-size: 11px;
  line-height: 1.2;
}

.sidebar-menu {
  flex: 1;
  background: transparent;
  border-right: none;
  padding: 12px 8px;
}

:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  padding: 0 16px !important;
  margin-bottom: 4px;
  border-radius: 8px;
  color: #d1d5db;
  font-size: 14px;
  transition: all 0.2s ease;
}

:deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.05);
  color: #ffffff;
}

:deep(.el-menu-item.is-active) {
  background: #3b82f6;
  color: #ffffff;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 500;
  font-size: 14px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-email {
  color: #ffffff;
  font-size: 13px;
  font-weight: 500;
}

.user-role {
  color: rgba(255, 255, 255, 0.5);
  font-size: 11px;
}

.layout-header {
  height: 64px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
}

.header-left {
  display: flex;
  align-items: center;
}

.system-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.layout-main {
  background: #f9fafb;
  padding: 32px;
  overflow-y: auto;
}
</style>
