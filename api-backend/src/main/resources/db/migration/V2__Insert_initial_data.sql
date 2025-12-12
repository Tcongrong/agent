-- 用户管理系统初始化数据脚本
-- 版本: V2
-- 描述: 插入基础角色和权限数据

-- 插入基础角色
INSERT INTO roles (name, display_name, description, level, is_system) VALUES
('SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 100, TRUE),
('ADMIN', '管理员', '系统管理员，拥有大部分管理权限', 50, TRUE),
('USER_MANAGER', '用户管理员', '负责用户管理的管理员', 30, FALSE),
('USER', '普通用户', '系统普通用户', 10, TRUE),
('GUEST', '访客', '系统访客用户', 0, TRUE)
ON DUPLICATE KEY UPDATE 
    display_name = VALUES(display_name),
    description = VALUES(description),
    updated_at = CURRENT_TIMESTAMP;

-- 插入用户管理权限
INSERT INTO permissions (name, display_name, description, resource, action, module, category, is_system) VALUES
-- 用户管理权限
('user:list', '用户列表', '查看用户列表', 'user', 'list', 'USER_MANAGEMENT', 'USER', TRUE),
('user:view', '查看用户', '查看用户详细信息', 'user', 'view', 'USER_MANAGEMENT', 'USER', TRUE),
('user:create', '创建用户', '创建新用户', 'user', 'create', 'USER_MANAGEMENT', 'USER', TRUE),
('user:update', '更新用户', '更新用户信息', 'user', 'update', 'USER_MANAGEMENT', 'USER', TRUE),
('user:delete', '删除用户', '删除用户', 'user', 'delete', 'USER_MANAGEMENT', 'USER', TRUE),
('user:reset_password', '重置密码', '重置用户密码', 'user', 'reset_password', 'USER_MANAGEMENT', 'USER', TRUE),
('user:assign_role', '分配角色', '为用户分配角色', 'user', 'assign_role', 'USER_MANAGEMENT', 'USER', TRUE),
('user:remove_role', '移除角色', '移除用户角色', 'user', 'remove_role', 'USER_MANAGEMENT', 'USER', TRUE),

-- 用户个人权限
('user:profile:view', '查看个人信息', '查看个人用户信息', 'profile', 'view', 'USER_PROFILE', 'USER', TRUE),
('user:profile:update', '更新个人信息', '更新个人用户信息', 'profile', 'update', 'USER_PROFILE', 'USER', TRUE),
('user:password:change', '修改密码', '修改个人密码', 'password', 'change', 'USER_PROFILE', 'USER', TRUE),

-- 角色管理权限
('role:list', '角色列表', '查看角色列表', 'role', 'list', 'ROLE_MANAGEMENT', 'ROLE', TRUE),
('role:view', '查看角色', '查看角色详细信息', 'role', 'view', 'ROLE_MANAGEMENT', 'ROLE', TRUE),
('role:create', '创建角色', '创建新角色', 'role', 'create', 'ROLE_MANAGEMENT', 'ROLE', TRUE),
('role:update', '更新角色', '更新角色信息', 'role', 'update', 'ROLE_MANAGEMENT', 'ROLE', TRUE),
('role:delete', '删除角色', '删除角色', 'role', 'delete', 'ROLE_MANAGEMENT', 'ROLE', TRUE),
('role:assign_permission', '分配权限', '为角色分配权限', 'role', 'assign_permission', 'ROLE_MANAGEMENT', 'ROLE', TRUE),
('role:remove_permission', '移除权限', '移除角色权限', 'role', 'remove_permission', 'ROLE_MANAGEMENT', 'ROLE', TRUE),

-- 权限管理权限
('permission:list', '权限列表', '查看权限列表', 'permission', 'list', 'PERMISSION_MANAGEMENT', 'PERMISSION', TRUE),
('permission:view', '查看权限', '查看权限详细信息', 'permission', 'view', 'PERMISSION_MANAGEMENT', 'PERMISSION', TRUE),
('permission:create', '创建权限', '创建新权限', 'permission', 'create', 'PERMISSION_MANAGEMENT', 'PERMISSION', TRUE),
('permission:update', '更新权限', '更新权限信息', 'permission', 'update', 'PERMISSION_MANAGEMENT', 'PERMISSION', TRUE),
('permission:delete', '删除权限', '删除权限', 'permission', 'delete', 'PERMISSION_MANAGEMENT', 'PERMISSION', TRUE),

-- 系统管理权限
('system:stats:view', '查看系统统计', '查看系统统计信息', 'system', 'stats_view', 'SYSTEM_MANAGEMENT', 'SYSTEM', TRUE),
('system:cleanup', '系统清理', '执行系统清理操作', 'system', 'cleanup', 'SYSTEM_MANAGEMENT', 'SYSTEM', TRUE),
('system:permission:check', '检查权限', '检查用户权限', 'system', 'permission_check', 'SYSTEM_MANAGEMENT', 'SYSTEM', TRUE),
('system:role:check', '检查角色', '检查用户角色', 'system', 'role_check', 'SYSTEM_MANAGEMENT', 'SYSTEM', TRUE)

ON DUPLICATE KEY UPDATE 
    display_name = VALUES(display_name),
    description = VALUES(description),
    updated_at = CURRENT_TIMESTAMP;

-- 为普通用户角色分配基础权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'USER' 
AND p.code IN (
    'user:profile:view',
    'user:profile:update',
    'user:password:change'
)
ON DUPLICATE KEY UPDATE created_at = created_at;

-- 为用户管理员分配用户管理权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'USER_MANAGER' 
AND p.code IN (
    'user:list', 'user:view', 'user:create', 'user:update', 'user:delete',
    'user:reset_password', 'user:assign_role', 'user:remove_role',
    'user:profile:view', 'user:profile:update', 'user:password:change'
)
ON DUPLICATE KEY UPDATE created_at = created_at;

-- 为管理员分配大部分权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'ADMIN' 
AND p.code IN (
    -- 用户管理
    'user:list', 'user:view', 'user:create', 'user:update', 'user:delete',
    'user:reset_password', 'user:assign_role', 'user:remove_role',
    'user:profile:view', 'user:profile:update', 'user:password:change',
    -- 角色管理
    'role:list', 'role:view', 'role:create', 'role:update', 'role:delete',
    'role:assign_permission', 'role:remove_permission',
    -- 权限管理（除了删除）
    'permission:list', 'permission:view', 'permission:create', 'permission:update',
    -- 系统管理
    'system:stats:view', 'system:cleanup', 'system:permission:check', 'system:role:check'
)
ON DUPLICATE KEY UPDATE created_at = created_at;

-- 超级管理员拥有所有权限（通过代码逻辑实现，这里不插入具体权限）
-- SUPER_ADMIN 角色在代码中会被特殊处理，拥有所有权限