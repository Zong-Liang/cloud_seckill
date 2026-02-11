-- ============================================================================
-- Flyway Migration: V2__add_soft_delete.sql
-- 描述: 商品表和库存流水表添加软删除字段
-- 作者: seckill
-- 时间: 2026-02-11
-- ============================================================================

-- 1. 商品表添加软删除字段
ALTER TABLE seckill_goods ADD COLUMN `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除' AFTER `update_time`;
