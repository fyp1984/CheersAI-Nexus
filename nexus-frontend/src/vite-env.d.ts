/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
  readonly VITE_AES_KEY: string
  readonly VITE_HMAC_KEY: string
  readonly VITE_DEV_LOGIN_IDENTITY?: string
  readonly VITE_DEV_LOGIN_PASSWORD?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
