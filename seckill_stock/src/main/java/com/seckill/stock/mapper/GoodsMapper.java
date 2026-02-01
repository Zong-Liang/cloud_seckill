package com.seckill.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.stock.entity.SeckillGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品 Mapper
 *
 * @author seckill
 * @since 1.0.0
 */
@Mapper
public interface GoodsMapper extends BaseMapper<SeckillGoods> {

        /**
         * 扣减库存（乐观锁）
         *
         * @param goodsId 商品ID
         * @param count   扣减数量
         * @param version 版本号
         * @return 影响行数
         */
        @Update("UPDATE seckill_goods SET stock_count = stock_count - #{count}, version = version + 1 " +
                        "WHERE id = #{goodsId} AND stock_count >= #{count} AND version = #{version}")
        int deductStock(@Param("goodsId") Long goodsId,
                        @Param("count") Integer count,
                        @Param("version") Integer version);

        /**
         * 回滚库存
         *
         * @param goodsId 商品ID
         * @param count   回滚数量
         * @return 影响行数
         */
        @Update("UPDATE seckill_goods SET stock_count = stock_count + #{count} WHERE id = #{goodsId}")
        int rollbackStock(@Param("goodsId") Long goodsId, @Param("count") Integer count);

        /**
         * 直接扣减库存（不使用乐观锁）
         * <p>
         * 用于 Redis 扣减成功后同步到 MySQL，此时并发已被 Redis 控制
         * </p>
         *
         * @param goodsId 商品ID
         * @param count   扣减数量
         * @return 影响行数
         */
        @Update("UPDATE seckill_goods SET stock_count = stock_count - #{count} " +
                        "WHERE id = #{goodsId} AND stock_count >= #{count}")
        int directDeductStock(@Param("goodsId") Long goodsId, @Param("count") Integer count);
}
