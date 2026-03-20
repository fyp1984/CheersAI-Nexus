import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY } from '../../constants/auth'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string>(localStorage.getItem(ACCESS_TOKEN_KEY) || '')
  const refreshToken = ref<string>(localStorage.getItem(REFRESH_TOKEN_KEY) || '')

  const token = computed(() => accessToken.value)
  const isAuthenticated = computed(() => accessToken.value.length > 0)

  function setAuth(tokens: { accessToken: string; refreshToken?: string }) {
    accessToken.value = tokens.accessToken
    localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken)
    if (tokens.refreshToken !== undefined) {
      refreshToken.value = tokens.refreshToken
      localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken)
    }
  }

  function setToken(value: string) {
    setAuth({ accessToken: value })
  }

  function clearToken() {
    accessToken.value = ''
    refreshToken.value = ''
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  }

  return {
    accessToken,
    refreshToken,
    token,
    isAuthenticated,
    setAuth,
    setToken,
    clearToken
  }
})
