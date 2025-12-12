-- 为permissions表添加code字段
-- 版本: V4
-- 描述: 为权限表添加code字段，保持与name字段一致

-- 为permissions表添加code字段
ALTER TABLE permissions
ADD COLUMN code VARCHAR(100) NOT NULL COMMENT '权限代码' AFTER name;

-- 设置唯一索引，确保code字段值唯一
ALTER TABLE permissions
ADD UNIQUE KEY uk_permission_code (code);

-- 更新现有数据，将name字段的值复制到code字段
UPDATE permissions
SET code = name;

-- 添加索引以提高查询性能
ALTER TABLE permissions
ADD INDEX idx_code (code);

-- 说明：
-- 1. code字段用于在代码中标识权限，与实体类中的code字段对应
-- 2. 初始值与name字段保持一致，因为之前系统中name字段实际上承担了code的作用
-- 3. 设置为NOT NULL以确保数据完整性
-- 4. 添加唯一索引防止重复值