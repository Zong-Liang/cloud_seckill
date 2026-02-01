import { useState, useEffect } from 'react'
import { Typography, Spin, Empty, Alert, Tabs, Select, Breadcrumb } from 'antd'
import { HomeOutlined } from '@ant-design/icons'
import { Link } from 'react-router-dom'
import { getGoodsList } from '@/api'
import { GoodsCard } from '@/components'
import type { GoodsVO } from '@/types'
import { GoodsStatus, GoodsStatusText } from '@/types'
import './index.css'

const { Title } = Typography

/**
 * 商品列表页
 */
export default function GoodsList() {
    const [loading, setLoading] = useState(true)
    const [goodsList, setGoodsList] = useState<GoodsVO[]>([])
    const [error, setError] = useState<string | null>(null)
    const [statusFilter, setStatusFilter] = useState<string>('all')
    const [sortBy, setSortBy] = useState<string>('default')

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

    // 过滤商品
    const getFilteredGoods = () => {
        let filtered = [...goodsList]

        // 状态过滤
        if (statusFilter !== 'all') {
            filtered = filtered.filter((g) => g.status === Number(statusFilter))
        }

        // 排序
        switch (sortBy) {
            case 'price-asc':
                filtered.sort((a, b) => a.seckillPrice - b.seckillPrice)
                break
            case 'price-desc':
                filtered.sort((a, b) => b.seckillPrice - a.seckillPrice)
                break
            case 'stock-desc':
                filtered.sort((a, b) => b.stockCount - a.stockCount)
                break
            case 'discount':
                filtered.sort((a, b) => {
                    const discountA = (a.goodsPrice - a.seckillPrice) / a.goodsPrice
                    const discountB = (b.goodsPrice - b.seckillPrice) / b.goodsPrice
                    return discountB - discountA
                })
                break
            default:
                // 默认按状态排序：进行中 > 未开始 > 已结束
                filtered.sort((a, b) => {
                    const order = { 1: 0, 0: 1, 2: 2, 3: 3 }
                    return (order[a.status as keyof typeof order] || 9) - (order[b.status as keyof typeof order] || 9)
                })
        }

        return filtered
    }

    const filteredGoods = getFilteredGoods()

    // Tab 选项
    const tabItems = [
        { key: 'all', label: `全部 (${goodsList.length})` },
        {
            key: String(GoodsStatus.IN_PROGRESS),
            label: `${GoodsStatusText[GoodsStatus.IN_PROGRESS]} (${goodsList.filter((g) => g.status === GoodsStatus.IN_PROGRESS).length
                })`,
        },
        {
            key: String(GoodsStatus.NOT_STARTED),
            label: `${GoodsStatusText[GoodsStatus.NOT_STARTED]} (${goodsList.filter((g) => g.status === GoodsStatus.NOT_STARTED).length
                })`,
        },
        {
            key: String(GoodsStatus.ENDED),
            label: `${GoodsStatusText[GoodsStatus.ENDED]} (${goodsList.filter((g) => g.status === GoodsStatus.ENDED).length
                })`,
        },
    ]

    return (
        <div className="goods-list-page">
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
                    { title: '商品列表' },
                ]}
            />

            {/* 标题和筛选 */}
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6">
                <Title level={2} className="mb-0">
                    秒杀商品
                </Title>
                <Select
                    value={sortBy}
                    onChange={setSortBy}
                    style={{ width: 150 }}
                    options={[
                        { value: 'default', label: '默认排序' },
                        { value: 'price-asc', label: '价格从低到高' },
                        { value: 'price-desc', label: '价格从高到低' },
                        { value: 'stock-desc', label: '库存从多到少' },
                        { value: 'discount', label: '折扣力度' },
                    ]}
                />
            </div>

            {/* 状态筛选 Tabs */}
            <Tabs
                activeKey={statusFilter}
                onChange={setStatusFilter}
                items={tabItems}
                className="mb-6"
            />

            {/* 错误提示 */}
            {error && (
                <Alert
                    message="加载失败"
                    description={error}
                    type="error"
                    showIcon
                    className="mb-6"
                    action={<a onClick={fetchGoods}>重试</a>}
                />
            )}

            {/* 加载状态 */}
            {loading ? (
                <div className="page-loading">
                    <Spin size="large" tip="加载中..." />
                </div>
            ) : (
                <>
                    {/* 商品网格 */}
                    {filteredGoods.length > 0 ? (
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                            {filteredGoods.map((goods) => (
                                <GoodsCard key={goods.id} goods={goods} />
                            ))}
                        </div>
                    ) : (
                        <Empty description="暂无商品" className="py-20" />
                    )}
                </>
            )}
        </div>
    )
}
