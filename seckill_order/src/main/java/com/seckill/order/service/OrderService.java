package com.seckill.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.vo.OrderVO;

import java.util.List;

/**
 * 订单服务接口
 *
 * @author seckill
 * @since 1.0.0
 */
public interface OrderService extends IService<SeckillOrder> {

    /**
     * 查询用户订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    List<OrderVO> listByUserId(Long userId);

    /**
     * 查询订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    OrderVO getOrderById(Long orderId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    OrderVO getOrderByOrderNo(Long orderNo);

    /**
     * 检查是否重复下单
     *
     * @param userId  用户ID
     * @param goodsId 商品ID
     * @return true-已下单
     */
    boolean hasOrder(Long userId, Long goodsId);

    /**
     * 根据订单号查询订单实体
     *
     * @param orderNo 订单号
     * @return 订单实体
     */
    SeckillOrder getByOrderNo(Long orderNo);

    /**
     * 更新订单状态
     *
     * @param orderId   订单ID
     * @param newStatus 新状态
     * @return true-更新成功
     */
    boolean updateOrderStatus(Long orderId, Integer newStatus);

    /**
     * 支付订单
     *
     * @param orderNo 订单号
     * @return true-支付成功
     */
    boolean payOrder(Long orderNo);

    /**
     * 取消订单
     *
     * @param orderNo 订单号
     * @return true-取消成功
     */
    boolean cancelOrder(Long orderNo);
}
