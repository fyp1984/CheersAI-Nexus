import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../store/modules/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录 / SSO' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('../layout/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/DashboardView.vue'),
        meta: { title: '数据分析', priority: 'P2' }
      },
      {
        path: 'product/list',
        name: 'ProductList',
        component: () => import('../views/ProductListView.vue'),
        meta: { title: '产品管理', priority: 'P0' }
      },
      {
        path: 'pricing/config',
        name: 'PricingConfig',
        component: () => import('../views/PricingConfigView.vue'),
        meta: { title: '功能定价', priority: 'P1' }
      },
      {
        path: 'user/list',
        name: 'UserList',
        component: () => import('../views/UserListView.vue'),
        meta: { title: '用户管理', priority: 'P0' }
      },
      {
        path: 'member/plans',
        name: 'MemberPlans',
        component: () => import('../views/MemberPlansView.vue'),
        meta: { title: '会员管理', priority: 'P0' }
      },
      {
        path: 'subscription/management',
        name: 'SubscriptionManagement',
        component: () => import('../views/SubscriptionManagementView.vue'),
        meta: { title: '订阅管理', priority: 'P1' }
      },
      {
        path: 'feedback/list',
        name: 'FeedbackList',
        component: () => import('../views/FeedbackListView.vue'),
        meta: { title: '用户反馈', priority: 'P0' }
      },
      {
        path: 'desktop/members',
        name: 'DesktopMembers',
        component: () => import('../views/DesktopMemberView.vue'),
        meta: { title: '桌面会员', priority: 'P1' }
      },
      {
        path: 'desktop/api-keys',
        name: 'ApiKeys',
        component: () => import('../views/ApiKeyView.vue'),
        meta: { title: 'API 密钥', priority: 'P1' }
      },
      {
        path: 'audit/logs',
        name: 'AuditLogs',
        component: () => import('../views/AuditLogsView.vue'),
        meta: { title: '审计日志', priority: 'P1' }
      },
      {
        path: 'notice/list',
        name: 'NoticeList',
        component: () => import('../views/NoticeListView.vue'),
        meta: { title: '公告系统', priority: 'P1' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const pageTitle = (to.meta.title as string | undefined) || 'CheersAI Nexus'
  document.title = `${pageTitle} - CheersAI Nexus`

  const authStore = useAuthStore()
  const isPublicRoute = to.path === '/login' || to.path === '/register'

  if (!isPublicRoute && !authStore.isAuthenticated) {
    return '/login'
  }

  if (isPublicRoute && authStore.isAuthenticated) {
    return '/dashboard'
  }

  return true
})

export default router
