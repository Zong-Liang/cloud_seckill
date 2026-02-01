import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { Layout, Menu, Button, Dropdown, Avatar, Space, Typography } from 'antd'
import {
    HomeOutlined,
    ShoppingOutlined,
    OrderedListOutlined,
    UserOutlined,
    LogoutOutlined,
    ThunderboltFilled,
} from '@ant-design/icons'
import { useUserStore } from '@/store'
import type { MenuProps } from 'antd'
import './index.css'

const { Header, Content, Footer } = Layout
const { Text } = Typography

/**
 * 应用主布局组件
 */
export default function AppLayout() {
    const navigate = useNavigate()
    const location = useLocation()
    const { user, isLoggedIn, logout } = useUserStore()

    // 导航菜单项
    const menuItems: MenuProps['items'] = [
        {
            key: '/',
            icon: <HomeOutlined />,
            label: '首页',
        },
        {
            key: '/goods',
            icon: <ShoppingOutlined />,
            label: '商品列表',
        },
    ]

    // 已登录用户菜单项
    const loggedInMenuItems: MenuProps['items'] = [
        ...menuItems,
        {
            key: '/orders',
            icon: <OrderedListOutlined />,
            label: '我的订单',
        },
    ]

    // 用户下拉菜单
    const userMenuItems: MenuProps['items'] = [
        {
            key: 'profile',
            icon: <UserOutlined />,
            label: '个人中心',
            onClick: () => navigate('/profile'),
        },
        {
            key: 'orders',
            icon: <OrderedListOutlined />,
            label: '我的订单',
            onClick: () => navigate('/orders'),
        },
        {
            type: 'divider',
        },
        {
            key: 'logout',
            icon: <LogoutOutlined />,
            label: '退出登录',
            danger: true,
            onClick: () => {
                logout()
                navigate('/')
            },
        },
    ]

    // 获取当前选中的菜单项
    const getSelectedKey = () => {
        const path = location.pathname
        if (path.startsWith('/goods')) return '/goods'
        if (path.startsWith('/orders') || path.startsWith('/order')) return '/orders'
        return '/'
    }

    return (
        <Layout className="app-layout min-h-screen">
            {/* 顶部导航 */}
            <Header className="app-header bg-white shadow-sm sticky top-0 z-50 px-4 md:px-8">
                <div className="max-w-7xl mx-auto flex items-center justify-between h-full">
                    {/* Logo */}
                    <div
                        className="flex items-center gap-2 cursor-pointer"
                        onClick={() => navigate('/')}
                    >
                        <ThunderboltFilled className="text-2xl text-red-500" />
                        <span className="text-xl font-bold bg-gradient-to-r from-red-500 to-orange-500 bg-clip-text text-transparent">
                            Cloud Seckill
                        </span>
                    </div>

                    {/* 导航菜单 */}
                    <Menu
                        mode="horizontal"
                        selectedKeys={[getSelectedKey()]}
                        items={isLoggedIn ? loggedInMenuItems : menuItems}
                        onClick={({ key }) => navigate(key)}
                        className="flex-1 justify-center border-none hidden md:flex"
                    />

                    {/* 用户区域 */}
                    <div className="flex items-center gap-4">
                        {isLoggedIn && user ? (
                            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
                                <Space className="cursor-pointer hover:opacity-80">
                                    <Avatar
                                        src={user.avatar}
                                        icon={!user.avatar && <UserOutlined />}
                                        className="bg-red-500"
                                    />
                                    <Text className="hidden sm:inline max-w-24 truncate">
                                        {user.nickname || user.username}
                                    </Text>
                                </Space>
                            </Dropdown>
                        ) : (
                            <Space>
                                <Button type="link" onClick={() => navigate('/login')}>
                                    登录
                                </Button>
                                <Button type="primary" danger onClick={() => navigate('/register')}>
                                    注册
                                </Button>
                            </Space>
                        )}
                    </div>
                </div>
            </Header>

            {/* 内容区域 */}
            <Content className="app-content flex-1">
                <div className="max-w-7xl mx-auto px-4 md:px-8 py-6">
                    <Outlet />
                </div>
            </Content>

            {/* 底部 */}
            <Footer className="app-footer bg-gray-800 text-gray-400 text-center py-6">
                <div className="max-w-7xl mx-auto">
                    <p className="mb-2">Cloud Seckill - 高并发分布式秒杀系统</p>
                    <p className="text-xs">
                        © {new Date().getFullYear()} Cloud Seckill. All rights reserved.
                    </p>
                </div>
            </Footer>
        </Layout>
    )
}
