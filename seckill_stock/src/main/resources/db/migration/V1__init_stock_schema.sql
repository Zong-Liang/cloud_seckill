-- ============================================================================
-- Flyway Migration: V1__init_stock_schema.sql
-- 描述: 初始化库存服务数据库表结构
-- 作者: seckill
-- 时间: 2026-02-01
-- ============================================================================

-- 秒杀商品表
CREATE TABLE IF NOT EXISTS `seckill_goods` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `goods_name` VARCHAR(255) NOT NULL COMMENT '商品名称',
    `goods_title` VARCHAR(500) DEFAULT NULL COMMENT '商品标题',
    `goods_img` VARCHAR(500) DEFAULT NULL COMMENT '商品图片',
    `goods_detail` TEXT COMMENT '商品详情',
    `goods_price` DECIMAL(10, 2) NOT NULL COMMENT '商品原价',
    `seckill_price` DECIMAL(10, 2) NOT NULL COMMENT '秒杀价格',
    `stock_count` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    `start_time` DATETIME NOT NULL COMMENT '秒杀开始时间',
    `end_time` DATETIME NOT NULL COMMENT '秒杀结束时间',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态: 0-未开始, 1-进行中, 2-已结束, 3-已下架',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀商品表';

-- 库存流水表（用于追踪库存变化）
CREATE TABLE IF NOT EXISTS `stock_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `goods_id` BIGINT NOT NULL COMMENT '商品ID',
    `order_no` BIGINT DEFAULT NULL COMMENT '订单号',
    `change_type` INT NOT NULL COMMENT '变更类型: 1-扣减, 2-回滚, 3-同步',
    `change_count` INT NOT NULL COMMENT '变更数量',
    `before_count` INT DEFAULT NULL COMMENT '变更前数量',
    `after_count` INT DEFAULT NULL COMMENT '变更后数量',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_goods_id` (`goods_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存变更流水表';

-- 插入测试商品
INSERT INTO `seckill_goods` (`id`, `goods_name`, `goods_title`, `goods_img`, `goods_detail`, `goods_price`, `seckill_price`, `stock_count`, `start_time`, `end_time`, `status`) VALUES
(1, 'iPhone 15 Pro Max 256GB', '苹果最新旗舰手机 A17 Pro芯片 钛金属边框', 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=400&h=400&fit=crop', '<p>苹果最新旗舰手机，A17 Pro芯片，钛金属边框，48MP主摄像头</p>', 9999.00, 8888.00, 100, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1),
(2, 'MacBook Pro 14寸 M3', 'M3芯片 性能强劲 Retina显示屏', 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mbp14-m3-max-pro-spaceblack-select-202310?wid=400&hei=400', '<p>M3芯片，Liquid Retina XDR显示屏，性能强劲</p>', 14999.00, 12999.00, 50, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1),
(3, 'AirPods Pro 2', '主动降噪 音质出众 无线充电', 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MQD83?wid=400&hei=400', '<p>主动降噪，自适应透明模式，个性化空间音频</p>', 1999.00, 1499.00, 200, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1),
(4, '小米14 Ultra 16GB+512GB', '徕卡光学镜头 骁龙8 Gen3芯片', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop', '<p>徕卡光学专业影像系统，骁龙8 Gen3，小米金刚龙晶玻璃</p>', 6499.00, 5999.00, 150, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1),
(5, 'Sony WH-1000XM5', '降噪旗舰 30小时续航 Hi-Res音质', 'https://www.sony.com/image/5d02da5df552836db894cead8a68f5f3?fmt=pjpeg&wid=400&hei=400', '<p>业界领先降噪，30小时续航，佩戴舒适</p>', 2999.00, 2299.00, 80, DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 8 DAY), 0)
ON DUPLICATE KEY UPDATE `update_time` = NOW();
