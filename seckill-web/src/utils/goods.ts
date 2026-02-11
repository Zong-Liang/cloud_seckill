import { GoodsStatus } from '@/types'
import type { GoodsVO } from '@/types'

/**
 * 根据 startTime / endTime 实时计算商品秒杀状态
 * 不依赖数据库的静态 status 字段
 */
export function getRealStatus(goods: GoodsVO): GoodsStatus {
    // 已下架的状态由后端标记，前端直接信任
    if (goods.status === GoodsStatus.OFF_SHELF) {
        return GoodsStatus.OFF_SHELF
    }

    const now = Date.now()
    const start = new Date(goods.startTime).getTime()
    const end = new Date(goods.endTime).getTime()

    if (now < start) return GoodsStatus.NOT_STARTED
    if (now > end) return GoodsStatus.ENDED
    return GoodsStatus.IN_PROGRESS
}
