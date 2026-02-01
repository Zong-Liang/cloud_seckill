package com.seckill.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.order.entity.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单 Mapper
 *
 * @author seckill
 * @since 1.0.0
 */
@Mapper
public interface OrderMapper extends BaseMapper<SeckillOrder> {

    /**
     * 查询用户对某商品的订单
     *
     * @param userId  用户ID
     * @param goodsId 商品ID
     * @return 订单
     */
    @Select("SELECT * FROM seckill_order WHERE user_id = #{userId} AND goods_id = #{goodsId} LIMIT 1")
    SeckillOrder selectByUserAndGoods(@Param("userId") Long userId, @Param("goodsId") Long goodsId);
}
