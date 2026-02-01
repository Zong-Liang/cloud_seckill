import request from './request'
import type { ApiResponse, LoginDTO, RegisterDTO, UserVO } from '@/types'

/**
 * 用户登录
 */
export function login(data: LoginDTO): Promise<ApiResponse<UserVO>> {
    return request.post('/user/login', data)
}

/**
 * 用户注册
 */
export function register(data: RegisterDTO): Promise<ApiResponse<UserVO>> {
    return request.post('/user/register', data)
}

/**
 * 获取当前用户信息
 */
export function getUserInfo(): Promise<ApiResponse<UserVO>> {
    return request.get('/user/info')
}
