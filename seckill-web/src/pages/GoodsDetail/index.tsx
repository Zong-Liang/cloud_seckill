import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
    Typography,
    Spin,
    Alert,
    Breadcrumb,
    Tag,
    Divider,
    Card,
    Image,
    Space,
} from 'antd'
import { HomeOutlined, FireOutlined } from '@ant-design/icons'
import { getGoodsDetail } from '@/api'
import { CountDown, SeckillButton, FavoriteButton, ReminderButton } from '@/components'
import type { GoodsVO } from '@/types'
import { GoodsStatus, GoodsStatusColor, GoodsStatusText } from '@/types'
import { formatPrice, calcDiscount, formatTime, getRealStatus } from '@/utils'
import './index.css'

const { Title, Text, Paragraph } = Typography

/**
 * 商品详情页
 */
export default function GoodsDetail() {
    const { id } = useParams<{ id: string }>()
    const [loading, setLoading] = useState(true)
    const [goods, setGoods] = useState<GoodsVO | null>(null)
    const [error, setError] = useState<string | null>(null)

    const fetchGoods = async (goodsId: number, showLoading = true) => {
        if (showLoading) setLoading(true)
        setError(null)
        try {
            const result = await getGoodsDetail(goodsId)
            setGoods(result.data)
        } catch (err) {
            if (showLoading) setError('获取商品详情失败')
            console.error(err)
        } finally {
            if (showLoading) setLoading(false)
        }
    }

    useEffect(() => {
        if (id) {
            fetchGoods(Number(id))
        }
    }, [id])

    // 刷新商品（倒计时结束时），静默刷新不显示 loading
    const handleRefresh = () => {
        if (id) {
            fetchGoods(Number(id), false)
        }
    }

    if (loading) {
        return (
            <div className="page-loading min-h-96">
                <Spin size="large" tip="加载中..." />
            </div>
        )
    }

    if (error || !goods) {
        return (
            <Alert
                message="加载失败"
                description={error || '商品不存在'}
                type="error"
                showIcon
                action={<Link to="/goods">返回列表</Link>}
            />
        )
    }

    const discount = calcDiscount(goods.goodsPrice, goods.seckillPrice)
    const status = getRealStatus(goods)

    return (
        <div className="goods-detail-page">
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
                        title: <Link to="/goods">商品列表</Link>,
                    },
                    { title: goods.goodsName },
                ]}
            />

            {/* 商品信息 */}
            <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
                <div className="flex flex-col lg:flex-row">
                    {/* 商品图片 */}
                    <div className="lg:w-1/2 p-4 lg:p-8 bg-gray-50">
                        <div className="relative aspect-square rounded-xl overflow-hidden">
                            <Image
                                src={goods.goodsImg || '/default-goods.png'}
                                alt={goods.goodsName}
                                className="w-full h-full object-cover"
                                fallback="/default-goods.png"
                            />
                            {/* 折扣标签 */}
                            {discount > 0 && (
                                <div className="absolute top-4 right-4 bg-gradient-to-r from-red-500 to-orange-500 text-white px-4 py-2 rounded-full text-sm font-bold">
                                    省 {discount}%
                                </div>
                            )}
                        </div>
                    </div>

                    {/* 商品信息 */}
                    <div className="lg:w-1/2 p-6 lg:p-8 flex flex-col">
                        {/* 状态标签 */}
                        <div className="mb-4">
                            <Tag
                                color={GoodsStatusColor[status]}
                                icon={status === GoodsStatus.IN_PROGRESS ? <FireOutlined /> : undefined}
                                className="text-sm px-3 py-1"
                            >
                                {GoodsStatusText[status]}
                            </Tag>
                        </div>

                        {/* 商品名称 */}
                        <div className="flex items-start gap-3">
                            <Title level={2} className="mb-2 flex-1">
                                {goods.goodsName}
                            </Title>
                            <FavoriteButton goodsId={goods.id} size={28} className="mt-2" />
                        </div>
                        <Text type="secondary" className="mb-4">
                            {goods.goodsTitle}
                        </Text>

                        {/* 价格区域 */}
                        <div className="bg-gradient-to-r from-red-50 to-orange-50 rounded-xl p-4 mb-6">
                            <div className="flex items-baseline gap-4 mb-2">
                                <div>
                                    <Text type="secondary" className="text-sm">秒杀价</Text>
                                    <div className="text-red-500 text-3xl font-bold">
                                        ¥{formatPrice(goods.seckillPrice)}
                                    </div>
                                </div>
                                <div>
                                    <Text type="secondary" className="text-sm">原价</Text>
                                    <div className="text-gray-400 line-through text-lg">
                                        ¥{formatPrice(goods.goodsPrice)}
                                    </div>
                                </div>
                                {discount > 0 && (
                                    <Tag color="red" className="ml-auto">
                                        立省 ¥{formatPrice(goods.goodsPrice - goods.seckillPrice)}
                                    </Tag>
                                )}
                            </div>
                        </div>

                        {/* 库存和活动时间 */}
                        <div className="space-y-3 mb-6">
                            <div className="flex justify-between">
                                <Text type="secondary">剩余库存</Text>
                                <Text strong className={goods.stockCount <= 10 ? 'text-red-500' : ''}>
                                    {goods.stockCount} 件
                                </Text>
                            </div>
                            <div className="flex justify-between">
                                <Text type="secondary">活动时间</Text>
                                <Text>
                                    {formatTime(goods.startTime, 'MM-DD HH:mm')} ~ {formatTime(goods.endTime, 'MM-DD HH:mm')}
                                </Text>
                            </div>
                        </div>

                        <Divider className="my-4" />

                        {/* 倒计时 */}
                        <div className="mb-6">
                            {status === GoodsStatus.IN_PROGRESS && (
                                <CountDown
                                    endTime={goods.endTime}
                                    prefix="距结束"
                                    size="large"
                                    onEnd={handleRefresh}
                                />
                            )}
                            {status === GoodsStatus.NOT_STARTED && (
                                <CountDown
                                    endTime={goods.startTime}
                                    prefix="距开始"
                                    size="large"
                                    onEnd={handleRefresh}
                                />
                            )}
                        </div>

                        {/* 秒杀按钮 */}
                        <div className="mt-auto flex gap-3">
                            <SeckillButton goods={goods} size="large" />
                            {status === GoodsStatus.NOT_STARTED && (
                                <ReminderButton
                                    goodsId={goods.id}
                                    goodsName={goods.goodsName}
                                    startTime={goods.startTime}
                                />
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* 商品详情 */}
            <Card className="mt-6" title="商品详情">
                <div
                    className="goods-detail-content"
                    dangerouslySetInnerHTML={{ __html: goods.goodsDetail || '暂无详情' }}
                />
            </Card>
        </div>
    )
}
