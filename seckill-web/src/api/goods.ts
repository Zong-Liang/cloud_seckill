import request from './request'
import type { ApiResponse, GoodsVO } from '@/types'

/**
 * 获取商品列表（所有状态）
 */
export function getGoodsList(): Promise<ApiResponse<GoodsVO[]>> {
  return request.get('/stock/goods/all')
}

/**
 * 获取商品详情
 */
export function getGoodsDetail(id: number): Promise<ApiResponse<GoodsVO>> {
    return request.get(`/stock/goods/${id}`)
}
