import { useState } from 'react'
import { Link } from 'react-router-dom'
import {
    Typography,
    Card,
    Button,
    Modal,
    Form,
    Input,
    Tag,
    Empty,
    Breadcrumb,
    Space,
    Popconfirm,
    message,
} from 'antd'
import {
    HomeOutlined,
    PlusOutlined,
    EditOutlined,
    DeleteOutlined,
    EnvironmentOutlined,
    PhoneOutlined,
    UserOutlined,
    StarOutlined,
} from '@ant-design/icons'
import { useAddressStore } from '@/store/addressStore'
import type { Address, AddressFormValues } from '@/types/address'

const { Title, Text } = Typography

/**
 * 收货地址管理页面
 */
export default function AddressList() {
    const { addresses, addAddress, updateAddress, deleteAddress, setDefault } = useAddressStore()
    const [modalOpen, setModalOpen] = useState(false)
    const [editingAddress, setEditingAddress] = useState<Address | null>(null)
    const [form] = Form.useForm<AddressFormValues>()

    const handleAdd = () => {
        setEditingAddress(null)
        form.resetFields()
        setModalOpen(true)
    }

    const handleEdit = (addr: Address) => {
        setEditingAddress(addr)
        form.setFieldsValue({
            name: addr.name,
            phone: addr.phone,
            province: addr.province,
            city: addr.city,
            district: addr.district,
            detail: addr.detail,
        })
        setModalOpen(true)
    }

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields()
            if (editingAddress) {
                updateAddress(editingAddress.id, values)
                message.success('地址修改成功')
            } else {
                addAddress(values)
                message.success('地址添加成功')
            }
            setModalOpen(false)
            form.resetFields()
        } catch {
            // 表单校验失败
        }
    }

    const handleDelete = (id: string) => {
        deleteAddress(id)
        message.success('地址已删除')
    }

    const handleSetDefault = (id: string) => {
        setDefault(id)
        message.success('已设为默认地址')
    }

    return (
        <div className="address-page max-w-3xl mx-auto">
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
                    {
                        title: <Link to="/profile">个人中心</Link>,
                    },
                    { title: '收货地址' },
                ]}
            />

            {/* 头部 */}
            <div className="flex items-center justify-between mb-6">
                <Title level={3} className="mb-0">
                    <EnvironmentOutlined className="mr-2" />
                    收货地址
                </Title>
                <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={handleAdd}
                >
                    新增地址
                </Button>
            </div>

            {/* 地址列表 */}
            {addresses.length === 0 ? (
                <Card>
                    <Empty
                        description="暂无收货地址"
                        className="py-12"
                    >
                        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                            添加地址
                        </Button>
                    </Empty>
                </Card>
            ) : (
                <div className="space-y-4">
                    {addresses.map((addr) => (
                        <Card
                            key={addr.id}
                            className={`transition-all hover:shadow-md ${addr.isDefault ? 'border-red-300 bg-red-50/30' : ''}`}
                        >
                            <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                                {/* 地址信息 */}
                                <div className="flex-1">
                                    <div className="flex items-center gap-3 mb-2">
                                        <Text strong className="text-base">
                                            <UserOutlined className="mr-1" />
                                            {addr.name}
                                        </Text>
                                        <Text type="secondary">
                                            <PhoneOutlined className="mr-1" />
                                            {addr.phone}
                                        </Text>
                                        {addr.isDefault && (
                                            <Tag color="red" className="ml-1">默认</Tag>
                                        )}
                                    </div>
                                    <Text className="text-gray-600">
                                        <EnvironmentOutlined className="mr-1" />
                                        {addr.province} {addr.city} {addr.district} {addr.detail}
                                    </Text>
                                </div>

                                {/* 操作按钮 */}
                                <Space>
                                    {!addr.isDefault && (
                                        <Button
                                            size="small"
                                            icon={<StarOutlined />}
                                            onClick={() => handleSetDefault(addr.id)}
                                        >
                                            设为默认
                                        </Button>
                                    )}
                                    <Button
                                        size="small"
                                        icon={<EditOutlined />}
                                        onClick={() => handleEdit(addr)}
                                    >
                                        编辑
                                    </Button>
                                    <Popconfirm
                                        title="确认删除该地址？"
                                        onConfirm={() => handleDelete(addr.id)}
                                        okText="确认"
                                        cancelText="取消"
                                    >
                                        <Button size="small" danger icon={<DeleteOutlined />}>
                                            删除
                                        </Button>
                                    </Popconfirm>
                                </Space>
                            </div>
                        </Card>
                    ))}
                </div>
            )}

            {/* 新增/编辑弹窗 */}
            <Modal
                title={editingAddress ? '编辑地址' : '新增地址'}
                open={modalOpen}
                onOk={handleSubmit}
                onCancel={() => {
                    setModalOpen(false)
                    form.resetFields()
                }}
                okText="保存"
                cancelText="取消"
                width={520}
                destroyOnClose
            >
                <Form
                    form={form}
                    layout="vertical"
                    className="mt-4"
                >
                    <div className="grid grid-cols-2 gap-x-4">
                        <Form.Item
                            name="name"
                            label="收件人"
                            rules={[{ required: true, message: '请输入收件人姓名' }]}
                        >
                            <Input placeholder="请输入姓名" prefix={<UserOutlined />} />
                        </Form.Item>
                        <Form.Item
                            name="phone"
                            label="手机号"
                            rules={[
                                { required: true, message: '请输入手机号' },
                                { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' },
                            ]}
                        >
                            <Input placeholder="请输入手机号" prefix={<PhoneOutlined />} />
                        </Form.Item>
                    </div>
                    <div className="grid grid-cols-3 gap-x-4">
                        <Form.Item
                            name="province"
                            label="省份"
                            rules={[{ required: true, message: '请输入省份' }]}
                        >
                            <Input placeholder="如：广东省" />
                        </Form.Item>
                        <Form.Item
                            name="city"
                            label="城市"
                            rules={[{ required: true, message: '请输入城市' }]}
                        >
                            <Input placeholder="如：深圳市" />
                        </Form.Item>
                        <Form.Item
                            name="district"
                            label="区县"
                            rules={[{ required: true, message: '请输入区县' }]}
                        >
                            <Input placeholder="如：南山区" />
                        </Form.Item>
                    </div>
                    <Form.Item
                        name="detail"
                        label="详细地址"
                        rules={[{ required: true, message: '请输入详细地址' }]}
                    >
                        <Input.TextArea
                            placeholder="请输入详细地址（街道、楼栋、门牌号等）"
                            rows={2}
                        />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    )
}
