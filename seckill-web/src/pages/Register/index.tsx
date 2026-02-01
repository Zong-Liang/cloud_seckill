import { useState } from 'react'
import { Form, Input, Button, Card, Typography, message, Divider } from 'antd'
import { UserOutlined, LockOutlined, PhoneOutlined, ThunderboltFilled } from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '@/api'
import { useUserStore } from '@/store'
import type { RegisterDTO } from '@/types'
import './index.css'

const { Title, Text } = Typography

/**
 * 注册页面
 */
export default function Register() {
    const [loading, setLoading] = useState(false)
    const navigate = useNavigate()
    const { setUser } = useUserStore()

    const onFinish = async (values: RegisterDTO) => {
        // 检查密码确认
        if (values.password !== values.confirmPassword) {
            message.error('两次输入的密码不一致')
            return
        }

        setLoading(true)
        try {
            const result = await register({
                username: values.username,
                password: values.password,
                nickname: values.nickname,
                phone: values.phone,
            })
            const { data } = result

            // 注册成功后自动登录
            if (data.token) {
                setUser(data, data.token)
                message.success('注册成功！')
                navigate('/', { replace: true })
            } else {
                message.success('注册成功，请登录')
                navigate('/login', { replace: true })
            }
        } catch (error) {
            console.error('注册失败:', error)
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="register-page min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-500 via-purple-500 to-pink-500 p-4">
            <Card className="register-card w-full max-w-md shadow-2xl" bordered={false}>
                {/* Logo */}
                <div className="text-center mb-8">
                    <ThunderboltFilled className="text-5xl text-red-500 mb-4" />
                    <Title level={2} className="mb-1">
                        创建账号
                    </Title>
                    <Text type="secondary">加入 Cloud Seckill</Text>
                </div>

                {/* 注册表单 */}
                <Form
                    name="register"
                    size="large"
                    onFinish={onFinish}
                    autoComplete="off"
                    layout="vertical"
                >
                    <Form.Item
                        name="username"
                        rules={[
                            { required: true, message: '请输入用户名' },
                            { min: 3, max: 20, message: '用户名3-20个字符' },
                            { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字、下划线' },
                        ]}
                    >
                        <Input
                            prefix={<UserOutlined className="text-gray-400" />}
                            placeholder="用户名"
                        />
                    </Form.Item>

                    <Form.Item
                        name="nickname"
                    >
                        <Input
                            prefix={<UserOutlined className="text-gray-400" />}
                            placeholder="昵称（选填）"
                        />
                    </Form.Item>

                    <Form.Item
                        name="phone"
                        rules={[
                            { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' },
                        ]}
                    >
                        <Input
                            prefix={<PhoneOutlined className="text-gray-400" />}
                            placeholder="手机号（选填）"
                        />
                    </Form.Item>

                    <Form.Item
                        name="password"
                        rules={[
                            { required: true, message: '请输入密码' },
                            { min: 6, max: 20, message: '密码6-20个字符' },
                        ]}
                    >
                        <Input.Password
                            prefix={<LockOutlined className="text-gray-400" />}
                            placeholder="密码"
                        />
                    </Form.Item>

                    <Form.Item
                        name="confirmPassword"
                        rules={[
                            { required: true, message: '请确认密码' },
                            ({ getFieldValue }) => ({
                                validator(_, value) {
                                    if (!value || getFieldValue('password') === value) {
                                        return Promise.resolve()
                                    }
                                    return Promise.reject(new Error('两次输入的密码不一致'))
                                },
                            }),
                        ]}
                    >
                        <Input.Password
                            prefix={<LockOutlined className="text-gray-400" />}
                            placeholder="确认密码"
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
                            注册
                        </Button>
                    </Form.Item>
                </Form>

                <Divider plain>
                    <Text type="secondary">已有账号？</Text>
                </Divider>

                <Link to="/login">
                    <Button block size="large" className="h-12">
                        返回登录
                    </Button>
                </Link>
            </Card>
        </div>
    )
}
