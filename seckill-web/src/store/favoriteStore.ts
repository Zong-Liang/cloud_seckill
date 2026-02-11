import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface FavoriteItem {
    goodsId: number
    timestamp: number
}

interface ReminderItem {
    goodsId: number
    startTime: string
    timerId?: ReturnType<typeof setTimeout>
}

interface FavoriteState {
    favorites: FavoriteItem[]
    reminders: ReminderItem[]

    toggleFavorite: (goodsId: number) => void
    isFavorited: (goodsId: number) => boolean
    getFavoriteIds: () => number[]

    setReminder: (goodsId: number, startTime: string, goodsName: string) => void
    removeReminder: (goodsId: number) => void
    hasReminder: (goodsId: number) => boolean
}

export const useFavoriteStore = create<FavoriteState>()(
    persist(
        (set, get) => ({
            favorites: [],
            reminders: [],

            toggleFavorite: (goodsId) => {
                set((state) => {
                    const exists = state.favorites.some((f) => f.goodsId === goodsId)
                    if (exists) {
                        return {
                            favorites: state.favorites.filter((f) => f.goodsId !== goodsId),
                        }
                    }
                    return {
                        favorites: [...state.favorites, { goodsId, timestamp: Date.now() }],
                    }
                })
            },

            isFavorited: (goodsId) => {
                return get().favorites.some((f) => f.goodsId === goodsId)
            },

            getFavoriteIds: () => {
                return get().favorites.map((f) => f.goodsId)
            },

            setReminder: (goodsId, startTime, goodsName) => {
                const start = new Date(startTime).getTime()
                const now = Date.now()
                const remindAt = start - 5 * 60 * 1000 // 提前 5 分钟

                if (remindAt <= now) return // 已过提醒时间

                // 请求浏览器通知权限
                if ('Notification' in window && Notification.permission === 'default') {
                    Notification.requestPermission()
                }

                // 设置定时器
                const timerId = setTimeout(() => {
                    if ('Notification' in window && Notification.permission === 'granted') {
                        new Notification('⏰ 秒杀即将开始！', {
                            body: `「${goodsName}」将在 5 分钟后开始秒杀，快去抢购吧！`,
                            icon: '/favicon.ico',
                        })
                    }
                }, remindAt - now)

                set((state) => ({
                    reminders: [
                        ...state.reminders.filter((r) => r.goodsId !== goodsId),
                        { goodsId, startTime, timerId },
                    ],
                }))
            },

            removeReminder: (goodsId) => {
                const reminder = get().reminders.find((r) => r.goodsId === goodsId)
                if (reminder?.timerId) {
                    clearTimeout(reminder.timerId)
                }
                set((state) => ({
                    reminders: state.reminders.filter((r) => r.goodsId !== goodsId),
                }))
            },

            hasReminder: (goodsId) => {
                return get().reminders.some((r) => r.goodsId === goodsId)
            },
        }),
        {
            name: 'seckill-favorite-storage',
            partialize: (state) => ({
                favorites: state.favorites,
                // reminders 中的 timerId 不序列化
                reminders: state.reminders.map(({ goodsId, startTime }) => ({
                    goodsId,
                    startTime,
                })),
            }),
        }
    )
)
