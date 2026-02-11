-- ============================================================================
-- Flyway Migration: V2__add_unique_constraint_and_soft_delete.sql
-- 描述: 添加订单防重唯一约束 + 软删除字段
-- 作者: seckill
-- 时间: 2026-02-11
-- ============================================================================

-- 1. 删除旧的普通索引，替换为唯一索引（防止同一用户重复秒杀同一商品）
DROP INDEX idx_user_goods ON seckill_order;
ALTER TABLE seckill_order ADD UNIQUE KEY uk_user_goods (`user_id`, `goods_id`);

-- 2. 添加软删除字段
ALTER TABLE seckill_order ADD COLUMN `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除' AFTER `update_time`;

-- 3. 用户表添加角色字段（RBAC 基础）
ALTER TABLE seckill_user ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色: user-普通用户, admin-管理员' AFTER `status`;

-- 4. 更新 admin 用户角色
UPDATE seckill_user SET role = 'admin' WHERE username = 'admin';
