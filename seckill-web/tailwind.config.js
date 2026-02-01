/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                primary: {
                    50: '#fff1f0',
                    100: '#ffccc7',
                    200: '#ffa39e',
                    300: '#ff7875',
                    400: '#ff4d4f',
                    500: '#f5222d',
                    600: '#cf1322',
                    700: '#a8071a',
                    800: '#820014',
                    900: '#5c0011',
                },
                seckill: {
                    red: '#ff4d4f',
                    orange: '#fa8c16',
                    gold: '#faad14',
                    green: '#52c41a',
                },
            },
            animation: {
                'pulse-fast': 'pulse 0.8s cubic-bezier(0.4, 0, 0.6, 1) infinite',
                'bounce-light': 'bounce 1s ease-in-out infinite',
            },
        },
    },
    plugins: [],
    // 防止和 Ant Design 样式冲突
    corePlugins: {
        preflight: false,
    },
}
