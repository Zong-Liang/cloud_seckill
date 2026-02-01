package com.seckill.stock.service;

/**
 * 库存缓存服务接口
 * <p>
 * 使用 Redis 进行库存预扣减，提高秒杀性能
 * </p>
 *
 * @author seckill
 * @since 1.0.0
 */
public interface StockCacheService {

    /**
     * 初始化商品库存到 Redis
     *
     * @param goodsId    商品ID
     * @param stockCount 库存数量
     */
    void initStock(Long goodsId, Integer stockCount);

    /**
     * 预扣减库存（原子操作）
     *
     * @param goodsId 商品ID
     * @param count   扣减数量
     * @return 扣减后的库存数量，-1表示库存不足
     */
    Long deductStock(Long goodsId, Integer count);

    /**
     * 回滚库存（原子操作）
     *
     * @param goodsId 商品ID
     * @param count   回滚数量
     * @return 回滚后的库存数量
     */
    Long rollbackStock(Long goodsId, Integer count);

    /**
     * 获取当前库存
     *
     * @param goodsId 商品ID
     * @return 库存数量，null表示未初始化
     */
    Long getStock(Long goodsId);

    /**
     * 检查库存是否充足
     *
     * @param goodsId 商品ID
     * @param count   需要数量
     * @return true-充足, false-不足
     */
    boolean hasStock(Long goodsId, Integer count);

    /**
     * 删除库存缓存
     *
     * @param goodsId 商品ID
     */
    void deleteStock(Long goodsId);
}
