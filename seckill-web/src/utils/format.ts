import dayjs from 'dayjs'

/**
 * 格式化价格
 * @param price 价格（分）
 * @param decimals 小数位数
 */
export function formatPrice(price: number, decimals = 2): string {
    return price.toFixed(decimals)
}

/**
 * 格式化时间
 * @param time 时间字符串或时间戳
 * @param format 格式化模板
 */
export function formatTime(
    time: string | number | Date | null | undefined,
    format = 'YYYY-MM-DD HH:mm:ss'
): string {
    if (!time) return '-'
    return dayjs(time).format(format)
}

/**
 * 格式化日期
 */
export function formatDate(time: string | number | Date | null | undefined): string {
    return formatTime(time, 'YYYY-MM-DD')
}

/**
 * 格式化订单号（每 4 位加空格，方便阅读）
 */
export function formatOrderNo(orderNo: number | string): string {
    const str = String(orderNo)
    return str.replace(/(\d{4})(?=\d)/g, '$1 ')
}

/**
 * 计算折扣百分比
 */
export function calcDiscount(originalPrice: number, seckillPrice: number): number {
    if (originalPrice <= 0) return 0
    return Math.round((1 - seckillPrice / originalPrice) * 100)
}

/**
 * 格式化库存
 */
export function formatStock(stock: number): string {
    if (stock >= 10000) {
        return (stock / 10000).toFixed(1) + '万'
    }
    if (stock >= 1000) {
        return (stock / 1000).toFixed(1) + 'k'
    }
    return String(stock)
}
