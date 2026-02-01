import { useState, useEffect, useMemo, useCallback } from 'react'

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
    const [timeLeft, setTimeLeft] = useState<TimeLeft>({
        days: 0,
        hours: 0,
        minutes: 0,
        seconds: 0,
        total: 0,
    })
    const [isStarted, setIsStarted] = useState(true)

    const targetTime = useMemo(() => new Date(endTime).getTime(), [endTime])
    const startTargetTime = useMemo(
        () => (startTime ? new Date(startTime).getTime() : null),
        [startTime]
    )

    const calculateTimeLeft = useCallback((): TimeLeft => {
        const now = Date.now()

        // 检查是否未开始
        if (startTargetTime && now < startTargetTime) {
            setIsStarted(false)
            const diff = startTargetTime - now
            return {
                days: Math.floor(diff / (1000 * 60 * 60 * 24)),
                hours: Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)),
                minutes: Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60)),
                seconds: Math.floor((diff % (1000 * 60)) / 1000),
                total: diff,
            }
        }

        setIsStarted(true)
        const diff = targetTime - now

        if (diff <= 0) {
            onEnd?.()
            return { days: 0, hours: 0, minutes: 0, seconds: 0, total: 0 }
        }

        return {
            days: Math.floor(diff / (1000 * 60 * 60 * 24)),
            hours: Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)),
            minutes: Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60)),
            seconds: Math.floor((diff % (1000 * 60)) / 1000),
            total: diff,
        }
    }, [targetTime, startTargetTime, onEnd])

    useEffect(() => {
        setTimeLeft(calculateTimeLeft())

        const timer = setInterval(() => {
            const newTimeLeft = calculateTimeLeft()
            setTimeLeft(newTimeLeft)

            if (newTimeLeft.total <= 0) {
                clearInterval(timer)
            }
        }, 1000)

        return () => clearInterval(timer)
    }, [calculateTimeLeft])

    const formatNumber = (num: number) => num.toString().padStart(2, '0')

    // 尺寸样式
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
