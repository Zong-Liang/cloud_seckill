import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { Typography, Spin, Result, Button, Card, Statistic, Space, Alert } from 'antd'
import {
    CheckCircleFilled,
    ClockCircleOutlined,
    ShoppingOutlined,
    HomeOutlined,
} from '@ant-design/icons'
import { getOrderByNo } from '@/api'
import { CountDown } from '@/components'
import type { OrderVO } from '@/types'
import { OrderStatus, OrderStatusText } from '@/types'
import { formatPrice, formatOrderNo, formatTime } from '@/utils'
import './index.css'

const { Title, Text, Paragraph } = Typography

/**
 * 秒杀结果页
 */
export default function SeckillResult() {
    const { orderNo } = useParams<{ orderNo: string }>()
    const navigate = useNavigate()
    const [loading, setLoading] = useState(true)
    const [order, setOrder] = useState<OrderVO | null>(null)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        if (orderNo) {
            fetchOrder(orderNo)
        }
    }, [orderNo])

    const fetchOrder = async (no: string) => {
        setLoading(true)
        setError(null)
        try {
            const result = await getOrderByNo(Number(no))
            setOrder(result.data)
        } catch (err) {
            // 订单可能还在创建中，稍后重试
            setTimeout(() => fetchOrder(no), 1000)
        } finally {
            setLoading(false)
        }
    }

    if (loading && !order) {
        return (
            <div className="seckill-result-page min-h-96 flex items-center justify-center">
                <div className="text-center">
                    <Spin size="large" />
                    <Paragraph className="mt-4 text-gray-500">订单创建中，请稍候...</Paragraph>
                </div>
            </div>
        )
    }

    if (error || !order) {
        return (
            <Result
                status="warning"
                title="订单查询失败"
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
            <div className="success-banner bg-gradient-to-r from-green-400 to-green-600 rounded-2xl p-8 text-white text-center mb-6">
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
                                    onEnd={() => fetchOrder(String(order.orderNo))}
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
        </div>
    )
}
