import { create } from 'zustand'

interface SeckillRecord {
    goodsId: number
    orderNo: number
    timestamp: number
}

interface SeckillState {
    /** 本次会话的秒杀记录 */
    seckillRecords: SeckillRecord[]
    /** 正在进行秒杀的商品 ID 集合 */
    pendingGoodsIds: number[]

    /** 添加秒杀记录 */
    addSeckillRecord: (goodsId: number, orderNo: number) => void
    /** 检查是否已秒杀过该商品 */
    hasSeckilled: (goodsId: number) => boolean
    /** 设置商品秒杀中状态 */
    setPending: (goodsId: number, pending: boolean) => void
    /** 检查商品是否正在秒杀中 */
    isPending: (goodsId: number) => boolean
    /** 清空记录 */
    clearRecords: () => void
}

export const useSeckillStore = create<SeckillState>((set, get) => ({
    seckillRecords: [],
    pendingGoodsIds: [],

    addSeckillRecord: (goodsId, orderNo) => {
        set((state) => ({
            seckillRecords: [
                ...state.seckillRecords,
                {
                    goodsId,
                    orderNo,
                    timestamp: Date.now()
                },
            ],
        }))
    },

    hasSeckilled: (goodsId) => {
        return get().seckillRecords.some((record) => record.goodsId === goodsId)
    },

    setPending: (goodsId, pending) => {
        set((state) => {
            if (pending) {
                // 添加到 pending 列表
                if (!state.pendingGoodsIds.includes(goodsId)) {
                    return { pendingGoodsIds: [...state.pendingGoodsIds, goodsId] }
                }
            } else {
                // 从 pending 列表移除
                return {
                    pendingGoodsIds: state.pendingGoodsIds.filter((id) => id !== goodsId)
                }
            }
            return state
        })
    },

    isPending: (goodsId) => {
        return get().pendingGoodsIds.includes(goodsId)
    },

    clearRecords: () => {
        set({ seckillRecords: [], pendingGoodsIds: [] })
    },
}))
