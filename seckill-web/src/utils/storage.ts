const TOKEN_KEY = 'token'
const USER_KEY = 'user'

/**
 * 获取 Token
 */
export function getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置 Token
 */
export function setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除 Token
 */
export function removeToken(): void {
    localStorage.removeItem(TOKEN_KEY)
}

/**
 * 获取用户信息
 */
export function getUser<T>(): T | null {
    const userStr = localStorage.getItem(USER_KEY)
    if (!userStr) return null
    try {
        return JSON.parse(userStr) as T
    } catch {
        return null
    }
}

/**
 * 设置用户信息
 */
export function setUser<T>(user: T): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
}

/**
 * 移除用户信息
 */
export function removeUser(): void {
    localStorage.removeItem(USER_KEY)
}

/**
 * 清除所有认证信息
 */
export function clearAuth(): void {
    removeToken()
    removeUser()
}
