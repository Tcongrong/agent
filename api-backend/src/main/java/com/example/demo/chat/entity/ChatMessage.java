package com.example.demo.chat.entity;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
public class ChatMessage {
    private Long id;           // 消息ID
    private Long sessionId;    // 会话ID
    private Long userId;       // 用户ID
    private String role;       // 角色 user/assistant/system
    private String content;    // 消息内容
    private LocalDateTime createdAt;  // 发送时间

    // getter and setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}