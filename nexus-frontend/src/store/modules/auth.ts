import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY } from '../../constants/auth'
import { fetchCurrentUser } from '../../api/auth'
import type { UserInfo } from '../../types/auth'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string>(localStorage.getItem(ACCESS_TOKEN_KEY) || '')
  const refreshToken = ref<string>(localStorage.getItem(REFRESH_TOKEN_KEY) || '')
  const user = ref<UserInfo | null>(null)
  const initialized = ref(false)

  const token = computed(() => accessToken.value)
  const isAuthenticated = computed(() => accessToken.value.length > 0)

  function setAuth(payload: { accessToken: string; refreshToken?: string; user?: UserInfo | null }) {
    accessToken.value = payload.accessToken
    localStorage.setItem(ACCESS_TOKEN_KEY, payload.accessToken)
    if (payload.refreshToken !== undefined) {
      refreshToken.value = payload.refreshToken
      localStorage.setItem(REFRESH_TOKEN_KEY, payload.refreshToken)
    }
    if (payload.user !== undefined) {
      user.value = payload.user
    }
  }

  function setToken(value: string) {
    setAuth({ accessToken: value })
  }

  function setUser(value: UserInfo | null) {
    user.value = value
  }

  async function initializeAuth() {
    if (initialized.value) return

    if (!accessToken.value) {
      initialized.value = true
      return
    }

    try {
      user.value = await fetchCurrentUser()
    } catch {
      clearToken()
    } finally {
      initialized.value = true
    }
  }

  function clearToken() {
    accessToken.value = ''
    refreshToken.value = ''
    user.value = null
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  }

  return {
    accessToken,
    refreshToken,
    user,
    initialized,
    token,
    isAuthenticated,
    setAuth,
    setToken,
    setUser,
    initializeAuth,
    clearToken
  }
})
