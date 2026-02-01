import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import {
    Typography,
    Spin,
    Empty,
    Alert,
    Tabs,
    Card,
    Tag,
    Button,
    Space,
    Modal,
    message,
    Breadcrumb,
} from 'antd'
import {
    HomeOutlined,
    ShoppingOutlined,
    ExclamationCircleOutlined,
} from '@ant-design/icons'
import { getMyOrders, cancelOrder } from '@/api'
import type { OrderVO } from '@/types'
import { OrderStatus, OrderStatusText, OrderStatusColor } from '@/types'
import { formatPrice, formatOrderNo, formatTime } from '@/utils'
import './index.css'

const { Title, Text } = Typography
const { confirm } = Modal

/**
 * 订单列表页
 */
export default function OrderList() {
    const navigate = useNavigate()
    const [loading, setLoading] = useState(true)
    const [orders, setOrders] = useState<OrderVO[]>([])
    const [error, setError] = useState<string | null>(null)
    const [activeTab, setActiveTab] = useState<string>('all')
    const [cancelLoading, setCancelLoading] = useState<number | null>(null)

    useEffect(() => {
        fetchOrders()
    }, [])

    const fetchOrders = async () => {
        setLoading(true)
        setError(null)
        try {
            const result = await getMyOrders()
            setOrders(result.data || [])
        } catch (err) {
            setError('获取订单列表失败')
            console.error(err)
        } finally {
            setLoading(false)
        }
    }

    // 取消订单
    const handleCancel = (order: OrderVO) => {
        confirm({
            title: '确认取消订单？',
            icon: <ExclamationCircleOutlined />,
            content: '取消后库存将释放，其他用户可以抢购',
            okText: '确认取消',
            okType: 'danger',
            cancelText: '再想想',
            onOk: async () => {
                setCancelLoading(order.id)
                try {
                    await cancelOrder(order.orderNo)
                    message.success('订单已取消')
                    fetchOrders()
                } catch (err) {
                    console.error(err)
                } finally {
                    setCancelLoading(null)
                }
            },
        })
    }

    // 过滤订单
    const getFilteredOrders = () => {
        if (activeTab === 'all') return orders
        return orders.filter((o) => o.status === Number(activeTab))
    }

    const filteredOrders = getFilteredOrders()

    // Tab 项
    const tabItems = [
        { key: 'all', label: `全部 (${orders.length})` },
        {
            key: String(OrderStatus.UNPAID),
            label: `待支付 (${orders.filter((o) => o.status === OrderStatus.UNPAID).length})`,
        },
        {
            key: String(OrderStatus.PAID),
            label: `已支付 (${orders.filter((o) => o.status === OrderStatus.PAID).length})`,
        },
        {
            key: String(OrderStatus.CANCELLED),
            label: `已取消 (${orders.filter((o) => o.status === OrderStatus.CANCELLED || o.status === OrderStatus.TIMEOUT).length
                })`,
        },
    ]

    // 渲染订单卡片
    const renderOrderCard = (order: OrderVO) => (
        <Card key={order.id} className="order-card mb-4" hoverable>
            {/* 订单头部 */}
            <div className="flex justify-between items-center mb-4 pb-4 border-b">
                <Space>
                    <Text type="secondary">订单编号：</Text>
                    <Text copyable={{ text: String(order.orderNo) }}>
                        {formatOrderNo(order.orderNo)}
                    </Text>
                </Space>
                <Space>
                    <Text type="secondary">{formatTime(order.createTime)}</Text>
                    <Tag color={OrderStatusColor[order.status as OrderStatus]}>
                        {OrderStatusText[order.status as OrderStatus]}
                    </Tag>
                </Space>
            </div>

            {/* 订单内容 */}
            <div
                className="flex items-center gap-4 cursor-pointer"
                onClick={() => navigate(`/order/${order.orderNo}`)}
            >
                <img
                    src={order.goodsImg || '/default-goods.png'}
                    alt={order.goodsName}
                    className="w-20 h-20 object-cover rounded-lg"
                />
                <div className="flex-1">
                    <Title level={5} className="mb-1" ellipsis>
                        {order.goodsName}
                    </Title>
                    <Text type="secondary">
                        ¥{formatPrice(order.goodsPrice)} × {order.goodsCount}
                    </Text>
                </div>
                <div className="text-right">
                    <Text type="secondary" className="text-sm">
                        实付款
                    </Text>
                    <div className="text-red-500 text-xl font-bold">
                        ¥{formatPrice(order.totalAmount)}
                    </div>
                </div>
            </div>

            {/* 操作按钮 */}
            <div className="flex justify-end gap-3 mt-4 pt-4 border-t">
                {order.status === OrderStatus.UNPAID && (
                    <>
                        <Button
                            onClick={() => handleCancel(order)}
                            loading={cancelLoading === order.id}
                        >
                            取消订单
                        </Button>
                        <Button
                            type="primary"
                            danger
                            onClick={() => navigate(`/pay/${order.orderNo}`)}
                        >
                            立即支付
                        </Button>
                    </>
                )}
                <Button onClick={() => navigate(`/order/${order.orderNo}`)}>
                    查看详情
                </Button>
            </div>
        </Card>
    )

    return (
        <div className="order-list-page">
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
                    { title: '我的订单' },
                ]}
            />

            <Title level={2} className="mb-6">
                <ShoppingOutlined className="mr-2" />
                我的订单
            </Title>

            {/* 状态筛选 */}
            <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} className="mb-4" />

            {/* 错误提示 */}
            {error && (
                <Alert
                    message="加载失败"
                    description={error}
                    type="error"
                    showIcon
                    className="mb-6"
                    action={<a onClick={fetchOrders}>重试</a>}
                />
            )}

            {/* 加载状态 */}
            {loading ? (
                <div className="page-loading">
                    <Spin size="large" tip="加载中..." />
                </div>
            ) : filteredOrders.length > 0 ? (
                <div>{filteredOrders.map(renderOrderCard)}</div>
            ) : (
                <Empty
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                    description="暂无订单"
                    className="py-20"
                >
                    <Button type="primary" onClick={() => navigate('/goods')}>
                        去抢购
                    </Button>
                </Empty>
            )}
        </div>
    )
}
