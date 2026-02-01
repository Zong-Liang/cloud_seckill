import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import App from './App'
import './styles/index.css'

// 设置 dayjs 中文
dayjs.locale('zh-cn')

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <ConfigProvider
            locale={zhCN}
            theme={{
                token: {
                    colorPrimary: '#ff4d4f',
                    borderRadius: 8,
                },
            }}
        >
            <App />
        </ConfigProvider>
    </StrictMode>,
)
