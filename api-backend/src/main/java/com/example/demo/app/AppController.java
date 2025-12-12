package com.example.demo.app;


import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.Map;

@RestController
@RequestMapping("/api/apps")
public class AppController {

    @GetMapping
    public Map<String, Object> list() {
        // 直接返回简单的Map，避免使用ApiResponse和AppVO
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        List<Map<String, Object>> apps = new ArrayList<>();
        Map<String, Object> app1 = new HashMap<>();
        app1.put("id", 1L);
        app1.put("name", "示例应用1");
        app1.put("description", "这是一个示例应用");
        apps.add(app1);
        
        response.put("data", apps);
        return response;
    }

    @GetMapping("/category/{category}")
    public Map<String, Object> listByCategory(@PathVariable String category) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        List<Map<String, Object>> apps = new ArrayList<>();
        Map<String, Object> app = new HashMap<>();
        app.put("id", 1L);
        app.put("name", "分类应用示例");
        app.put("category", category);
        apps.add(app);
        
        response.put("data", apps);
        return response;
    }
}