import { useState, useEffect, useRef, useCallback } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { Typography, Spin, Result, Button, Card, Steps, Space, Alert, Modal } from 'antd'
import {
    CheckCircleFilled,
    ClockCircleOutlined,
    ShoppingOutlined,
    HomeOutlined,
    LoadingOutlined,
    SyncOutlined,
    ThunderboltOutlined,
    ShareAltOutlined,
} from '@ant-design/icons'
import { getOrderByNo } from '@/api'
import { CountDown } from '@/components'
import ShareCard from '@/components/ShareCard'
import type { OrderVO } from '@/types'
import { OrderStatus, OrderStatusText } from '@/types'
import { formatPrice, formatOrderNo, formatTime } from '@/utils'
import './index.css'

const { Title, Text, Paragraph } = Typography

/** 排队阶段 */
enum QueuePhase {
    QUEUING = 0,      // 排队中
    DEDUCTING = 1,     // 扣减库存中
    CREATING = 2,      // 创建订单中
    SUCCESS = 3,       // 成功
    FAILED = -1,       // 失败
}

const PHASE_TEXTS = {
    [QueuePhase.QUEUING]: '排队中...',
    [QueuePhase.DEDUCTING]: '扣减库存中...',
    [QueuePhase.CREATING]: '生成订单中...',
    [QueuePhase.SUCCESS]: '秒杀成功！',
    [QueuePhase.FAILED]: '查询超时',
}

/** 最大轮询次数 */
const MAX_POLL_COUNT = 30
/** 轮询间隔(ms) */
const POLL_INTERVAL = 1000

/**
 * 秒杀结果页
 */
