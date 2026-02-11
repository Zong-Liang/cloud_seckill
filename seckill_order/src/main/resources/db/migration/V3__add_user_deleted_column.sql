-- ============================================================================
-- Flyway Migration: V3__add_user_deleted_column.sql
-- 描述: 给 seckill_user 表添加 deleted 软删除字段
-- 作者: seckill
-- 时间: 2026-02-11
-- ============================================================================

-- seckill_user 表添加软删除字段（实体类 SeckillUser 已定义 @TableLogic deleted 字段）
ALTER TABLE seckill_user ADD COLUMN `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除' AFTER `update_time`;
