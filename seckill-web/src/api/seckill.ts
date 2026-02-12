import request from "./request";
import type { ApiResponse, SeckillRequest } from "@/types";

/**
 * 执行秒杀
 * @returns 订单号
 */
export function doSeckill(data: SeckillRequest): Promise<ApiResponse<string>> {
  return request.post("/stock/seckill/do", data);
}

/**
 * 检查用户是否已秒杀该商品
 */
export function checkKilled(
  userId: number,
  goodsId: number,
): Promise<ApiResponse<boolean>> {
  return request.get("/stock/seckill/check", {
    params: { userId, goodsId },
  });
}

/**
 * 初始化商品库存到 Redis
 */
export function initStock(goodsId: number): Promise<ApiResponse<void>> {
  return request.post(`/stock/seckill/init/${goodsId}`);
}

/**
 * 初始化所有商品库存
 */
export function initAllStock(): Promise<ApiResponse<void>> {
  return request.post("/stock/seckill/init/all");
}
