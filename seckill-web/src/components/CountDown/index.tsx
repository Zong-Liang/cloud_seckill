import { useState, useEffect, useRef } from 'react'

interface TimeLeft {
    days: number
    hours: number
    minutes: number
    seconds: number
    total: number
}

interface CountDownProps {
    /** 结束时间 */
    endTime: string | Date
    /** 开始时间（可选，用于显示"即将开始"） */
    startTime?: string | Date
    /** 倒计时结束回调 */
    onEnd?: () => void
    /** 前缀文本 */
    prefix?: string
    /** 尺寸 */
    size?: 'small' | 'default' | 'large'
}

const ZERO: TimeLeft = { days: 0, hours: 0, minutes: 0, seconds: 0, total: 0 }

/**
 * 计算时间差（纯函数，无副作用）
 */
function calcDiff(targetMs: number): TimeLeft {
    const diff = targetMs - Date.now()
    if (diff <= 0) return ZERO
    return {
        days: Math.floor(diff / (1000 * 60 * 60 * 24)),
        hours: Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)),
        minutes: Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60)),
        seconds: Math.floor((diff % (1000 * 60)) / 1000),
        total: diff,
    }
}

/**
 * 倒计时组件
 */
export default function CountDown({
    endTime,
    startTime,
    onEnd,
    prefix = '剩余',
    size = 'default',
}: CountDownProps) {
    const [timeLeft, setTimeLeft] = useState<TimeLeft>(ZERO)
    const [isStarted, setIsStarted] = useState(!startTime)
    const onEndRef = useRef(onEnd)
    const hasEndedRef = useRef(false)

    // 保持 onEnd 回调最新引用
    useEffect(() => {
        onEndRef.current = onEnd
    }, [onEnd])

    useEffect(() => {
        const endMs = new Date(endTime).getTime()
        const startMs = startTime ? new Date(startTime).getTime() : null

        hasEndedRef.current = false

        function tick() {
            const now = Date.now()

            // 阶段1: 未开始，倒计到开始时间
            if (startMs && now < startMs) {
                setIsStarted(false)
                setTimeLeft(calcDiff(startMs))
                return
            }

            // 阶段2: 已开始，倒计到结束时间
            setIsStarted(true)
            const remaining = calcDiff(endMs)
            setTimeLeft(remaining)

            // 阶段3: 已结束
            if (remaining.total <= 0 && !hasEndedRef.current) {
                hasEndedRef.current = true
                onEndRef.current?.()
            }
        }

        // 立即执行一次
        tick()

        // 每秒更新
        const timer = setInterval(tick, 1000)
        return () => clearInterval(timer)
    }, [endTime, startTime])  // 仅依赖 props，不依赖任何回调

    const formatNumber = (num: number) => num.toString().padStart(2, '0')

    const sizeClass = {
        small: 'text-xs',
        default: 'text-sm',
        large: 'text-base',
    }

    const itemPadding = {
        small: 'px-1 py-0.5',
        default: 'px-2 py-1',
        large: 'px-3 py-1.5',
    }

    const displayPrefix = isStarted ? prefix : '距开始'

    return (
        <div className={`countdown-box ${sizeClass[size]}`}>
            <span className="text-gray-600 mr-2">{displayPrefix}</span>
            <div className="flex items-center gap-1">
                {timeLeft.days > 0 && (
                    <>
                        <div className={`countdown-item ${itemPadding[size]}`}>
                            <span>{formatNumber(timeLeft.days)}</span>
                            <span className="countdown-label">天</span>
                        </div>
                        <span className="countdown-separator">:</span>
                    </>
                )}
                <div className={`countdown-item ${itemPadding[size]}`}>
                    <span>{formatNumber(timeLeft.hours)}</span>
                    <span className="countdown-label">时</span>
                </div>
                <span className="countdown-separator">:</span>
                <div className={`countdown-item ${itemPadding[size]}`}>
                    <span>{formatNumber(timeLeft.minutes)}</span>
                    <span className="countdown-label">分</span>
                </div>
                <span className="countdown-separator">:</span>
                <div className={`countdown-item ${itemPadding[size]}`}>
                    <span>{formatNumber(timeLeft.seconds)}</span>
                    <span className="countdown-label">秒</span>
                </div>
            </div>
        </div>
    )
}
