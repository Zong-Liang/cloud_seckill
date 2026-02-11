import { Typography, Button, message } from 'antd'
import {
    CopyOutlined,
    ThunderboltFilled,
} from '@ant-design/icons'
import type { OrderVO } from '@/types'
import { formatPrice } from '@/utils'
import { useUserStore } from '@/store'
import './index.css'

const { Text } = Typography

interface ShareCardProps {
    order: OrderVO
}

/**
 * 秒杀分享卡片组件
 */
export default function ShareCard({ order }: ShareCardProps) {
    const { user } = useUserStore()
    const saved = order.goodsPrice - (order.totalAmount / order.goodsCount)

    const shareText = `🎉 我在 Cloud Seckill 秒杀了「${order.goodsName}」，仅花 ¥${formatPrice(order.totalAmount)}，省了 ¥${formatPrice(saved)}！快来一起抢购吧！`

    const handleCopyLink = () => {
        const url = `${window.location.origin}/goods/${order.goodsId}`
        navigator.clipboard.writeText(`${shareText}\n${url}`).then(() => {
            message.success('分享内容已复制到剪贴板')
        }).catch(() => {
            message.error('复制失败，请手动复制')
        })
    }

    return (
        <div className="share-card-wrapper">
            {/* 卡片主体 */}
            <div className="share-card">
                {/* 头部渐变 */}
                <div className="share-card-header">
                    <div className="share-brand">
                        <ThunderboltFilled className="text-xl" />
                        <span className="text-lg font-bold">Cloud Seckill</span>
                    </div>
                    <div className="share-title">🎉 秒杀战绩</div>
                </div>

                {/* 商品信息 */}
                <div className="share-card-body">
                    <div className="share-goods">
                        <img
                            src={order.goodsImg || '/default-goods.png'}
                            alt={order.goodsName}
                            className="share-goods-img"
                        />
                        <div className="share-goods-info">
                            <Text strong className="text-base line-clamp-2">
                                {order.goodsName}
                            </Text>
                            <div className="mt-2">
                                <span className="share-price">¥{formatPrice(order.totalAmount)}</span>
                                <span className="share-original-price">
                                    ¥{formatPrice(order.goodsPrice)}
                                </span>
                            </div>
                            {saved > 0 && (
                                <div className="share-saved">
                                    比原价省了 ¥{formatPrice(saved)} 🔥
                                </div>
                            )}
                        </div>
                    </div>

                    {/* 用户信息 */}
                    <div className="share-user">
                        <Text type="secondary" className="text-sm">
                            — {user?.nickname || user?.username || '秒杀达人'} 的战绩
                        </Text>
                    </div>
                </div>

                {/* 底部 */}
                <div className="share-card-footer">
                    <Text type="secondary" className="text-xs">
                        长按保存图片 · 分享给好友一起抢
                    </Text>
                </div>
            </div>

            {/* 操作按钮 */}
            <div className="share-actions">
                <Button
                    type="primary"
                    icon={<CopyOutlined />}
                    onClick={handleCopyLink}
                    block
                    size="large"
                >
                    复制分享内容
                </Button>
            </div>
        </div>
    )
}
