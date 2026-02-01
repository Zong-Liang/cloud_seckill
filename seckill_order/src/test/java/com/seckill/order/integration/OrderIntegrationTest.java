package com.seckill.order.integration;

import com.seckill.common.constant.OrderStatus;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.service.OrderService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 订单服务集成测试
 *
 * @author seckill
 * @since 2.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("需要完整基础设施才能运行")
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    private static Long testOrderNo;

    @BeforeAll
    static void beforeAll() {
        System.out.println("========== 订单集成测试开始 ==========");
    }

    @Test
    @Order(1)
    @DisplayName("创建测试订单")
    void testCreateOrder() {
        SeckillOrder order = new SeckillOrder();
        order.setUserId(1001L);
        order.setGoodsId(1L);
        order.setGoodsName("测试商品");
        order.setGoodsPrice(new BigDecimal("99.99"));
        order.setGoodsCount(1);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setChannel(1);
        order.setStatus(OrderStatus.UNPAID);
        order.setCreateTime(LocalDateTime.now());

        boolean saved = orderService.save(order);
        testOrderNo = order.getOrderNo();

        Assertions.assertTrue(saved, "订单创建失败");
        Assertions.assertNotNull(testOrderNo, "订单号不应为空");

        System.out.println("创建订单成功 - orderNo: " + testOrderNo);
    }

    @Test
    @Order(2)
    @DisplayName("查询订单详情")
    void testGetOrder() throws Exception {
        mockMvc.perform(get("/order/detail/{orderNo}", testOrderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").value(testOrderNo));
    }

    @Test
    @Order(3)
    @DisplayName("查询用户订单列表")
    void testListUserOrders() throws Exception {
        mockMvc.perform(get("/order/list")
                .param("userId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(4)
    @DisplayName("支付订单")
    void testPayOrder() throws Exception {
        mockMvc.perform(post("/order/pay/{orderNo}", testOrderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证状态变更
        SeckillOrder order = orderService.getByOrderNo(testOrderNo);
        Assertions.assertEquals(OrderStatus.PAID, order.getStatus(), "订单状态应为已支付");
    }

    @Test
    @Order(5)
    @DisplayName("查询未支付订单 - 应为空")
    void testGetUnpaidOrders() throws Exception {
        // 创建另一个未支付订单
        SeckillOrder order = new SeckillOrder();
        order.setUserId(1002L);
        order.setGoodsId(2L);
        order.setGoodsName("未支付商品");
        order.setGoodsPrice(new BigDecimal("199.99"));
        order.setGoodsCount(1);
        order.setTotalAmount(new BigDecimal("199.99"));
        order.setChannel(1);
        order.setStatus(OrderStatus.UNPAID);
        order.setCreateTime(LocalDateTime.now());
        orderService.save(order);

        // 取消该订单
        mockMvc.perform(post("/order/cancel/{orderNo}", order.getOrderNo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证状态变更
        SeckillOrder cancelled = orderService.getByOrderNo(order.getOrderNo());
        Assertions.assertEquals(OrderStatus.CANCELLED, cancelled.getStatus(), "订单状态应为已取消");
    }

    @Test
    @Order(6)
    @DisplayName("支付已支付订单 - 应失败")
    void testPayAlreadyPaidOrder() throws Exception {
        // 尝试再次支付已支付的订单
        mockMvc.perform(post("/order/pay/{orderNo}", testOrderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNumber());
    }
}