export default function SeckillResult() {
    const { orderNo } = useParams<{ orderNo: string }>()
    const navigate = useNavigate()
    const [loading, setLoading] = useState(true)
    const [order, setOrder] = useState<OrderVO | null>(null)
    const [error, setError] = useState<string | null>(null)
    const [phase, setPhase] = useState<QueuePhase>(QueuePhase.QUEUING)
    const pollCountRef = useRef(0)
    const timerRef = useRef<ReturnType<typeof setTimeout>>()
    const [showShare, setShowShare] = useState(false)

    // 模拟阶段推进（排队 → 扣库存 → 创建订单）
    useEffect(() => {
        if (phase === QueuePhase.QUEUING) {
            const t = setTimeout(() => setPhase(QueuePhase.DEDUCTING), 800)
            return () => clearTimeout(t)
        }
        if (phase === QueuePhase.DEDUCTING) {
            const t = setTimeout(() => setPhase(QueuePhase.CREATING), 1200)
            return () => clearTimeout(t)
        }
    }, [phase])

    // 轮询获取订单
    const pollOrder = useCallback(async () => {
        if (!orderNo) return

        pollCountRef.current++
        try {
            const result = await getOrderByNo(Number(orderNo))
            if (result.data) {
                setOrder(result.data)
                setPhase(QueuePhase.SUCCESS)
                setLoading(false)
                return
            }
        } catch {
            // 订单可能还在创建中
        }

        if (pollCountRef.current >= MAX_POLL_COUNT) {
            setPhase(QueuePhase.FAILED)
            setLoading(false)
            setError('订单查询超时，请到订单列表中查看')
            return
        }

        // 继续轮询
        timerRef.current = setTimeout(pollOrder, POLL_INTERVAL)
    }, [orderNo])

    useEffect(() => {
        pollOrder()
        return () => {
            if (timerRef.current) clearTimeout(timerRef.current)
        }
    }, [pollOrder])

    // 排队中动画
    if (loading && phase !== QueuePhase.FAILED) {
        return (
            <div className="seckill-result-page">
                <div className="queue-animation-container">
                    {/* 排队进度 */}
                    <div className="queue-header">
                        <div className="queue-icon-wrapper">
                            <ThunderboltOutlined className="queue-bolt-icon" />
                        </div>
                        <Title level={3} className="mb-2">正在处理您的秒杀请求</Title>
                        <Text type="secondary">请耐心等待，勿重复操作</Text>
                    </div>

                    <Steps
                        current={phase}
                        className="queue-steps"
                        items={[
                            {
                                title: '排队等待',
                                description: phase === QueuePhase.QUEUING ? '进行中...' : '完成',
                                icon: phase === QueuePhase.QUEUING
                                    ? <LoadingOutlined className="text-orange-500" />
                                    : <CheckCircleFilled className="text-green-500" />,
                            },
                            {
                                title: '扣减库存',
                                description: phase === QueuePhase.DEDUCTING ? '进行中...'
                                    : phase > QueuePhase.DEDUCTING ? '完成' : '等待中',
                                icon: phase === QueuePhase.DEDUCTING
                                    ? <SyncOutlined spin className="text-blue-500" />
                                    : phase > QueuePhase.DEDUCTING
                                        ? <CheckCircleFilled className="text-green-500" />
                                        : <ClockCircleOutlined className="text-gray-300" />,
                            },
                            {
                                title: '生成订单',
                                description: phase === QueuePhase.CREATING ? '进行中...'
                                    : phase > QueuePhase.CREATING ? '完成' : '等待中',
                                icon: phase === QueuePhase.CREATING
                                    ? <SyncOutlined spin className="text-blue-500" />
                                    : phase > QueuePhase.CREATING
                                        ? <CheckCircleFilled className="text-green-500" />
                                        : <ClockCircleOutlined className="text-gray-300" />,
                            },
                        ]}
                    />

                    {/* 进度条 */}
                    <div className="queue-progress-bar">
                        <div
                            className="queue-progress-fill"
                            style={{ width: `${Math.min((phase + 1) * 33, 100)}%` }}
                        />
                    </div>

                    <Text type="secondary" className="queue-tip">
                        {PHASE_TEXTS[phase]} (已等待 {pollCountRef.current}s)
                    </Text>
                </div>
            </div>
        )
    }

    // 超时/失败
    if (error || !order) {
        return (
            <Result
                status="warning"
                title="订单查询超时"
                subTitle="请稍后在订单列表中查看"
                extra={[
                    <Button key="home" onClick={() => navigate('/')}>
                        返回首页
                    </Button>,
                    <Button key="orders" type="primary" onClick={() => navigate('/orders')}>
                        查看订单
                    </Button>,
                ]}
            />
        )
    }

    // 计算支付截止时间（订单创建后15分钟）
    const payDeadline = new Date(new Date(order.createTime).getTime() + 15 * 60 * 1000)

    return (
        <div className="seckill-result-page">
            {/* 成功提示 */}
            <div className="success-banner bg-gradient-to-r from-green-400 to-green-600 rounded-2xl p-8 text-white text-center mb-6 success-entrance">
                <CheckCircleFilled className="text-6xl mb-4" />
                <Title level={2} className="text-white mb-2">
                    🎉 恭喜，秒杀成功！
                </Title>
                <Paragraph className="text-white/90 text-lg mb-0">
                    订单已创建，请尽快完成支付
                </Paragraph>
            </div>

            {/* 订单信息卡片 */}
            <Card className="mb-6">
                <div className="flex flex-col lg:flex-row gap-6">
                    {/* 订单详情 */}
                    <div className="flex-1">
                        <div className="flex items-start gap-4 mb-4">
                            <img
                                src={order.goodsImg || '/default-goods.png'}
                                alt={order.goodsName}
                                className="w-20 h-20 object-cover rounded-lg"
                            />
                            <div>
                                <Title level={4} className="mb-1">
                                    {order.goodsName}
                                </Title>
                                <Text type="secondary">数量：{order.goodsCount}</Text>
                            </div>
                        </div>

                        <div className="space-y-2 text-gray-600">
                            <div className="flex justify-between">
                                <span>订单编号</span>
                                <Text copyable={{ text: String(order.orderNo) }}>
                                    {formatOrderNo(order.orderNo)}
                                </Text>
                            </div>
                            <div className="flex justify-between">
                                <span>下单时间</span>
                                <span>{formatTime(order.createTime)}</span>
                            </div>
                            <div className="flex justify-between">
                                <span>订单状态</span>
                                <span>{OrderStatusText[order.status as OrderStatus]}</span>
                            </div>
                        </div>
                    </div>

                    {/* 支付信息 */}
                    <div className="lg:w-64 bg-gray-50 rounded-xl p-4 text-center">
                        <Text type="secondary">应付金额</Text>
                        <div className="text-red-500 text-3xl font-bold my-2">
                            ¥{formatPrice(order.totalAmount)}
                        </div>

                        {order.status === OrderStatus.UNPAID && (
                            <>
                                <Alert
                                    message={
                                        <div className="flex items-center gap-2">
                                            <ClockCircleOutlined />
                                            <span>请在规定时间内完成支付</span>
                                        </div>
                                    }
                                    type="warning"
                                    showIcon={false}
                                    className="mb-4"
                                />
                                <CountDown
                                    endTime={payDeadline}
                                    prefix="剩余"
                                    size="small"
                                    onEnd={() => pollOrder()}
                                />
                            </>
                        )}
                    </div>
                </div>
            </Card>

            {/* 操作按钮 */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
                {order.status === OrderStatus.UNPAID && (
                    <Button
                        type="primary"
                        danger
                        size="large"
                        icon={<ShoppingOutlined />}
                        onClick={() => navigate(`/pay/${order.orderNo}`)}
                        className="h-12 px-8"
                    >
                        立即支付
                    </Button>
                )}
                <Button
                    size="large"
                    icon={<ShareAltOutlined />}
                    onClick={() => setShowShare(true)}
                    className="h-12 px-8"
                >
                    分享战绩
                </Button>
                <Button
                    size="large"
                    onClick={() => navigate('/orders')}
                    className="h-12 px-8"
                >
                    查看订单
                </Button>
                <Button
                    size="large"
                    icon={<HomeOutlined />}
                    onClick={() => navigate('/')}
                    className="h-12 px-8"
                >
                    继续抢购
                </Button>
            </div>

            {/* 分享弹窗 */}
            <Modal
                title="分享秒杀战绩"
                open={showShare}
                onCancel={() => setShowShare(false)}
                footer={null}
                width={420}
                centered
            >
                <ShareCard order={order} />
            </Modal>
        </div>
    )
}
