-- 聊天功能数据库迁移脚本
-- 版本: V5
-- 描述: 创建聊天会话和消息相关的表结构

-- 心理健康Agent平台数据库初始化脚本
-- 数据库: health_agent

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库（指定字符集）
CREATE DATABASE IF NOT EXISTS health_agent_db 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE health_agent_db;

-- 首先禁用外键检查，以避免创建表的顺序问题
SET FOREIGN_KEY_CHECKS = 0;

-- 确保角色表存在
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    code VARCHAR(100) NOT NULL COMMENT '角色代码',
    display_name VARCHAR(100) NOT NULL COMMENT '显示名称',
    description TEXT COMMENT '角色描述',
    level INT DEFAULT 0 COMMENT '角色级别',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',  
    is_system BOOLEAN DEFAULT FALSE COMMENT '是否系统角色',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '删除时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 确保权限表存在
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    code VARCHAR(100) NOT NULL COMMENT '权限代码',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '权限名称',
    display_name VARCHAR(100) NOT NULL COMMENT '显示名称',
    description TEXT COMMENT '权限描述',
    module VARCHAR(50) NOT NULL COMMENT '权限模块',
    category VARCHAR(50) NOT NULL COMMENT '类别',
    action VARCHAR(20) NOT NULL COMMENT '操作类型',
    resource VARCHAR(100) COMMENT '资源标识',
    is_system BOOLEAN DEFAULT FALSE COMMENT '是否系统权限',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    bio VARCHAR(255) COMMENT '个人简介',
    status TINYINT DEFAULT 1 COMMENT '用户状态：1-正常，0-禁用',
    email_verified TINYINT DEFAULT 0 COMMENT '邮箱是否验证：1-已验证，0-未验证',
    phone_verified TINYINT DEFAULT 0 COMMENT '手机号是否验证：1-已验证，0-未验证',
    login_attempts INT DEFAULT 0 COMMENT '登录尝试次数',
    locked_until DATETIME COMMENT '锁定截止时间',
    last_login_at DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '删除时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 确保用户角色关联表存在（不使用外键约束以避免问题）
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    assigned_by BIGINT COMMENT '分配者用户ID',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 确保角色权限关联表存在（不使用外键约束以避免问题）
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    granted_by BIGINT COMMENT '授权者ID',
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 创建聊天会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '会话标题',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '删除标记(0:未删除, 1:已删除)',
    
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at),
    INDEX idx_is_deleted (is_deleted),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 创建聊天消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) NOT NULL COMMENT '消息角色(user, assistant, system)',
    content TEXT NOT NULL COMMENT '消息内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '删除标记(0:未删除, 1:已删除)',
    
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role (role),
    INDEX idx_created_at (created_at),
    INDEX idx_is_deleted (is_deleted),
    
    FOREIGN KEY (session_id) REFERENCES chat_session(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 插入基础角色数据
INSERT INTO roles (name, code, display_name, description, level, status, is_system) VALUES
('SUPER_ADMIN', 'SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 100, 1, TRUE),
('ADMIN', 'ADMIN', '管理员', '系统管理员，拥有大部分管理权限', 50, 1, TRUE),
('USER_MANAGER', 'USER_MANAGER', '用户管理员', '负责用户管理的管理员', 30, 1, FALSE),
('USER', 'USER', '普通用户', '系统普通用户', 10, 1, TRUE),
('GUEST', 'GUEST', '访客', '系统访客用户', 0, 1, TRUE)
ON DUPLICATE KEY UPDATE 
    code = VALUES(code),
    display_name = VALUES(display_name),
    description = VALUES(description),
    level = VALUES(level),
    status = VALUES(status),
    is_system = VALUES(is_system),
    updated_at = CURRENT_TIMESTAMP;

-- 插入基础权限数据
INSERT INTO permissions (name, code, display_name, description, resource, action, module, category, is_system) VALUES
-- 用户管理权限
('user:list', 'USER:LIST', '用户列表', '查看用户列表', 'user', 'list', 'USER_MANAGEMENT', 'USER', TRUE),
('user:view', 'USER:VIEW', '查看用户', '查看用户详细信息', 'user', 'view', 'USER_MANAGEMENT', 'USER', TRUE),
('user:create', 'USER:CREATE', '创建用户', '创建新用户', 'user', 'create', 'USER_MANAGEMENT', 'USER', TRUE),
('user:update', 'USER:UPDATE', '更新用户', '更新用户信息', 'user', 'update', 'USER_MANAGEMENT', 'USER', TRUE),
('user:delete', 'USER:DELETE', '删除用户', '删除用户', 'user', 'delete', 'USER_MANAGEMENT', 'USER', TRUE),
('user:reset_password', 'USER:RESET_PASSWORD', '重置密码', '重置用户密码', 'user', 'reset_password', 'USER_MANAGEMENT', 'USER', TRUE),
('user:assign_role', 'USER:ASSIGN_ROLE', '分配角色', '为用户分配角色', 'user', 'assign_role', 'USER_MANAGEMENT', 'USER', TRUE),
('user:remove_role', 'USER:REMOVE_ROLE', '移除角色', '移除用户角色', 'user', 'remove_role', 'USER_MANAGEMENT', 'USER', TRUE),

-- 用户个人权限
('user:profile:view', 'USER:PROFILE:VIEW', '查看个人信息', '查看个人用户信息', 'profile', 'view', 'USER_PROFILE', 'USER', TRUE),
('user:profile:update', 'USER:PROFILE:UPDATE', '更新个人信息', '更新个人用户信息', 'profile', 'update', 'USER_PROFILE', 'USER', TRUE),
('user:password:change', 'USER:PASSWORD:CHANGE', '修改密码', '修改个人密码', 'password', 'change', 'USER_PROFILE', 'USER', TRUE),

-- 角色管理权限
('role:list', 'ROLE:LIST', '角色列表', '查看角色列表', 'role', 'list', 'ROLE_MANAGEMENT', 'ROLE', TRUE),
('role:view', 'ROLE:VIEW', '查看角色', '查看角色详细信息', 'role', 'view', 'ROLE_MANAGEMENT', 'ROLE', TRUE),
('role:create', 'ROLE:CREATE', '创建角色', '创建新角色', 'role', 'create', 'ROLE_MANAGEMENT', 'ROLE', TRUE),
('role:update', 'ROLE:UPDATE', '更新角色', '更新角色信息', 'role', 'update', 'ROLE_MANAGEMENT', 'ROLE', TRUE),
('role:delete', 'ROLE:DELETE', '删除角色', '删除角色', 'role', 'delete', 'ROLE_MANAGEMENT', 'ROLE', TRUE),
('role:assign_permission', 'ROLE:ASSIGN_PERMISSION', '分配权限', '为角色分配权限', 'role', 'assign_permission', 'ROLE_MANAGEMENT', 'ROLE', TRUE),
('role:remove_permission', 'ROLE:REMOVE_PERMISSION', '移除权限', '移除角色权限', 'role', 'remove_permission', 'ROLE_MANAGEMENT', 'ROLE', TRUE),

-- 系统管理权限
('system:stats:view', 'SYSTEM:STATS:VIEW', '查看系统统计', '查看系统统计信息', 'system', 'stats_view', 'SYSTEM_MANAGEMENT', 'SYSTEM', TRUE)
ON DUPLICATE KEY UPDATE 
    code = VALUES(code),
    display_name = VALUES(display_name),
    description = VALUES(description),
    updated_at = CURRENT_TIMESTAMP;

-- 为普通用户角色分配基础权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'USER' 
AND p.name IN (
    'user:profile:view',
    'user:profile:update',
    'user:password:change'
)
ON DUPLICATE KEY UPDATE created_at = CURRENT_TIMESTAMP;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 创建默认管理员用户（密码为：admin123，已使用BCrypt加密）
INSERT INTO users (username, email, password_hash, nickname, phone, avatar_url, bio, status, email_verified, phone_verified)
VALUES ('admin', 'admin@example.com', '$2a$10$IELu1pHz8guuV7OB.MPSgOSuKOrkZ0uG72qk1T.GWx0/HJ0f7ijN6', '系统管理员', '13800138000', NULL, '系统管理员账户', 1, TRUE, TRUE)
ON DUPLICATE KEY UPDATE 
    email = VALUES(email),
    nickname = VALUES(nickname),
    phone = VALUES(phone),
    avatar_url = VALUES(avatar_url),
    bio = VALUES(bio),
    status = VALUES(status),
    email_verified = VALUES(email_verified),
    phone_verified = VALUES(phone_verified),
    updated_at = CURRENT_TIMESTAMP;

-- 为管理员用户分配超级管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'SUPER_ADMIN'
ON DUPLICATE KEY UPDATE created_at = CURRENT_TIMESTAMP;




