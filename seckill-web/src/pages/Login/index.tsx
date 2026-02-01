import { useState } from 'react'
import { Form, Input, Button, Card, Typography, message, Divider } from 'antd'
import { UserOutlined, LockOutlined, ThunderboltFilled } from '@ant-design/icons'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { login } from '@/api'
import { useUserStore } from '@/store'
import type { LoginDTO } from '@/types'
import './index.css'

const { Title, Text } = Typography

/**
 * 登录页面
 */
export default function Login() {
    const [loading, setLoading] = useState(false)
    const navigate = useNavigate()
    const location = useLocation()
    const { setUser } = useUserStore()

    // 获取登录后跳转地址
    const from = (location.state as { from?: string })?.from || '/'

    const onFinish = async (values: LoginDTO) => {
        setLoading(true)
        try {
            const result = await login(values)
            console.log('登录响应:', result) // 调试日志

            const { data } = result

            // 存储用户信息和 Token
            if (data && data.token) {
                setUser(data, data.token)
                message.success('登录成功！')
                // 使用 setTimeout 确保状态更新后再跳转
                setTimeout(() => {
                    navigate(from, { replace: true })
                }, 100)
            } else if (data) {
                // 即使没有 token 也尝试存储用户信息
                console.warn('登录成功但没有 token:', data)
                message.success('登录成功！')
                setTimeout(() => {
                    navigate(from, { replace: true })
                }, 100)
            } else {
                console.error('登录响应数据异常:', result)
                message.error('登录失败，请重试')
            }
        } catch (error) {
            console.error('登录失败:', error)
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="login-page min-h-screen flex items-center justify-center bg-gradient-to-br from-red-500 via-orange-500 to-yellow-500 p-4">
            <Card className="login-card w-full max-w-md shadow-2xl" bordered={false}>
                {/* Logo */}
                <div className="text-center mb-8">
                    <ThunderboltFilled className="text-5xl text-red-500 mb-4" />
                    <Title level={2} className="mb-1">
                        欢迎回来
                    </Title>
                    <Text type="secondary">登录 Cloud Seckill 账号</Text>
                </div>

                {/* 登录表单 */}
                <Form
                    name="login"
                    size="large"
                    onFinish={onFinish}
                    autoComplete="off"
                    layout="vertical"
                >
                    <Form.Item
                        name="username"
                        rules={[
                            { required: true, message: '请输入用户名' },
                            { min: 3, message: '用户名至少3个字符' },
                        ]}
                    >
                        <Input
                            prefix={<UserOutlined className="text-gray-400" />}
                            placeholder="用户名"
                        />
                    </Form.Item>

                    <Form.Item
                        name="password"
                        rules={[
                            { required: true, message: '请输入密码' },
                            { min: 6, message: '密码至少6个字符' },
                        ]}
                    >
                        <Input.Password
                            prefix={<LockOutlined className="text-gray-400" />}
                            placeholder="密码"
                        />
                    </Form.Item>

                    <Form.Item className="mb-4">
                        <Button
                            type="primary"
                            htmlType="submit"
                            block
                            danger
                            loading={loading}
                            className="h-12 text-base"
                        >
                            登录
                        </Button>
                    </Form.Item>
                </Form>

                <Divider plain>
                    <Text type="secondary">还没有账号？</Text>
                </Divider>

                <Link to="/register">
                    <Button block size="large" className="h-12">
                        立即注册
                    </Button>
                </Link>

                {/* 测试账号提示 */}
                <div className="mt-6 p-4 bg-gray-50 rounded-lg">
                    <Text type="secondary" className="text-xs">
                        测试账号：user1 / 123456
                    </Text>
                </div>
            </Card>
        </div>
    )
}
