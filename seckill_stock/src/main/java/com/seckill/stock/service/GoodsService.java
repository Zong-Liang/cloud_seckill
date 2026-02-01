package com.seckill.stock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.stock.entity.SeckillGoods;
import com.seckill.stock.vo.GoodsVO;

import java.util.List;

/**
 * 商品服务接口
 *
 * @author seckill
 * @since 1.0.0
 */
public interface GoodsService extends IService<SeckillGoods> {

    /**
     * 获取进行中的秒杀商品列表
     *
     * @return 商品列表
     */
    List<GoodsVO> listOngoingGoods();

    /**
     * 获取所有秒杀商品列表
     *
     * @return 商品列表
     */
    List<GoodsVO> listAllGoods();

    /**
     * 根据ID获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    GoodsVO getGoodsById(Long id);

    /**
     * 扣减库存（乐观锁）
     *
     * @param goodsId 商品ID
     * @param count   扣减数量
     * @return true-扣减成功
     */
    boolean deductStock(Long goodsId, Integer count);

    /**
     * 回滚库存
     *
     * @param goodsId 商品ID
     * @param count   回滚数量
     * @return true-回滚成功
     */
    boolean rollbackStock(Long goodsId, Integer count);

    /**
     * 检查商品是否可以秒杀
     *
     * @param goodsId 商品ID
     * @return 商品信息（校验通过）
     */
    SeckillGoods checkSeckillable(Long goodsId);

    /**
     * 扣减 MySQL 库存（不使用乐观锁，直接扣减）
     * <p>
     * 用于 Redis 扣减成功后同步到 MySQL
     * </p>
     *
     * @param goodsId 商品ID
     * @param count   扣减数量
     * @return true-扣减成功
     */
    boolean deductMySQLStock(Long goodsId, Integer count);

    /**
     * 获取 Redis 中的缓存库存
     *
     * @param goodsId 商品ID
     * @return 库存数量，null 表示未缓存
     */
    Long getRedisStock(Long goodsId);
}
