import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { Typography, Spin, Result, Button, Card, Radio, Space, message, Breadcrumb } from 'antd'
import {
    HomeOutlined,
    AlipayCircleOutlined,
    WechatOutlined,
    CreditCardOutlined,
    CheckCircleFilled,
} from '@ant-design/icons'
import { getOrderByNo, payOrder } from '@/api'
import { CountDown } from '@/components'
import type { OrderVO } from '@/types'
import { OrderStatus } from '@/types'
import { formatPrice, formatOrderNo, formatTime } from '@/utils'
import './index.css'

const { Title, Text, Paragraph } = Typography

/**
 * 支付页面（模拟）
 */
export default function Pay() {
    const { orderNo } = useParams<{ orderNo: string }>()
    const navigate = useNavigate()
    const [loading, setLoading] = useState(true)
    const [order, setOrder] = useState<OrderVO | null>(null)
    const [error, setError] = useState<string | null>(null)
    const [payMethod, setPayMethod] = useState<string>('alipay')
    const [paying, setPaying] = useState(false)
    const [paySuccess, setPaySuccess] = useState(false)

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
            // 如果已支付，直接显示成功
            if (result.data.status !== OrderStatus.UNPAID) {
                setPaySuccess(true)
            }
        } catch (err) {
            setError('获取订单详情失败')
            console.error(err)
        } finally {
            setLoading(false)
        }
    }

    // 模拟支付
    const handlePay = async () => {
        if (!order) return
        setPaying(true)
        try {
            await payOrder(order.orderNo)
            setPaySuccess(true)
            message.success('支付成功！')
        } catch (err) {
            console.error(err)
        } finally {
            setPaying(false)
        }
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
            <Result
                status="error"
                title="订单不存在"
                extra={
                    <Button type="primary" onClick={() => navigate('/orders')}>
                        查看订单
                    </Button>
                }
            />
        )
    }

    // 支付成功
    if (paySuccess) {
        return (
            <div className="pay-page max-w-2xl mx-auto">
                <Result
                    icon={<CheckCircleFilled className="text-green-500" />}
                    title="支付成功！"
                    subTitle={
                        <div>
                            <p>订单号：{formatOrderNo(order.orderNo)}</p>
                            <p>支付金额：¥{formatPrice(order.totalAmount)}</p>
                        </div>
                    }
                    extra={[
                        <Button key="orders" onClick={() => navigate('/orders')}>
                            查看订单
                        </Button>,
                        <Button key="home" type="primary" onClick={() => navigate('/')}>
                            继续抢购
                        </Button>,
                    ]}
                />
            </div>
        )
    }

    // 计算支付截止时间
    const payDeadline = new Date(new Date(order.createTime).getTime() + 15 * 60 * 1000)

    return (
        <div className="pay-page max-w-2xl mx-auto">
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
                    { title: '订单支付' },
                ]}
            />

            {/* 订单信息 */}
            <Card className="mb-6">
                <div className="text-center mb-4">
                    <Text type="secondary">订单金额</Text>
                    <div className="text-red-500 text-4xl font-bold my-2">
                        ¥{formatPrice(order.totalAmount)}
                    </div>
                    <Text type="secondary">
                        订单号：{formatOrderNo(order.orderNo)}
                    </Text>
                </div>

                {/* 倒计时 */}
                <div className="text-center py-4 bg-orange-50 rounded-lg mb-4">
                    <Text type="warning" className="block mb-2">
                        请在规定时间内完成支付，超时订单将自动取消
                    </Text>
                    <CountDown
                        endTime={payDeadline}
                        prefix="剩余"
                        size="default"
                        onEnd={() => fetchOrder(String(order.orderNo))}
                    />
                </div>

                {/* 商品信息 */}
                <div className="flex items-center gap-4 p-4 bg-gray-50 rounded-lg">
                    <img
                        src={order.goodsImg || '/default-goods.png'}
                        alt={order.goodsName}
                        className="w-16 h-16 object-cover rounded"
                    />
                    <div className="flex-1">
                        <Text strong>{order.goodsName}</Text>
                        <br />
                        <Text type="secondary">
                            ¥{formatPrice(order.goodsPrice)} × {order.goodsCount}
                        </Text>
                    </div>
                </div>
            </Card>

            {/* 支付方式 */}
            <Card title="选择支付方式" className="mb-6">
                <Radio.Group
                    value={payMethod}
                    onChange={(e) => setPayMethod(e.target.value)}
                    className="w-full"
                >
                    <Space direction="vertical" className="w-full">
                        <Radio value="alipay" className="pay-method-item">
                            <div className="flex items-center gap-3">
                                <AlipayCircleOutlined className="text-2xl text-blue-500" />
                                <span>支付宝</span>
                            </div>
                        </Radio>
                        <Radio value="wechat" className="pay-method-item">
                            <div className="flex items-center gap-3">
                                <WechatOutlined className="text-2xl text-green-500" />
                                <span>微信支付</span>
                            </div>
                        </Radio>
                        <Radio value="card" className="pay-method-item">
                            <div className="flex items-center gap-3">
                                <CreditCardOutlined className="text-2xl text-orange-500" />
                                <span>银行卡支付</span>
                            </div>
                        </Radio>
                    </Space>
                </Radio.Group>
            </Card>

            {/* 支付按钮 */}
            <Button
                type="primary"
                danger
                block
                size="large"
                loading={paying}
                onClick={handlePay}
                className="h-14 text-lg"
            >
                确认支付 ¥{formatPrice(order.totalAmount)}
            </Button>

            <Paragraph type="secondary" className="text-center mt-4 text-sm">
                点击"确认支付"即表示您同意《用户服务协议》
            </Paragraph>
        </div>
    )
}
