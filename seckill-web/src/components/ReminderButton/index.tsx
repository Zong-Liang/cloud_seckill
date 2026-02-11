import { Button, message } from 'antd'
import { BellOutlined, BellFilled } from '@ant-design/icons'
import { useFavoriteStore } from '@/store/favoriteStore'

interface ReminderButtonProps {
    goodsId: number
    goodsName: string
    startTime: string
    className?: string
}

/**
 * 开抢提醒按钮 — 点击设置/取消浏览器通知提醒
 */
export default function ReminderButton({ goodsId, goodsName, startTime, className = '' }: ReminderButtonProps) {
    const { hasReminder, setReminder, removeReminder } = useFavoriteStore()
    const reminded = hasReminder(goodsId)

    const handleClick = async () => {
        if (reminded) {
            removeReminder(goodsId)
            message.info('已取消开抢提醒')
            return
        }

        // 检查通知权限
        if ('Notification' in window) {
            const permission = await Notification.requestPermission()
            if (permission !== 'granted') {
                message.warning('请允许浏览器通知权限以接收提醒')
                return
            }
        }

        setReminder(goodsId, startTime, goodsName)
        message.success('已设置开抢提醒，将在开始前 5 分钟通知您')
    }

    return (
        <Button
            icon={reminded ? <BellFilled /> : <BellOutlined />}
            type={reminded ? 'primary' : 'default'}
            ghost={reminded}
            onClick={handleClick}
            className={className}
        >
            {reminded ? '已设提醒' : '开抢提醒'}
        </Button>
    )
}
