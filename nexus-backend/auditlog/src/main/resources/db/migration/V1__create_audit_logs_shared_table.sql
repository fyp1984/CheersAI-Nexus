-- =====================================================
-- CheersAI Nexus 审计日志服务数据库初始化脚本
-- Version: V1
-- 依赖: auth 服务的 V1__create_auth_tables.sql
-- 说明: 审计日志服务依赖 auth 服务创建的 nexus.audit_logs 表
--       此脚本仅用于声明依赖关系，确保 Flyway 执行顺序正确
-- =====================================================

-- 注意: nexus.audit_logs 表已在 auth 模块的 V1__create_auth_tables.sql 中创建
-- 此脚本不创建任何表，仅作为依赖声明使用
-- Flyway 会确保此脚本在 auth/V1 之后执行

-- 验证表是否存在（如果不存在则 Flyway 会报错，确保依赖正确）
SELECT 1 FROM nexus.audit_logs LIMIT 1;
