-- ========================================
-- Cloud Seckill 数据库初始化脚本
-- 包含库存服务和订单服务的表结构
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS seckill_stock DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS seckill_order DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS nacos_config DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ========================================
-- 库存服务数据库
-- ========================================
USE seckill_stock;

-- 秒杀商品表
DROP TABLE IF EXISTS seckill_goods;
CREATE TABLE seckill_goods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    goods_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    goods_title VARCHAR(500) COMMENT '商品标题',
    goods_img VARCHAR(500) COMMENT '商品图片',
    goods_detail TEXT COMMENT '商品详情',
    goods_price DECIMAL(10, 2) NOT NULL COMMENT '商品原价',
    seckill_price DECIMAL(10, 2) NOT NULL COMMENT '秒杀价格',
    stock_count INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    start_time DATETIME NOT NULL COMMENT '秒杀开始时间',
    end_time DATETIME NOT NULL COMMENT '秒杀结束时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-未开始 1-进行中 2-已结束 3-已下架',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- 插入测试数据
INSERT INTO seckill_goods (goods_name, goods_title, goods_img, goods_price, seckill_price, stock_count, start_time, end_time, status) VALUES
('iPhone 15 Pro', 'Apple iPhone 15 Pro 256GB 钛金属原色', '/images/iphone15pro.jpg', 9999.00, 7999.00, 100, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1),
('MacBook Pro 14', 'Apple MacBook Pro 14英寸 M3 Pro芯片', '/images/macbookpro14.jpg', 16999.00, 14999.00, 50, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1),
('AirPods Pro 2', 'Apple AirPods Pro 第二代 MagSafe充电盒', '/images/airpodspro2.jpg', 1899.00, 1499.00, 500, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1),
('iPad Pro 12.9', 'Apple iPad Pro 12.9英寸 M2芯片 WiFi版', '/images/ipadpro.jpg', 10999.00, 8999.00, 80, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1),
('Apple Watch Ultra 2', 'Apple Watch Ultra 2 钛金属表壳', '/images/watchultra2.jpg', 6499.00, 5499.00, 200, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1);

-- ========================================
-- 订单服务数据库
-- ========================================
USE seckill_order;

-- 秒杀订单表
DROP TABLE IF EXISTS seckill_order;
CREATE TABLE seckill_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no BIGINT NOT NULL COMMENT '订单号（雪花ID）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    goods_name VARCHAR(200) COMMENT '商品名称（快照）',
    goods_img VARCHAR(500) COMMENT '商品图片（快照）',
    goods_price DECIMAL(10, 2) COMMENT '商品价格（快照）',
    goods_count INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    total_amount DECIMAL(10, 2) COMMENT '订单总金额',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待支付 1-已支付 2-已发货 3-已收货 4-已取消 5-已超时',
    channel VARCHAR(20) COMMENT '下单渠道',
    pay_time DATETIME COMMENT '支付时间',
    deliver_time DATETIME COMMENT '发货时间',
    receive_time DATETIME COMMENT '收货时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_goods_id (goods_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

-- 用户表（简化版，实际项目应放在独立用户服务）
DROP TABLE IF EXISTS seckill_user;
CREATE TABLE seckill_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname VARCHAR(50) COMMENT '昵称',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_username (username),
    UNIQUE INDEX uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入测试用户（密码: 123456，BCrypt加密）
INSERT INTO seckill_user (username, password, nickname, phone) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKqVSyamHFGK.KhFZL2iA.HZxKy6', '管理员', '13800000000'),
('test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKqVSyamHFGK.KhFZL2iA.HZxKy6', '测试用户', '13800000001');

-- ========================================
-- 授权
-- ========================================
GRANT ALL PRIVILEGES ON seckill_stock.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON seckill_order.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON nacos_config.* TO 'root'@'%';
FLUSH PRIVILEGES;
