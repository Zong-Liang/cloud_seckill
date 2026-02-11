import axios, {
  AxiosInstance,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from "axios";
import { message } from "antd";
import type { ApiResponse } from "@/types";

// 创建 Axios 实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  timeout: 15000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从 localStorage 获取 Token
    const token = localStorage.getItem("token");
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error("请求拦截器错误:", error);
    return Promise.reject(error);
  },
);

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, message: msg, data } = response.data;

    // 业务成功
    if (code === 200) {
      return response.data as ApiResponse;
    }

    // 业务错误处理
    handleBusinessError(code, msg);
    return Promise.reject(response.data);
  },
  (error) => {
    // HTTP 错误处理
    if (error.response) {
      const { status, data } = error.response;

      switch (status) {
        case 401:
          message.error("登录已过期，请重新登录");
          localStorage.removeItem("token");
          localStorage.removeItem("user");
          window.location.href = "/login";
          break;
        case 403:
          message.error("没有权限访问");
          break;
        case 404:
          message.error("请求的资源不存在");
          break;
        case 429:
          message.warning("请求过于频繁，请稍后重试");
          break;
        case 500:
          message.error(data?.message || "服务器内部错误");
          break;
        default:
          message.error(data?.message || "网络错误，请稍后重试");
      }
    } else if (error.code === "ECONNABORTED") {
      message.error("请求超时，请稍后重试");
    } else {
      message.error("网络连接失败，请检查网络");
    }

    return Promise.reject(error);
  },
);

/**
 * 业务错误码处理
 * 注意：错误码必须与后端 ResultCode 枚举严格一致
 */
function handleBusinessError(code: number, msg: string) {
  switch (code) {
    case 401:
      message.error("请先登录");
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      setTimeout(() => {
        window.location.href = "/login";
      }, 1000);
      break;

    // ========== 库存相关 1001-1099 ==========
    case 1001: // GOODS_NOT_EXIST
      message.error("商品不存在");
      break;
    case 1002: // STOCK_NOT_ENOUGH
      message.warning("商品已售罄");
      break;
    case 1003: // ACTIVITY_NOT_STARTED
      message.warning("秒杀活动尚未开始");
      break;
    case 1004: // ACTIVITY_ENDED
      message.warning("秒杀活动已结束");
      break;
    case 1005: // GOODS_OFF_SHELF
      message.warning("商品已下架");
      break;

    // ========== 订单相关 1101-1199 ==========
    case 1101: // ORDER_NOT_EXIST
      message.error("订单不存在");
      break;
    case 1102: // ORDER_ALREADY_PAID
      message.warning("订单已支付");
      break;
    case 1103: // ORDER_CANCELLED
      message.warning("订单已取消");
      break;
    case 1104: // REPEAT_ORDER
      message.warning("您已参与过该商品秒杀");
      break;
    case 1105: // ORDER_TIMEOUT
      message.warning("订单已超时");
      break;
    case 1106: // ORDER_STATUS_ERROR
      message.error("订单状态异常");
      break;

    // ========== 限流相关 1201-1299 ==========
    case 1201: // RATE_LIMIT
      message.warning("访问过于频繁，请稍后重试");
      break;
    case 1202: // SYSTEM_BUSY
      message.warning("系统繁忙，请稍后重试");
      break;
    case 1203: // SERVICE_DEGRADED
      message.warning("服务暂时不可用，请稍后重试");
      break;

    default:
      message.error(msg || "操作失败");
  }
}

export default request;
