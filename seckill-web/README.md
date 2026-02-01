# Cloud Seckill 前端

基于 React 18 + Vite + TypeScript + Ant Design + Tailwind CSS 构建的秒杀系统前端。

## 技术栈

- **框架**: React 18
- **构建工具**: Vite 6
- **开发语言**: TypeScript
- **UI 组件库**: Ant Design 5
- **CSS 框架**: Tailwind CSS 3
- **状态管理**: Zustand
- **HTTP 客户端**: Axios
- **路由**: React Router 6

## 项目结构

```
src/
├── api/          # API 接口层
├── components/   # 公共组件
├── hooks/        # 自定义 Hooks
├── layouts/      # 布局组件
├── pages/        # 页面组件
├── router/       # 路由配置
├── store/        # 状态管理
├── styles/       # 全局样式
├── types/        # TypeScript 类型定义
└── utils/        # 工具函数
```

## 开发

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

开发服务器启动后访问: http://localhost:5173

### 构建生产版本

```bash
npm run build
```

## 后端对接

前端通过 Vite 代理连接后端 Gateway（默认 `http://localhost:9000`）。

启动后端服务：

```bash
# 1. 启动基础设施
docker-compose up -d

# 2. 启动后端微服务
cd seckill_gateway && mvn spring-boot:run
cd seckill_order && mvn spring-boot:run
cd seckill_stock && mvn spring-boot:run
```

## 页面功能

| 页面 | 路由 | 功能 |
|------|------|------|
| 首页 | `/` | 展示秒杀商品 |
| 商品列表 | `/goods` | 全部商品浏览 |
| 商品详情 | `/goods/:id` | 商品信息和秒杀 |
| 秒杀结果 | `/seckill/result/:orderNo` | 秒杀成功展示 |
| 订单列表 | `/orders` | 用户订单管理 |
| 订单详情 | `/order/:orderNo` | 订单详情 |
| 支付页面 | `/pay/:orderNo` | 模拟支付 |
| 个人中心 | `/profile` | 用户信息 |
| 登录 | `/login` | 用户登录 |
| 注册 | `/register` | 用户注册 |

## 测试账号

- 用户名: `user1`
- 密码: `123456`
