/**
 * 商品信息
 */
export interface Goods {
    id: number
    goodsName: string
    goodsTitle: string
    goodsImg: string
    goodsDetail: string
    goodsPrice: number
    seckillPrice: number
    stockCount: number
    startTime: string
    endTime: string
    status: number
}

/**
 * 商品视图对象
 */
export type GoodsVO = Goods

/**
 * 商品状态枚举
 */
export enum GoodsStatus {
    /** 未开始 */
    NOT_STARTED = 0,
    /** 进行中 */
    IN_PROGRESS = 1,
    /** 已结束 */
    ENDED = 2,
    /** 已下架 */
    OFF_SHELF = 3,
}

/**
 * 商品状态文本映射
 */
export const GoodsStatusText: Record<GoodsStatus, string> = {
    [GoodsStatus.NOT_STARTED]: '即将开始',
    [GoodsStatus.IN_PROGRESS]: '进行中',
    [GoodsStatus.ENDED]: '已结束',
    [GoodsStatus.OFF_SHELF]: '已下架',
}

/**
 * 商品状态颜色映射
 */
export const GoodsStatusColor: Record<GoodsStatus, string> = {
    [GoodsStatus.NOT_STARTED]: 'blue',
    [GoodsStatus.IN_PROGRESS]: 'red',
    [GoodsStatus.ENDED]: 'default',
    [GoodsStatus.OFF_SHELF]: 'default',
}
