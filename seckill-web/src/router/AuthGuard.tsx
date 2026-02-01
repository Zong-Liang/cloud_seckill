import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useUserStore } from '@/store'

/**
 * 路由守卫组件
 * 用于保护需要登录才能访问的页面
 */
export default function AuthGuard() {
    const { isLoggedIn } = useUserStore()
    const location = useLocation()

    if (!isLoggedIn) {
        // 未登录，重定向到登录页，并记录当前路径
        return <Navigate to="/login" state={{ from: location.pathname }} replace />
    }

    // 已登录，渲染子路由
    return <Outlet />
}
