import { Card, Tag, Typography, Progress } from 'antd'
import { useNavigate } from 'react-router-dom'
import { FireOutlined } from '@ant-design/icons'
import type { GoodsVO } from '@/types'
import { GoodsStatus, GoodsStatusColor, GoodsStatusText } from '@/types'
import { formatPrice, calcDiscount } from '@/utils'
import CountDown from '../CountDown'
import './index.css'

const { Text, Title } = Typography

interface GoodsCardProps {
    /** 商品信息 */
    goods: GoodsVO
}

/**
 * 商品卡片组件
 */
export default function GoodsCard({ goods }: GoodsCardProps) {
    const navigate = useNavigate()

    // 计算折扣
    const discount = calcDiscount(goods.goodsPrice, goods.seckillPrice)

    // 计算库存进度
    const stockPercent = Math.min(100, Math.max(0, (goods.stockCount / 100) * 100))

    // 获取状态标签
    const getStatusTag = () => {
        const status = goods.status as GoodsStatus
        const icon = status === GoodsStatus.IN_PROGRESS ? <FireOutlined /> : undefined
        return (
            <Tag color={GoodsStatusColor[status]} icon={icon}>
                {GoodsStatusText[status]}
            </Tag>
        )
    }

    return (
        <Card
            hoverable
            className="goods-card overflow-hidden"
            cover={
                <div className="goods-card-cover relative aspect-square bg-gray-100">
                    <img
                        src={goods.goodsImg || '/default-goods.png'}
                        alt={goods.goodsName}
                        className="w-full h-full object-cover"
                        loading="lazy"
                    />
                    {/* 折扣标签 */}
                    {discount > 0 && (
                        <div className="discount-tag">
                            省 {discount}%
                        </div>
                    )}
                </div>
            }
            onClick={() => navigate(`/goods/${goods.id}`)}
        >
            <div className="goods-card-content">
                {/* 状态和库存 */}
                <div className="flex justify-between items-center mb-2">
                    {getStatusTag()}
                    <Text type="secondary" className="text-xs">
                        剩余 {goods.stockCount} 件
                    </Text>
                </div>

                {/* 商品名称 */}
                <Title level={5} ellipsis={{ rows: 2 }} className="goods-name mb-2 h-11">
                    {goods.goodsName}
                </Title>

                {/* 价格 */}
                <div className="flex items-baseline gap-2 mb-2">
                    <Text className="price-seckill">
                        ¥{formatPrice(goods.seckillPrice)}
                    </Text>
                    <Text delete type="secondary" className="text-xs">
                        ¥{formatPrice(goods.goodsPrice)}
                    </Text>
                </div>

                {/* 库存进度条 */}
                <div className="mb-2">
                    <Progress
                        percent={stockPercent}
                        size="small"
                        showInfo={false}
                        strokeColor={{
                            '0%': '#ff4d4f',
                            '100%': '#fa8c16',
                        }}
                    />
                </div>

                {/* 倒计时 */}
                {goods.status === GoodsStatus.IN_PROGRESS && (
                    <CountDown
                        endTime={goods.endTime}
                        prefix="剩余"
                        size="small"
                    />
                )}
                {goods.status === GoodsStatus.NOT_STARTED && (
                    <CountDown
                        endTime={goods.endTime}
                        startTime={goods.startTime}
                        prefix="距开始"
                        size="small"
                    />
                )}
            </div>
        </Card>
    )
}
