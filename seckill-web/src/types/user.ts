/**
 * 用户信息
 */
export interface User {
    id: number
    username: string
    nickname: string
    phone: string
    avatar: string
    status: number
}

/**
 * 用户视图对象（包含 token）
 */
export interface UserVO extends User {
    token?: string
}

/**
 * 登录请求参数
 */
export interface LoginDTO {
    username: string
    password: string
}

/**
 * 注册请求参数
 */
export interface RegisterDTO {
    username: string
    password: string
    confirmPassword?: string
    nickname?: string
    phone?: string
}

/**
 * 用户状态枚举
 */
export enum UserStatus {
    DISABLED = 0,
    NORMAL = 1,
}
