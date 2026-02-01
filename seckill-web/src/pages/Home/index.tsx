import { useState, useEffect } from 'react'
import { Typography, Spin, Empty, Alert, Carousel } from 'antd'
import { ThunderboltOutlined, FireOutlined, SafetyCertificateOutlined, RocketOutlined } from '@ant-design/icons'
import { getGoodsList } from '@/api'
import { GoodsCard } from '@/components'
import type { GoodsVO } from '@/types'
import { GoodsStatus } from '@/types'
import './index.css'

const { Title, Text, Paragraph } = Typography

/**
 * 首页
 */
export default function Home() {
    const [loading, setLoading] = useState(true)
    const [goodsList, setGoodsList] = useState<GoodsVO[]>([])
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        fetchGoods()
    }, [])

    const fetchGoods = async () => {
        setLoading(true)
        setError(null)
        try {
            const result = await getGoodsList()
            setGoodsList(result.data || [])
        } catch (err) {
            setError('获取商品列表失败，请稍后重试')
            console.error(err)
        } finally {
            setLoading(false)
        }
    }

    // 筛选进行中的秒杀商品
    const activeGoods = goodsList.filter((g) => g.status === GoodsStatus.IN_PROGRESS)
    // 即将开始的商品
    const upcomingGoods = goodsList.filter((g) => g.status === GoodsStatus.NOT_STARTED)

    return (
        <div className="home-page">
            {/* Hero Banner */}
            <section className="hero-section bg-gradient-to-r from-red-500 via-orange-500 to-yellow-500 rounded-2xl p-8 md:p-12 mb-8 text-white">
                <div className="flex flex-col md:flex-row items-center justify-between">
                    <div className="mb-6 md:mb-0">
                        <Title level={1} className="text-white mb-4">
                            <ThunderboltOutlined className="mr-2" />
                            限时秒杀
                        </Title>
                        <Paragraph className="text-white/90 text-lg mb-0 max-w-lg">
                            每日精选好物，超低价格，限量抢购。千万用户的信赖之选，高并发技术保障公平公正。
                        </Paragraph>
                    </div>
                    <div className="flex gap-4">
                        <div className="text-center">
                            <div className="text-4xl font-bold">{activeGoods.length}</div>
                            <div className="text-white/80">进行中</div>
                        </div>
                        <div className="text-center">
                            <div className="text-4xl font-bold">{upcomingGoods.length}</div>
                            <div className="text-white/80">即将开始</div>
                        </div>
                    </div>
                </div>
            </section>

            {/* 特色展示 */}
            <section className="features-section grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                <div className="feature-card bg-white rounded-xl p-6 flex items-center gap-4 shadow-sm">
                    <FireOutlined className="text-3xl text-red-500" />
                    <div>
                        <Text strong>限时低价</Text>
                        <br />
                        <Text type="secondary" className="text-sm">超值折扣，先到先得</Text>
                    </div>
                </div>
                <div className="feature-card bg-white rounded-xl p-6 flex items-center gap-4 shadow-sm">
                    <SafetyCertificateOutlined className="text-3xl text-green-500" />
                    <div>
                        <Text strong>正品保障</Text>
                        <br />
                        <Text type="secondary" className="text-sm">品质保证，售后无忧</Text>
                    </div>
                </div>
                <div className="feature-card bg-white rounded-xl p-6 flex items-center gap-4 shadow-sm">
                    <RocketOutlined className="text-3xl text-blue-500" />
                    <div>
                        <Text strong>极速发货</Text>
                        <br />
                        <Text type="secondary" className="text-sm">下单即发，闪电送达</Text>
                    </div>
                </div>
            </section>

            {/* 错误提示 */}
            {error && (
                <Alert
                    message="加载失败"
                    description={error}
                    type="error"
                    showIcon
                    className="mb-6"
                    action={
                        <a onClick={fetchGoods}>重试</a>
                    }
                />
            )}

            {/* 加载状态 */}
            {loading ? (
                <div className="page-loading">
                    <Spin size="large" tip="加载中..." />
                </div>
            ) : (
                <>
                    {/* 正在进行的秒杀 */}
                    {activeGoods.length > 0 && (
                        <section className="mb-8">
                            <div className="flex items-center gap-2 mb-4">
                                <FireOutlined className="text-red-500 text-xl" />
                                <Title level={3} className="mb-0">
                                    🔥 正在秒杀
                                </Title>
                            </div>
                            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                                {activeGoods.map((goods) => (
                                    <GoodsCard key={goods.id} goods={goods} />
                                ))}
                            </div>
                        </section>
                    )}

                    {/* 即将开始 */}
                    {upcomingGoods.length > 0 && (
                        <section className="mb-8">
                            <div className="flex items-center gap-2 mb-4">
                                <Title level={3} className="mb-0">
                                    ⏰ 即将开始
                                </Title>
                            </div>
                            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                                {upcomingGoods.map((goods) => (
                                    <GoodsCard key={goods.id} goods={goods} />
                                ))}
                            </div>
                        </section>
                    )}

                    {/* 所有商品 */}
                    {goodsList.length > 0 && activeGoods.length === 0 && upcomingGoods.length === 0 && (
                        <section className="mb-8">
                            <Title level={3} className="mb-4">全部商品</Title>
                            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                                {goodsList.map((goods) => (
                                    <GoodsCard key={goods.id} goods={goods} />
                                ))}
                            </div>
                        </section>
                    )}

                    {/* 空状态 */}
                    {goodsList.length === 0 && !error && (
                        <Empty
                            description="暂无商品"
                            className="py-20"
                        />
                    )}
                </>
            )}

            {/* 活动规则 */}
            <section className="rules-section bg-white rounded-xl p-6 shadow-sm">
                <Title level={4}>📌 活动规则</Title>
                <ul className="text-gray-600 space-y-2 list-disc list-inside">
                    <li>每人每件商品限购1件</li>
                    <li>下单后15分钟内完成支付，否则订单自动取消</li>
                    <li>秒杀商品不支持退换货</li>
                    <li>恶意刷单行为将被封禁账号</li>
                </ul>
            </section>
        </div>
    )
}
