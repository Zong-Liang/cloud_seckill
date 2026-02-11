import { Timeline, Typography, Tag } from 'antd'
import {
    CheckCircleOutlined,
    CarOutlined,
    ShopOutlined,
    EnvironmentOutlined,
    SmileOutlined,
    ClockCircleOutlined,
} from '@ant-design/icons'
import type { OrderVO } from '@/types'
import { OrderStatus } from '@/types'
import dayjs from 'dayjs'
import './index.css'

const { Text } = Typography

interface LogisticsTimelineProps {
    order: OrderVO
}

interface LogisticsNode {
    title: string
    description: string
    time: string
    icon: React.ReactNode
    color: string
    active: boolean
}

/**
 * 模拟物流时间线组件
 * 根据订单状态和支付时间推算模拟物流节点
 */
export default function LogisticsTimeline({ order }: LogisticsTimelineProps) {
    const status = order.status as OrderStatus
    const payTime = order.payTime ? dayjs(order.payTime) : null

    if (!payTime || status < OrderStatus.PAID) {
        return null
    }

    // 根据支付时间推算物流节点
    const nodes: LogisticsNode[] = [
        {
            title: '订单已支付',
            description: '您的订单已完成支付，商家正在准备商品',
            time: payTime.format('MM-DD HH:mm'),
            icon: <CheckCircleOutlined />,
            color: 'green',
            active: status >= OrderStatus.PAID,
        },
        {
            title: '商家已发货',
            description: '包裹已从仓库出发，运单号：SF' + String(order.orderNo).slice(-10),
            time: payTime.add(2, 'hour').format('MM-DD HH:mm'),
            icon: <ShopOutlined />,
            color: status >= OrderStatus.SHIPPED ? 'green' : 'gray',
            active: status >= OrderStatus.SHIPPED,
        },
        {
            title: '运输中',
            description: '包裹已到达【本地分拣中心】，正在分拣',
            time: payTime.add(1, 'day').format('MM-DD HH:mm'),
            icon: <CarOutlined />,
            color: status >= OrderStatus.SHIPPED ? 'blue' : 'gray',
            active: status >= OrderStatus.SHIPPED,
        },
        {
            title: '派送中',
            description: '快递员正在派送，请注意接听电话',
            time: payTime.add(2, 'day').format('MM-DD HH:mm'),
            icon: <EnvironmentOutlined />,
            color: status >= OrderStatus.SHIPPED ? 'blue' : 'gray',
            active: status >= OrderStatus.SHIPPED,
        },
        {
            title: '已签收',
            description: '包裹已签收，感谢您的购买！',
            time: payTime.add(3, 'day').format('MM-DD HH:mm'),
            icon: <SmileOutlined />,
            color: status >= OrderStatus.RECEIVED ? 'green' : 'gray',
            active: status >= OrderStatus.RECEIVED,
        },
    ]

    // 找到当前活跃节点
    const currentIndex = status >= OrderStatus.RECEIVED ? 4
        : status >= OrderStatus.SHIPPED ? 3
            : 0

    return (
        <div className="logistics-timeline">
            <div className="flex items-center justify-between mb-4">
                <Text strong>物流信息</Text>
                {status === OrderStatus.SHIPPED && (
                    <Tag color="blue" icon={<CarOutlined />}>运输中</Tag>
                )}
                {status === OrderStatus.RECEIVED && (
                    <Tag color="green" icon={<SmileOutlined />}>已签收</Tag>
                )}
                {status === OrderStatus.PAID && (
                    <Tag color="orange" icon={<ClockCircleOutlined />}>待发货</Tag>
                )}
            </div>

            <Timeline
                items={nodes.map((node, index) => ({
                    dot: <span className={`logistics-dot ${index === currentIndex ? 'dot-active' : ''}`}>
                        {node.icon}
                    </span>,
                    color: node.color,
                    children: (
                        <div className={`logistics-node ${node.active ? 'is-active' : 'is-inactive'}`}>
                            <div className="flex items-center gap-2 mb-1">
                                <Text strong={index === currentIndex}>
                                    {node.title}
                                </Text>
                                <Text type="secondary" className="text-xs">
                                    {node.active ? node.time : '预计 ' + node.time}
                                </Text>
                            </div>
                            <Text type="secondary" className="text-sm">
                                {node.description}
                            </Text>
                        </div>
                    ),
                }))}
            />
        </div>
    )
}
