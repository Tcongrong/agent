package com.example.demo.chat.controller;

import com.example.demo.chat.dto.CreateSessionRequest;
import com.example.demo.chat.dto.UpdateSessionRequest;
import com.example.demo.chat.dto.BatchDeleteSessionsRequest;
import com.example.demo.chat.entity.ChatSession;
import com.example.demo.chat.service.ChatSessionService;
import com.example.demo.common.ApiResponse;
import com.example.demo.common.BusinessException;
import com.example.demo.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天会话控制器
 */
@RestController
@RequestMapping("/api/v1/chat/sessions")
public class ChatSessionController {

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户的会话列表
     */
    @GetMapping
    public ApiResponse<List<ChatSession>> getUserSessions(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<ChatSession> sessions = chatSessionService.getUserSessions(userId);
            return ApiResponse.ok(sessions);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "获取会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建新会话
     */
    @PostMapping
    public ApiResponse<ChatSession> createSession(@Valid @RequestBody CreateSessionRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            ChatSession session = chatSessionService.createSession(userId, request.getTitle());
            return ApiResponse.ok(session);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "创建会话失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个会话详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ChatSession> getSession(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            ChatSession session = chatSessionService.getSessionById(id, userId);
            return ApiResponse.ok(session);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "获取会话详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新会话标题
     */
    @PutMapping("/{id}")
    public ApiResponse<ChatSession> updateSession(@PathVariable Long id, @Valid @RequestBody UpdateSessionRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            ChatSession session = chatSessionService.updateSessionTitle(id, userId, request.getTitle());
            return ApiResponse.ok(session);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "更新会话失败: " + e.getMessage());
        }
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSession(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            chatSessionService.deleteSession(id, userId);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "删除会话失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除会话
     */
    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDeleteSessions(@Valid @RequestBody BatchDeleteSessionsRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            chatSessionService.batchDeleteSessions(request.getIds(), userId);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "批量删除会话失败: " + e.getMessage());
        }
    }

    /**
     * 从请求中获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(401, "未提供有效的认证token");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "token无效或已过期");
        }

        // 从token中获取用户ID
        // 假设JwtUtil中有一个方法可以获取用户ID
        return jwtUtil.getUserIdFromToken(token);
    }
}