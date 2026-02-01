/**
 * 订单信息
 */
export interface Order {
    id: number
    orderNo: number
    userId: number
    goodsId: number
    goodsName: string
    goodsImg: string
    goodsPrice: number
    goodsCount: number
    totalAmount: number
    channel: number
    status: number
    createTime: string
    payTime: string | null
}

/**
 * 订单视图对象
 */
export type OrderVO = Order

/**
 * 秒杀请求参数
 */
export interface SeckillRequest {
    userId: number
    goodsId: number
    count?: number
    channel?: string
}

/**
 * 订单状态枚举
 */
export enum OrderStatus {
    /** 待支付 */
    UNPAID = 0,
    /** 已支付 */
    PAID = 1,
    /** 已发货 */
    SHIPPED = 2,
    /** 已收货 */
    RECEIVED = 3,
    /** 已取消 */
    CANCELLED = 4,
    /** 已超时 */
    TIMEOUT = 5,
}

/**
 * 订单状态文本映射
 */
export const OrderStatusText: Record<OrderStatus, string> = {
    [OrderStatus.UNPAID]: '待支付',
    [OrderStatus.PAID]: '已支付',
    [OrderStatus.SHIPPED]: '已发货',
    [OrderStatus.RECEIVED]: '已收货',
    [OrderStatus.CANCELLED]: '已取消',
    [OrderStatus.TIMEOUT]: '已超时',
}

/**
 * 订单状态颜色映射
 */
export const OrderStatusColor: Record<OrderStatus, string> = {
    [OrderStatus.UNPAID]: 'orange',
    [OrderStatus.PAID]: 'green',
    [OrderStatus.SHIPPED]: 'blue',
    [OrderStatus.RECEIVED]: 'green',
    [OrderStatus.CANCELLED]: 'default',
    [OrderStatus.TIMEOUT]: 'red',
}

/**
 * 下单渠道枚举
 */
export enum OrderChannel {
    PC = 1,
    ANDROID = 2,
    IOS = 3,
    WECHAT = 4,
}

/**
 * 渠道文本映射
 */
export const OrderChannelText: Record<OrderChannel, string> = {
    [OrderChannel.PC]: 'PC',
    [OrderChannel.ANDROID]: 'Android',
    [OrderChannel.IOS]: 'iOS',
    [OrderChannel.WECHAT]: '小程序',
}
