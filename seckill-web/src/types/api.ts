/**
 * API 统一响应格式
 */
export interface ApiResponse<T = unknown> {
    code: number
    message: string
    data: T
    timestamp?: number
}

/**
 * 分页请求参数
 */
export interface PageQuery {
    page?: number
    pageSize?: number
}

/**
 * 分页响应
 */
export interface PageResult<T> {
    records: T[]
    total: number
    page: number
    pageSize: number
}
