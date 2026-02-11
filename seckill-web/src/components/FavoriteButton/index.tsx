import { useState } from 'react'
import { HeartOutlined, HeartFilled } from '@ant-design/icons'
import { useFavoriteStore } from '@/store/favoriteStore'
import './index.css'

interface FavoriteButtonProps {
    goodsId: number
    size?: number
    className?: string
}

/**
 * 收藏按钮组件 — 心形图标，点击切换收藏状态
 */
export default function FavoriteButton({ goodsId, size = 22, className = '' }: FavoriteButtonProps) {
    const { isFavorited, toggleFavorite } = useFavoriteStore()
    const favorited = isFavorited(goodsId)
    const [animating, setAnimating] = useState(false)

    const handleClick = (e: React.MouseEvent) => {
        e.preventDefault()
        e.stopPropagation()
        if (!favorited) {
            setAnimating(true)
            setTimeout(() => setAnimating(false), 400)
        }
        toggleFavorite(goodsId)
    }

    return (
        <span
            className={`favorite-btn ${favorited ? 'is-favorited' : ''} ${animating ? 'is-animating' : ''} ${className}`}
            onClick={handleClick}
            title={favorited ? '取消收藏' : '收藏商品'}
        >
            {favorited ? (
                <HeartFilled style={{ fontSize: size, color: '#ff4d4f' }} />
            ) : (
                <HeartOutlined style={{ fontSize: size, color: '#999' }} />
            )}
        </span>
    )
}
