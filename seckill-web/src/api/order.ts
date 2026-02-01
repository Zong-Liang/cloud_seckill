import request from './request'
import type { ApiResponse, OrderVO } from '@/types'

/**
 * 获取当前用户订单列表
 */
export function getMyOrders(): Promise<ApiResponse<OrderVO[]>> {
    return request.get('/order/list')
}

/**
 * 根据订单 ID 获取订单详情
 */
export function getOrderById(id: number): Promise<ApiResponse<OrderVO>> {
    return request.get(`/order/${id}`)
}

/**
 * 根据订单号获取订单详情
 */
export function getOrderByNo(orderNo: number | string): Promise<ApiResponse<OrderVO>> {
    return request.get(`/order/no/${orderNo}`)
}

/**
 * 检查用户是否已下单（内部调用）
 */
export function checkOrder(userId: number, goodsId: number): Promise<ApiResponse<boolean>> {
    return request.get('/order/check', {
        params: { userId, goodsId }
    })
}

/**
 * 支付订单（模拟）
 */
export function payOrder(orderNo: number | string): Promise<ApiResponse<boolean>> {
    return request.post(`/order/pay/${orderNo}`)
}

/**
 * 取消订单
 */
export function cancelOrder(orderNo: number | string): Promise<ApiResponse<boolean>> {
    return request.post(`/order/cancel/${orderNo}`)
}
