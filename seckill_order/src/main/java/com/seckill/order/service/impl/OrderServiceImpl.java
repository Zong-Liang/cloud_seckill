package com.seckill.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.common.constant.OrderStatus;
import com.seckill.common.exception.BusinessException;
import com.seckill.common.id.DistributedIdGenerator;
import com.seckill.common.result.ResultCode;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.mapper.OrderMapper;
import com.seckill.order.service.OrderService;
import com.seckill.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 * <p>
 * 使用 Java 8 特性重构：
 * </p>
 * <ul>
 * <li>Stream API 处理集合</li>
 * <li>Optional 处理空值</li>
 * <li>Lambda 表达式简化代码</li>
 * <li>方法引用优化可读性</li>
 * </ul>
 *
 * @author seckill
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, SeckillOrder> implements OrderService {

    private final OrderMapper orderMapper;
    private final DistributedIdGenerator idGenerator;

    /**
     * 状态转换规则映射（使用 Java 8 Map.of 风格初始化）
     */
    private static final Map<Integer, Set<Integer>> STATUS_TRANSITIONS = Map.of(
            OrderStatus.UNPAID, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED, OrderStatus.TIMEOUT),
            OrderStatus.PAID, Set.of(OrderStatus.SHIPPED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.RECEIVED));

    /**
     * 状态描述映射
     */
    private static final Map<Integer, String> STATUS_DESCRIPTIONS = Map.of(
            OrderStatus.UNPAID, "待支付",
            OrderStatus.PAID, "已支付",
            OrderStatus.SHIPPED, "已发货",
            OrderStatus.RECEIVED, "已收货",
            OrderStatus.CANCELLED, "已取消",
            OrderStatus.TIMEOUT, "已超时");

    @Override
    public List<OrderVO> listByUserId(Long userId) {
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getUserId, userId)
                .orderByDesc(SeckillOrder::getCreateTime);

        // 使用 Stream API 转换
        return this.list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderVO getOrderById(Long orderId) {
        // 使用 Optional 处理空值
        return Optional.ofNullable(this.getById(orderId))
                .map(this::convertToVO)
                .orElseThrow(() -> new BusinessException(ResultCode.ORDER_NOT_EXIST));
    }

    @Override
    public OrderVO getOrderByOrderNo(Long orderNo) {
        return findByOrderNo(orderNo)
                .map(this::convertToVO)
                .orElseThrow(() -> new BusinessException(ResultCode.ORDER_NOT_EXIST));
    }

    @Override
    public boolean hasOrder(Long userId, Long goodsId) {
        return Optional.ofNullable(orderMapper.selectByUserAndGoods(userId, goodsId))
                .isPresent();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SeckillOrder entity) {
        // 使用 Optional 处理默认值
        Optional.ofNullable(entity.getOrderNo())
                .orElseGet(() -> {
                    Long orderNo = idGenerator.nextId();
                    entity.setOrderNo(orderNo);
                    return orderNo;
                });

        Optional.ofNullable(entity.getStatus())
                .orElseGet(() -> {
                    entity.setStatus(OrderStatus.UNPAID);
                    return OrderStatus.UNPAID;
                });

        boolean result = super.save(entity);

        // 使用 Lambda 简化日志
        Optional.of(result)
                .filter(Boolean::booleanValue)
                .ifPresent(r -> log.info("订单创建成功 - orderNo: {}, userId: {}, goodsId: {}",
                        entity.getOrderNo(), entity.getUserId(), entity.getGoodsId()));

        return result;
    }

    @Override
    public SeckillOrder getByOrderNo(Long orderNo) {
        return findByOrderNo(orderNo).orElse(null);
    }

    /**
     * 内部方法：根据订单号查询（返回 Optional）
     */
    private Optional<SeckillOrder> findByOrderNo(Long orderNo) {
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getOrderNo, orderNo);
        return Optional.ofNullable(this.getOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(Long orderId, Integer newStatus) {
        return Optional.ofNullable(this.getById(orderId))
                .filter(order -> isValidStatusTransition(order.getStatus(), newStatus))
                .map(order -> {
                    SeckillOrder updateOrder = new SeckillOrder();
                    updateOrder.setId(orderId);
                    updateOrder.setStatus(newStatus);

                    boolean success = this.updateById(updateOrder);
                    if (success) {
                        log.info("订单状态更新成功 - orderId: {}, oldStatus: {}, newStatus: {}",
                                orderId, order.getStatus(), newStatus);
                    }
                    return success;
                })
                .orElseGet(() -> {
                    log.warn("订单状态变更无效 - orderId: {}, newStatus: {}", orderId, newStatus);
                    return false;
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long orderNo) {
        SeckillOrder order = findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ResultCode.ORDER_NOT_EXIST));

        // 使用 Predicate 校验状态
        Predicate<SeckillOrder> canPay = o -> o.getStatus() == OrderStatus.UNPAID;

        if (!canPay.test(order)) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "订单状态不正确，无法支付");
        }

        SeckillOrder updateOrder = new SeckillOrder();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatus.PAID);
        updateOrder.setPayTime(LocalDateTime.now());

        boolean success = this.updateById(updateOrder);

        Optional.of(success)
                .filter(Boolean::booleanValue)
                .ifPresent(s -> log.info("订单支付成功 - orderNo: {}, userId: {}", orderNo, order.getUserId()));

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderNo) {
        SeckillOrder order = findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ResultCode.ORDER_NOT_EXIST));

        // 使用 Lambda 校验状态
        Predicate<SeckillOrder> canCancel = o -> o.getStatus() == OrderStatus.UNPAID;

        if (!canCancel.test(order)) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "只能取消待支付的订单");
        }

        SeckillOrder updateOrder = new SeckillOrder();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatus.CANCELLED);

        boolean success = this.updateById(updateOrder);

        Optional.of(success)
                .filter(Boolean::booleanValue)
                .ifPresent(s -> log.info("订单取消成功 - orderNo: {}, userId: {}", orderNo, order.getUserId()));

        return success;
    }

    /**
     * 校验状态变更是否合法（使用 Map 配置化）
     */
    private boolean isValidStatusTransition(Integer currentStatus, Integer newStatus) {
        return Optional.ofNullable(currentStatus)
                .flatMap(current -> Optional.ofNullable(STATUS_TRANSITIONS.get(current)))
                .map(allowedStatuses -> allowedStatuses.contains(newStatus))
                .orElse(false);
    }

    /**
     * 转换为 VO（使用方法引用）
     */
    private OrderVO convertToVO(SeckillOrder order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);

        // 使用 Map.getOrDefault 获取状态描述
        vo.setStatusDesc(STATUS_DESCRIPTIONS.getOrDefault(order.getStatus(), "未知"));

        return vo;
    }
}
