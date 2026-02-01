-- ============================================================================
-- Flyway Migration: V1__init_order_schema.sql
-- 描述: 初始化订单服务数据库表结构
-- 作者: seckill
-- 时间: 2026-02-01
-- ============================================================================

-- 订单表
CREATE TABLE IF NOT EXISTS `seckill_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` BIGINT NOT NULL COMMENT '订单编号(雪花算法)',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `goods_id` BIGINT NOT NULL COMMENT '商品ID',
    `goods_name` VARCHAR(255) NOT NULL COMMENT '商品名称(快照)',
    `goods_img` VARCHAR(500) DEFAULT NULL COMMENT '商品图片(快照)',
    `goods_price` DECIMAL(10, 2) NOT NULL COMMENT '秒杀价格(快照)',
    `goods_count` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    `channel` INT DEFAULT 1 COMMENT '下单渠道: 1-PC, 2-Android, 3-iOS, 4-小程序',
    `status` INT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待支付, 1-已支付, 2-已发货, 3-已收货, 4-已取消, 5-已超时',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_goods_id` (`goods_id`),
    KEY `idx_user_goods` (`user_id`, `goods_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀订单表';

-- 用户表
CREATE TABLE IF NOT EXISTS `seckill_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(100) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `login_count` INT NOT NULL DEFAULT 0 COMMENT '登录次数',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀用户表';

-- 插入测试用户 (密码都是 123456 的 BCrypt 哈希)
INSERT INTO `seckill_user` (`id`, `username`, `password`, `nickname`, `phone`, `status`, `login_count`) VALUES
(1, 'admin', '$2a$10$2vpxXYWEViWmM4zwCUrthueo7i1tfu6EO3LkbqUo.zMFjOKJPEeLS', '管理员', '13800138000', 1, 0),
(2, 'test', '$2a$10$2vpxXYWEViWmM4zwCUrthueo7i1tfu6EO3LkbqUo.zMFjOKJPEeLS', '测试用户', '13800138001', 1, 0),
(3, 'user1', '$2a$10$2vpxXYWEViWmM4zwCUrthueo7i1tfu6EO3LkbqUo.zMFjOKJPEeLS', '用户1', '13800138002', 1, 0)
ON DUPLICATE KEY UPDATE `update_time` = NOW();
