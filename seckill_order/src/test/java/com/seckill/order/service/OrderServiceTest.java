package com.seckill.order.service;

import com.seckill.common.constant.OrderStatus;
import com.seckill.common.exception.BusinessException;
import com.seckill.common.id.DistributedIdGenerator;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.mapper.OrderMapper;
import com.seckill.order.service.impl.OrderServiceImpl;
import com.seckill.order.vo.OrderVO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 订单服务单元测试
 *
 * @author seckill
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务单元测试")
public class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private DistributedIdGenerator idGenerator;

    @InjectMocks
    private OrderServiceImpl orderService;

    private SeckillOrder testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new SeckillOrder();
        testOrder.setId(1L);
        testOrder.setOrderNo(123456789L);
        testOrder.setUserId(1001L);
        testOrder.setGoodsId(1L);
        testOrder.setGoodsName("测试商品");
        testOrder.setGoodsPrice(new BigDecimal("99.99"));
        testOrder.setGoodsCount(1);
        testOrder.setTotalAmount(new BigDecimal("99.99"));
        testOrder.setStatus(OrderStatus.UNPAID);
        testOrder.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("状态变更 - 待支付到已支付")
    void testUpdateOrderStatus_UnpaidToPaid() {
        // Given - 使用 spy 来模拟 getById
        OrderServiceImpl spyService = spy(orderService);
        SeckillOrder order = new SeckillOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.UNPAID);

        doReturn(order).when(spyService).getById(1L);
        doReturn(true).when(spyService).updateById(any());

        // When
        boolean result = spyService.updateOrderStatus(1L, OrderStatus.PAID);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("状态变更 - 非法状态转换")
    void testUpdateOrderStatus_InvalidTransition() {
        // Given
        OrderServiceImpl spyService = spy(orderService);
        SeckillOrder order = new SeckillOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.CANCELLED); // 已取消

        doReturn(order).when(spyService).getById(1L);

        // When - 尝试从已取消变为已支付
        boolean result = spyService.updateOrderStatus(1L, OrderStatus.PAID);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("状态变更 - 订单不存在")
    void testUpdateOrderStatus_OrderNotFound() {
        // Given
        OrderServiceImpl spyService = spy(orderService);
        doReturn(null).when(spyService).getById(anyLong());

        // When
        boolean result = spyService.updateOrderStatus(999L, OrderStatus.PAID);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("检查用户是否有订单")
    void testHasOrder() {
        // Given
        when(orderMapper.selectByUserAndGoods(1001L, 1L)).thenReturn(testOrder);

        // When
        boolean result = orderService.hasOrder(1001L, 1L);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("用户无订单")
    void testHasOrder_NotFound() {
        // Given
        when(orderMapper.selectByUserAndGoods(1001L, 1L)).thenReturn(null);

        // When
        boolean result = orderService.hasOrder(1001L, 1L);

        // Then
        assertFalse(result);
    }
}
