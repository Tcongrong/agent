package com.example.demo.menu;

import com.example.demo.common.ApiResponse;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单控制器
 */
@RestController
@RequestMapping("/api/v1/menu")
public class MenuController {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取用户菜单
     * 根据用户名返回不同的菜单（管理员菜单或普通用户菜单）
     */
    @GetMapping
    public ApiResponse<List<MenuNode>> getMenu(HttpServletRequest request) {
        try {
            // 从请求头获取token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ApiResponse.fail(401, "未提供有效的认证token");
            }

            String token = authHeader.substring(7);
            
            // 验证token并获取用户名
            if (!jwtUtil.validateToken(token)) {
                return ApiResponse.fail(401, "token无效或已过期");
            }

            String username = jwtUtil.getUsernameFromToken(token);
            
            // 根据用户名返回不同的菜单
            List<MenuNode> menu = new ArrayList<>();
            
            if ("admin".equals(username)) {
                // 管理员菜单
                menu = buildAdminMenu();
            } else {
                // 普通用户菜单
                menu = buildUserMenu();
            }

            return ApiResponse.ok(menu);
        } catch (Exception e) {
            return ApiResponse.fail(500, "获取菜单失败: " + e.getMessage());
        }
    }

    /**
     * 构建管理员菜单
     */
    private List<MenuNode> buildAdminMenu() {
        List<MenuNode> menu = new ArrayList<>();
        
        // 仪表盘
        MenuNode dashboard = new MenuNode("dashboard", "仪表盘", "/dashboard", "icon-dashboard", null);
        menu.add(dashboard);
        
        // 系统管理
        MenuNode system = new MenuNode("system", "系统管理", null, "icon-settings", new ArrayList<>());
        system.getChildren().add(new MenuNode("users", "用户管理", "/system/users", "icon-user", null));
        system.getChildren().add(new MenuNode("roles", "角色管理", "/system/roles", "icon-role", null));
        system.getChildren().add(new MenuNode("permissions", "权限管理", "/system/permissions", "icon-permission", null));
        system.getChildren().add(new MenuNode("logs", "系统日志", "/system/logs", "icon-log", null));
        menu.add(system);
        
        // 内容管理
        MenuNode content = new MenuNode("content", "内容管理", null, "icon-content", new ArrayList<>());
        content.getChildren().add(new MenuNode("articles", "文章管理", "/content/articles", "icon-article", null));
        content.getChildren().add(new MenuNode("categories", "分类管理", "/content/categories", "icon-category", null));
        menu.add(content);
        
        // 数据统计
        MenuNode statistics = new MenuNode("statistics", "数据统计", "/statistics", "icon-statistics", null);
        menu.add(statistics);
        
        return menu;
    }

    /**
     * 构建普通用户菜单
     */
    private List<MenuNode> buildUserMenu() {
        List<MenuNode> menu = new ArrayList<>();
        
        // 仪表盘
        MenuNode dashboard = new MenuNode("dashboard", "个人中心", "/user/profile", "icon-user", null);
        menu.add(dashboard);
        
        // 我的内容
        MenuNode myContent = new MenuNode("myContent", "我的内容", null, "icon-content", new ArrayList<>());
        myContent.getChildren().add(new MenuNode("myArticles", "我的文章", "/user/articles", "icon-article", null));
        myContent.getChildren().add(new MenuNode("myComments", "我的评论", "/user/comments", "icon-comment", null));
        menu.add(myContent);
        
        // 设置
        MenuNode settings = new MenuNode("settings", "账号设置", "/user/settings", "icon-settings", null);
        menu.add(settings);
        
        return menu;
    }

    /**
     * 菜单节点数据结构
     */
    public static class MenuNode {
        private String key;
        private String title;
        private String path;
        private String icon;
        private List<MenuNode> children;

        public MenuNode(String key, String title, String path, String icon, List<MenuNode> children) {
            this.key = key;
            this.title = title;
            this.path = path;
            this.icon = icon;
            this.children = children;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public List<MenuNode> getChildren() {
            return children;
        }

        public void setChildren(List<MenuNode> children) {
            this.children = children;
        }
    }
    
    /**
     * 获取仪表盘数据
     * 仅管理员可访问，返回仪表盘的一系列数据
     */
    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboardData(HttpServletRequest request) {
        try {
            // 从请求头获取token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ApiResponse.fail(401, "未提供有效的认证token");
            }

            String token = authHeader.substring(7);
            
            // 验证token并获取用户名
            if (!jwtUtil.validateToken(token)) {
                return ApiResponse.fail(401, "token无效或已过期");
            }

            String username = jwtUtil.getUsernameFromToken(token);
            
            // 仅管理员可访问
            if (!"admin".equals(username)) {
                return ApiResponse.fail(403, "没有权限访问仪表盘数据");
            }

            // 构建仪表盘数据
            Map<String, Object> dashboardData = new HashMap<>();
            
            // 系统概览数据
            Map<String, Object> systemOverview = new HashMap<>();
            systemOverview.put("totalUsers", 128);
            systemOverview.put("activeUsers", 86);
            systemOverview.put("totalArticles", 564);
            systemOverview.put("totalCategories", 28);
            dashboardData.put("systemOverview", systemOverview);
            
            // 近期活动统计
            List<Map<String, Object>> recentActivities = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("date", "2024-01-" + (20 + i));
                activity.put("newUsers", 15 + i * 2);
                activity.put("newArticles", 30 + i * 5);
                recentActivities.add(activity);
            }
            dashboardData.put("recentActivities", recentActivities);
            
            // 内容分类统计
            Map<String, Integer> contentDistribution = new HashMap<>();
            contentDistribution.put("技术", 234);
            contentDistribution.put("生活", 156);
            contentDistribution.put("学习", 98);
            contentDistribution.put("工作", 76);
            dashboardData.put("contentDistribution", contentDistribution);
            
            // 系统状态
            Map<String, Object> systemStatus = new HashMap<>();
            systemStatus.put("serverTime", System.currentTimeMillis());
            systemStatus.put("systemVersion", "v1.0.0");
            systemStatus.put("databaseStatus", "normal");
            dashboardData.put("systemStatus", systemStatus);

            return ApiResponse.ok(dashboardData);
        } catch (Exception e) {
            return ApiResponse.fail(500, "获取仪表盘数据失败: " + e.getMessage());
        }
    }
}