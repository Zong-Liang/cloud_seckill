import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import {
    Typography,
    Spin,
    Alert,
    Card,
    Tag,
    Button,
    Steps,
    Divider,
    Modal,
    message,
    Breadcrumb,
    Descriptions,
} from 'antd'
import {
    HomeOutlined,
    ShoppingOutlined,
    ExclamationCircleOutlined,
    CheckCircleOutlined,
    ClockCircleOutlined,
    CloseCircleOutlined,
    CarOutlined,
    SmileOutlined,
} from '@ant-design/icons'
import { getOrderByNo, cancelOrder, payOrder } from '@/api'
import { CountDown } from '@/components'
import type { OrderVO } from '@/types'
import { OrderStatus, OrderStatusText, OrderStatusColor, OrderChannelText } from '@/types'
import { formatPrice, formatOrderNo, formatTime } from '@/utils'
import './index.css'

const { Title, Text, Paragraph } = Typography
const { confirm } = Modal

/**
 * 订单详情页
 */
export default function OrderDetail() {
    const { orderNo } = useParams<{ orderNo: string }>()
    const navigate = useNavigate()
    const [loading, setLoading] = useState(true)
    const [order, setOrder] = useState<OrderVO | null>(null)
    const [error, setError] = useState<string | null>(null)
    const [actionLoading, setActionLoading] = useState(false)

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
            setError('获取订单详情失败')
            console.error(err)
        } finally {
            setLoading(false)
        }
    }

    // 取消订单
    const handleCancel = () => {
        if (!order) return
        confirm({
            title: '确认取消订单？',
            icon: <ExclamationCircleOutlined />,
            content: '取消后库存将释放，其他用户可以抢购',
            okText: '确认取消',
            okType: 'danger',
            cancelText: '再想想',
            onOk: async () => {
                setActionLoading(true)
                try {
                    await cancelOrder(order.orderNo)
                    message.success('订单已取消')
                    fetchOrder(String(order.orderNo))
                } catch (err) {
                    console.error(err)
                } finally {
                    setActionLoading(false)
                }
            },
        })
    }

    // 支付订单
    const handlePay = async () => {
        if (!order) return
        setActionLoading(true)
        try {
            await payOrder(order.orderNo)
            message.success('支付成功！')
            fetchOrder(String(order.orderNo))
        } catch (err) {
            console.error(err)
        } finally {
            setActionLoading(false)
        }
    }

    // 获取订单步骤
    const getOrderSteps = () => {
        const status = order?.status as OrderStatus
        const items = [
            { title: '下单成功', icon: <CheckCircleOutlined /> },
            { title: '待支付', icon: <ClockCircleOutlined /> },
            { title: '已支付', icon: <CheckCircleOutlined /> },
            { title: '已发货', icon: <CarOutlined /> },
            { title: '已完成', icon: <SmileOutlined /> },
        ]

        let current = 0
        switch (status) {
            case OrderStatus.UNPAID:
                current = 1
                break
            case OrderStatus.PAID:
                current = 2
                break
            case OrderStatus.SHIPPED:
                current = 3
                break
            case OrderStatus.RECEIVED:
                current = 4
                break
            case OrderStatus.CANCELLED:
            case OrderStatus.TIMEOUT:
                return { items: [{ title: '已取消', icon: <CloseCircleOutlined /> }], current: 0, status: 'error' as const }
            default:
                current = 0
        }

        return { items, current, status: 'process' as const }
    }

    if (loading) {
        return (
            <div className="page-loading min-h-96">
                <Spin size="large" tip="加载中..." />
            </div>
        )
    }

    if (error || !order) {
        return (
            <Alert
                message="加载失败"
                description={error || '订单不存在'}
                type="error"
                showIcon
                action={<Link to="/orders">返回列表</Link>}
            />
        )
    }

    const status = order.status as OrderStatus
    const steps = getOrderSteps()
    const payDeadline = new Date(new Date(order.createTime).getTime() + 15 * 60 * 1000)

    return (
        <div className="order-detail-page">
            {/* 面包屑 */}
            <Breadcrumb
                className="mb-4"
                items={[
                    {
                        title: (
                            <Link to="/">
                                <HomeOutlined /> 首页
                            </Link>
                        ),
                    },
                    {
                        title: <Link to="/orders">我的订单</Link>,
                    },
                    { title: '订单详情' },
                ]}
            />

            {/* 订单状态 */}
            <Card className="mb-6">
                <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-4 mb-6">
                    <div>
                        <div className="flex items-center gap-3 mb-2">
                            <Title level={3} className="mb-0">
                                {OrderStatusText[status]}
                            </Title>
                            <Tag color={OrderStatusColor[status]}>
                                {OrderStatusText[status]}
                            </Tag>
                        </div>
                        {status === OrderStatus.UNPAID && (
                            <div className="flex items-center gap-2">
                                <Text type="warning">请在规定时间内完成支付：</Text>
                                <CountDown
                                    endTime={payDeadline}
                                    size="small"
                                    onEnd={() => fetchOrder(String(order.orderNo))}
                                />
                            </div>
                        )}
                    </div>
                    <div className="text-right">
                        <Text type="secondary">订单金额</Text>
                        <div className="text-red-500 text-2xl font-bold">
                            ¥{formatPrice(order.totalAmount)}
                        </div>
                    </div>
                </div>

                {/* 订单进度 */}
                {status !== OrderStatus.CANCELLED && status !== OrderStatus.TIMEOUT && (
                    <Steps current={steps.current} status={steps.status} items={steps.items} />
                )}
            </Card>

            {/* 商品信息 */}
            <Card title="商品信息" className="mb-6">
                <div className="flex items-center gap-4">
                    <img
                        src={order.goodsImg || '/default-goods.png'}
                        alt={order.goodsName}
                        className="w-24 h-24 object-cover rounded-lg cursor-pointer"
                        onClick={() => navigate(`/goods/${order.goodsId}`)}
                    />
                    <div className="flex-1">
                        <Title level={4} className="mb-1">
                            {order.goodsName}
                        </Title>
                        <Text type="secondary">单价：¥{formatPrice(order.goodsPrice)}</Text>
                        <Text type="secondary" className="ml-4">
                            数量：{order.goodsCount}
                        </Text>
                    </div>
                    <div className="text-right">
                        <Text>小计</Text>
                        <div className="text-lg font-bold">¥{formatPrice(order.totalAmount)}</div>
                    </div>
                </div>
            </Card>

            {/* 订单信息 */}
            <Card title="订单信息" className="mb-6">
                <Descriptions column={{ xs: 1, sm: 2 }}>
                    <Descriptions.Item label="订单编号">
                        <Text copyable={{ text: String(order.orderNo) }}>
                            {formatOrderNo(order.orderNo)}
                        </Text>
                    </Descriptions.Item>
                    <Descriptions.Item label="下单时间">
                        {formatTime(order.createTime)}
                    </Descriptions.Item>
                    <Descriptions.Item label="订单状态">
                        <Tag color={OrderStatusColor[status]}>{OrderStatusText[status]}</Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="下单渠道">
                        {OrderChannelText[order.channel as keyof typeof OrderChannelText] || 'PC'}
                    </Descriptions.Item>
                    {order.payTime && (
                        <Descriptions.Item label="支付时间">
                            {formatTime(order.payTime)}
                        </Descriptions.Item>
                    )}
                </Descriptions>
            </Card>

            {/* 操作按钮 */}
            <div className="flex justify-center gap-4">
                <Button size="large" onClick={() => navigate('/orders')}>
                    返回订单列表
                </Button>
                {status === OrderStatus.UNPAID && (
                    <>
                        <Button size="large" onClick={handleCancel} loading={actionLoading}>
                            取消订单
                        </Button>
                        <Button
                            type="primary"
                            danger
                            size="large"
                            onClick={handlePay}
                            loading={actionLoading}
                        >
                            立即支付
                        </Button>
                    </>
                )}
            </div>
        </div>
    )
}
