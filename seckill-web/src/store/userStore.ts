import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { UserVO } from '@/types'

interface UserState {
    /** 当前用户信息 */
    user: UserVO | null
    /** Token */
    token: string | null
    /** 是否已登录 */
    isLoggedIn: boolean

    /** 设置用户信息（登录成功后调用） */
    setUser: (user: UserVO, token: string) => void
    /** 登出 */
    logout: () => void
    /** 更新用户信息 */
    updateUser: (user: Partial<UserVO>) => void
}

export const useUserStore = create<UserState>()(
    persist(
        (set, get) => ({
            user: null,
            token: null,
            isLoggedIn: false,

            setUser: (user, token) => {
                // 同时保存到 localStorage（用于 axios 拦截器）
                localStorage.setItem('token', token)
                set({
                    user,
                    token,
                    isLoggedIn: true
                })
            },

            logout: () => {
                localStorage.removeItem('token')
                set({
                    user: null,
                    token: null,
                    isLoggedIn: false
                })
            },

            updateUser: (updates) => {
                const currentUser = get().user
                if (currentUser) {
                    set({
                        user: { ...currentUser, ...updates }
                    })
                }
            },
        }),
        {
            name: 'seckill-user-storage',
            // 只持久化这些字段
            partialize: (state) => ({
                user: state.user,
                token: state.token,
                isLoggedIn: state.isLoggedIn,
            }),
        }
    )
)
