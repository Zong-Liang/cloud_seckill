import { Link } from 'react-router-dom'
import { Typography, Card, Avatar, Descriptions, Button, Divider, Breadcrumb } from 'antd'
import {
    HomeOutlined,
    UserOutlined,
    PhoneOutlined,
    MailOutlined,
    LogoutOutlined,
    EditOutlined,
} from '@ant-design/icons'
import { useUserStore } from '@/store'
import { formatTime } from '@/utils'
import './index.css'

const { Title, Text } = Typography

/**
 * 个人中心页面
 */
export default function Profile() {
    const { user, logout } = useUserStore()

    if (!user) {
        return null
    }

    return (
        <div className="profile-page max-w-3xl mx-auto">
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
                    { title: '个人中心' },
                ]}
            />

            {/* 用户信息卡片 */}
            <Card className="mb-6">
                <div className="flex flex-col sm:flex-row items-center gap-6">
                    <Avatar
                        size={100}
                        src={user.avatar}
                        icon={!user.avatar && <UserOutlined />}
                        className="bg-gradient-to-r from-red-500 to-orange-500"
                    />
                    <div className="text-center sm:text-left">
                        <Title level={3} className="mb-1">
                            {user.nickname || user.username}
                        </Title>
                        <Text type="secondary">@{user.username}</Text>
                    </div>
                    <div className="sm:ml-auto">
                        <Button icon={<EditOutlined />}>编辑资料</Button>
                    </div>
                </div>
            </Card>

            {/* 账户信息 */}
            <Card title="账户信息" className="mb-6">
                <Descriptions column={1}>
                    <Descriptions.Item label={<><UserOutlined className="mr-2" />用户名</>}>
                        {user.username}
                    </Descriptions.Item>
                    <Descriptions.Item label={<><UserOutlined className="mr-2" />昵称</>}>
                        {user.nickname || '未设置'}
                    </Descriptions.Item>
                    <Descriptions.Item label={<><PhoneOutlined className="mr-2" />手机号</>}>
                        {user.phone || '未绑定'}
                    </Descriptions.Item>
                    <Descriptions.Item label="账户状态">
                        <span className="text-green-500">正常</span>
                    </Descriptions.Item>
                </Descriptions>
            </Card>

            {/* 快捷操作 */}
            <Card title="快捷操作" className="mb-6">
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                    <Link to="/orders">
                        <div className="text-center p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                            <div className="text-2xl mb-2">📦</div>
                            <Text>我的订单</Text>
                        </div>
                    </Link>
                    <Link to="/goods">
                        <div className="text-center p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                            <div className="text-2xl mb-2">🛒</div>
                            <Text>去抢购</Text>
                        </div>
                    </Link>
                    <div className="text-center p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer">
                        <div className="text-2xl mb-2">📍</div>
                        <Text>收货地址</Text>
                    </div>
                    <div className="text-center p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer">
                        <div className="text-2xl mb-2">⚙️</div>
                        <Text>账户设置</Text>
                    </div>
                </div>
            </Card>

            {/* 退出登录 */}
            <div className="text-center">
                <Button
                    danger
                    icon={<LogoutOutlined />}
                    size="large"
                    onClick={() => {
                        logout()
                        window.location.href = '/'
                    }}
                >
                    退出登录
                </Button>
            </div>
        </div>
    )
}
