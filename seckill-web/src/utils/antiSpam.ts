/**
 * 请求频率限制器
 */
class RequestRateLimiter {
    private requests: Map<string, number[]> = new Map()
    private readonly windowMs: number
    private readonly maxRequests: number

    constructor(windowMs = 1000, maxRequests = 1) {
        this.windowMs = windowMs
        this.maxRequests = maxRequests
    }

    /**
     * 检查是否可以发起请求
     */
    canMakeRequest(key: string): boolean {
        const now = Date.now()
        const timestamps = this.requests.get(key) || []

        // 清理过期的时间戳
        const validTimestamps = timestamps.filter((t) => now - t < this.windowMs)

        if (validTimestamps.length >= this.maxRequests) {
            return false
        }

        validTimestamps.push(now)
        this.requests.set(key, validTimestamps)
        return true
    }

    /**
     * 清除指定 key 的限制
     */
    clear(key: string): void {
        this.requests.delete(key)
    }

    /**
     * 清除所有限制
     */
    clearAll(): void {
        this.requests.clear()
    }
}

// 秒杀请求限流器（1秒1次）
export const seckillRateLimiter = new RequestRateLimiter(1000, 1)

/**
 * 检查是否可以执行秒杀
 */
export function canDoSeckill(goodsId: number): boolean {
    return seckillRateLimiter.canMakeRequest(`seckill:${goodsId}`)
}

/**
 * 防抖函数
 */
export function debounce<T extends (...args: unknown[]) => unknown>(
    fn: T,
    delay: number
): (...args: Parameters<T>) => void {
    let timer: ReturnType<typeof setTimeout> | null = null
    return (...args: Parameters<T>) => {
        if (timer) {
            clearTimeout(timer)
        }
        timer = setTimeout(() => {
            fn(...args)
        }, delay)
    }
}

/**
 * 节流函数
 */
export function throttle<T extends (...args: unknown[]) => unknown>(
    fn: T,
    delay: number
): (...args: Parameters<T>) => void {
    let lastTime = 0
    return (...args: Parameters<T>) => {
        const now = Date.now()
        if (now - lastTime >= delay) {
            lastTime = now
            fn(...args)
        }
    }
}
