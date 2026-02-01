import { useState, useCallback, useRef } from 'react'
import { Button, Modal, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import { ThunderboltOutlined } from '@ant-design/icons'
import { doSeckill } from '@/api'
import { useUserStore, useSeckillStore } from '@/store'
import { canDoSeckill } from '@/utils'
import type { GoodsVO } from '@/types'
import { GoodsStatus } from '@/types'

interface SeckillButtonProps {
    /** 商品信息 */
    goods: GoodsVO
    /** 是否禁用 */
    disabled?: boolean
    /** 自定义类名 */
    className?: string
    /** 按钮大小 */
    size?: 'small' | 'middle' | 'large'
}

/**
 * 秒杀按钮组件
 */
export default function SeckillButton({
    goods,
    disabled,
    className = '',
    size = 'large',
}: SeckillButtonProps) {
    const [loading, setLoading] = useState(false)
    const navigate = useNavigate()
    const { user, isLoggedIn } = useUserStore()
    const { hasSeckilled, addSeckillRecord, isPending, setPending } = useSeckillStore()

    // 防抖
    const lastClickRef = useRef(0)
    const CLICK_INTERVAL = 1000

    // 获取按钮状态
    const getButtonState = useCallback(() => {
        const now = Date.now()
        const startTime = new Date(goods.startTime).getTime()
        const endTime = new Date(goods.endTime).getTime()

        // 已下架
        if (goods.status === GoodsStatus.OFF_SHELF) {
            return { text: '已下架', disabled: true, type: 'default' as const }
        }
        // 未开始
        if (now < startTime || goods.status === GoodsStatus.NOT_STARTED) {
            return { text: '即将开始', disabled: true, type: 'default' as const }
        }
        // 已结束
        if (now > endTime || goods.status === GoodsStatus.ENDED) {
            return { text: '已结束', disabled: true, type: 'default' as const }
        }
        // 已售罄
        if (goods.stockCount <= 0) {
            return { text: '已售罄', disabled: true, type: 'default' as const }
        }
        // 已秒杀过
        if (isLoggedIn && hasSeckilled(goods.id)) {
            return { text: '已抢购', disabled: true, type: 'default' as const }
        }
        // 正在秒杀中
        if (isPending(goods.id)) {
            return { text: '抢购中...', disabled: true, type: 'primary' as const }
        }
        // 可秒杀
        return { text: '立即秒杀', disabled: false, type: 'primary' as const }
    }, [goods, isLoggedIn, hasSeckilled, isPending])

    const buttonState = getButtonState()

    // 执行秒杀
    const handleSeckill = async () => {
        // 检查登录状态
        if (!isLoggedIn || !user) {
            Modal.confirm({
                title: '提示',
                content: '请先登录后再参与秒杀',
                okText: '去登录',
                cancelText: '取消',
                onOk: () => {
                    navigate('/login', { state: { from: `/goods/${goods.id}` } })
                },
            })
            return
        }

        // 防抖检查
        const now = Date.now()
        if (now - lastClickRef.current < CLICK_INTERVAL) {
            message.warning('请勿频繁点击')
            return
        }
        lastClickRef.current = now

        // 限流检查
        if (!canDoSeckill(goods.id)) {
            message.warning('请稍后再试')
            return
        }

        // 已秒杀检查
        if (hasSeckilled(goods.id)) {
            message.warning('您已参与过该商品秒杀')
            return
        }

        setLoading(true)
        setPending(goods.id, true)

        try {
            const result = await doSeckill({
                userId: user.id,
                goodsId: goods.id,
                count: 1,
                channel: 'PC',
            })

            // 秒杀成功
            const orderNo = result.data
            addSeckillRecord(goods.id, orderNo)
            message.success('恭喜，秒杀成功！')

            // 跳转到结果页
            navigate(`/seckill/result/${orderNo}`)
        } catch (error) {
            // 错误已在 axios 拦截器中处理
            console.error('秒杀失败:', error)
        } finally {
            setLoading(false)
            setPending(goods.id, false)
        }
    }

    return (
        <Button
            type={buttonState.type}
            size={size}
            block
            danger={buttonState.type === 'primary'}
            loading={loading}
            disabled={disabled || buttonState.disabled}
            onClick={handleSeckill}
            icon={buttonState.type === 'primary' ? <ThunderboltOutlined /> : undefined}
            className={`seckill-btn ${className}`}
        >
            {buttonState.text}
        </Button>
    )
}
