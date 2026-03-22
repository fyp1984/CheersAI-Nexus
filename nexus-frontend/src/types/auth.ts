export type UserInfo = {
  id: string
  email?: string
  phone?: string
  username?: string
  nickname?: string
  avatarUrl?: string
}

export type AuthResponse = {
  accessToken: string
  refreshToken: string
  idToken: string
  expiresIn: number
  tokenType: string
  user: UserInfo
}
