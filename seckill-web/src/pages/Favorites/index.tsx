import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Typography, Breadcrumb, Empty, Button, Spin, Alert } from 'antd'
import { HomeOutlined, HeartOutlined } from '@ant-design/icons'
import { getGoodsList } from '@/api'
import { GoodsCard } from '@/components'
import { useFavoriteStore } from '@/store/favoriteStore'
import type { GoodsVO } from '@/types'

const { Title } = Typography

/**
 * 我的收藏页面
 */
export default function Favorites() {
    const { favorites } = useFavoriteStore()
    const favoriteIds = favorites.map((f) => f.goodsId)
    const [loading, setLoading] = useState(true)
    const [allGoods, setAllGoods] = useState<GoodsVO[]>([])
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        fetchGoods()
    }, [])

    const fetchGoods = async () => {
        setLoading(true)
        setError(null)
        try {
            const result = await getGoodsList()
            setAllGoods(result.data || [])
        } catch {
            setError('获取商品失败')
        } finally {
            setLoading(false)
        }
    }

    // 过滤出已收藏商品
    const favoriteGoods = allGoods.filter((g) => favoriteIds.includes(g.id))

    return (
        <div className="favorites-page">
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
                    { title: '我的收藏' },
                ]}
            />

            <div className="flex items-center gap-2 mb-6">
                <HeartOutlined className="text-red-500 text-xl" />
                <Title level={3} className="mb-0">
                    我的收藏 ({favoriteIds.length})
                </Title>
            </div>

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

            {loading ? (
                <div className="page-loading min-h-96">
                    <Spin size="large" tip="加载中..." />
                </div>
            ) : favoriteGoods.length === 0 ? (
                <Empty
                    description={favoriteIds.length > 0 ? '收藏的商品已下架' : '还没有收藏任何商品'}
                    className="py-20"
                >
                    <Link to="/goods">
                        <Button type="primary">去逛逛</Button>
                    </Link>
                </Empty>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                    {favoriteGoods.map((goods) => (
                        <GoodsCard key={goods.id} goods={goods} />
                    ))}
                </div>
            )}
        </div>
    )
}
