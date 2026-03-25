const textEncoder = new TextEncoder()
const textDecoder = new TextDecoder()

let cachedAesKey: CryptoKey | null = null
let cachedHmacKey: CryptoKey | null = null

type CipherPayload = {
  v: number
  iv: string
  data: string
}

function assertEnv(name: 'VITE_AES_KEY' | 'VITE_HMAC_KEY'): string {
  const value = import.meta.env[name]
  if (!value) {
    throw new Error(`${name} is required for secure communication`)
  }
  return value
}

function toBase64(bytes: Uint8Array): string {
  let binary = ''
  bytes.forEach((b) => {
    binary += String.fromCharCode(b)
  })
  return btoa(binary)
}

function fromBase64(base64: string): Uint8Array {
  const binary = atob(base64)
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i += 1) {
    bytes[i] = binary.charCodeAt(i)
  }
  return bytes
}

async function deriveAesRawKey(secret: string): Promise<ArrayBuffer> {
  return crypto.subtle.digest('SHA-256', textEncoder.encode(secret))
}

async function getAesKey(): Promise<CryptoKey> {
  if (cachedAesKey) {
    return cachedAesKey
  }
  const raw = await deriveAesRawKey(assertEnv('VITE_AES_KEY'))
  cachedAesKey = await crypto.subtle.importKey('raw', raw, 'AES-GCM', false, ['encrypt', 'decrypt'])
  return cachedAesKey
}

async function getHmacKey(): Promise<CryptoKey> {
  if (cachedHmacKey) {
    return cachedHmacKey
  }
  cachedHmacKey = await crypto.subtle.importKey(
    'raw',
    textEncoder.encode(assertEnv('VITE_HMAC_KEY')),
    { name: 'HMAC', hash: 'SHA-256' },
    false,
    ['sign', 'verify']
  )
  return cachedHmacKey
}

function toCanonicalText(data: unknown): string {
  return JSON.stringify(data ?? {})
}

export async function encryptPayload(data: unknown): Promise<string> {
  const key = await getAesKey()
  const iv = crypto.getRandomValues(new Uint8Array(12))
  const plain = textEncoder.encode(toCanonicalText(data))
  const encrypted = await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, plain)
  const payload: CipherPayload = {
    v: 1,
    iv: toBase64(iv),
    data: toBase64(new Uint8Array(encrypted))
  }
  return toBase64(textEncoder.encode(JSON.stringify(payload)))
}

export async function decryptPayload(input: string): Promise<unknown> {
  const key = await getAesKey()
  const payloadText = textDecoder.decode(fromBase64(input))
  const payload = JSON.parse(payloadText) as CipherPayload
  const iv = fromBase64(payload.iv)
  const data = fromBase64(payload.data)
  const decrypted = await crypto.subtle.decrypt({ name: 'AES-GCM', iv }, key, data)
  const plainText = textDecoder.decode(new Uint8Array(decrypted))
  try {
    return JSON.parse(plainText)
  } catch {
    return plainText
  }
}

export async function signPayload(data: unknown): Promise<string> {
  const key = await getHmacKey()
  const signature = await crypto.subtle.sign('HMAC', key, textEncoder.encode(toCanonicalText(data)))
  return toBase64(new Uint8Array(signature))
}

export async function verifyPayloadSignature(data: unknown, signature: string): Promise<boolean> {
  const key = await getHmacKey()
  return crypto.subtle.verify(
    'HMAC',
    key,
    fromBase64(signature),
    textEncoder.encode(toCanonicalText(data))
  )
}
