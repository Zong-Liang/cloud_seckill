import { Suspense, lazy } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { Spin } from 'antd'
import { MainLayout } from '@/layouts'
import AuthGuard from './AuthGuard'

// 懒加载页面组件
const Home = lazy(() => import('@/pages/Home'))
const Login = lazy(() => import('@/pages/Login'))
const Register = lazy(() => import('@/pages/Register'))
const GoodsList = lazy(() => import('@/pages/GoodsList'))
const GoodsDetail = lazy(() => import('@/pages/GoodsDetail'))
const SeckillResult = lazy(() => import('@/pages/SeckillResult'))
const OrderList = lazy(() => import('@/pages/OrderList'))
const OrderDetail = lazy(() => import('@/pages/OrderDetail'))
const Pay = lazy(() => import('@/pages/Pay'))
const Profile = lazy(() => import('@/pages/Profile'))
const AddressList = lazy(() => import('@/pages/AddressList'))
const Favorites = lazy(() => import('@/pages/Favorites'))

// 加载占位组件
const PageLoading = () => (
    <div className="page-loading min-h-96">
        <Spin size="large" tip="加载中..." />
    </div>
)

/**
 * 应用路由配置
 */
export default function AppRouter() {
    return (
        <Routes>
            {/* 登录/注册页面（不使用主布局） */}
            <Route
                path="/login"
                element={
                    <Suspense fallback={<PageLoading />}>
                        <Login />
                    </Suspense>
                }
            />
            <Route
                path="/register"
                element={
                    <Suspense fallback={<PageLoading />}>
                        <Register />
                    </Suspense>
                }
            />

            {/* 使用主布局的页面 */}
            <Route path="/" element={<MainLayout />}>
                {/* 首页 */}
                <Route
                    index
                    element={
                        <Suspense fallback={<PageLoading />}>
                            <Home />
                        </Suspense>
                    }
                />

                {/* 商品列表 */}
                <Route
                    path="goods"
                    element={
                        <Suspense fallback={<PageLoading />}>
                            <GoodsList />
                        </Suspense>
                    }
                />

                {/* 商品详情 */}
                <Route
                    path="goods/:id"
                    element={
                        <Suspense fallback={<PageLoading />}>
                            <GoodsDetail />
                        </Suspense>
                    }
                />

                {/* 需要登录的页面 */}
                <Route element={<AuthGuard />}>
                    {/* 秒杀结果 */}
                    <Route
                        path="seckill/result/:orderNo"
                        element={
                            <Suspense fallback={<PageLoading />}>
                                <SeckillResult />
                            </Suspense>
                        }
                    />

                    {/* 订单列表 */}
                    <Route
                        path="orders"
                        element={
                            <Suspense fallback={<PageLoading />}>
                                <OrderList />
                            </Suspense>
                        }
                    />

                    {/* 订单详情 */}
                    <Route
                        path="order/:orderNo"
                        element={
                            <Suspense fallback={<PageLoading />}>
                                <OrderDetail />
                            </Suspense>
                        }
                    />

                    {/* 支付页面 */}
                    <Route
                        path="pay/:orderNo"
                        element={
                            <Suspense fallback={<PageLoading />}>
                                <Pay />
                            </Suspense>
                        }
                    />

                    {/* 个人中心 */}
                    <Route
                        path="profile"
                        element={
                            <Suspense fallback={<PageLoading />}>
                                <Profile />
                            </Suspense>
                        }
                    />

                    {/* 收货地址 */}
                    <Route
                        path="address"
                        element={
                            <Suspense fallback={<PageLoading />}>
                                <AddressList />
                            </Suspense>
                        }
                    />

                    {/* 我的收藏 */}
                    <Route
                        path="favorites"
                        element={
                            <Suspense fallback={<PageLoading />}>
                                <Favorites />
                            </Suspense>
                        }
                    />
                </Route>
            </Route>

            {/* 404 重定向到首页 */}
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    )
}
