package com.example.demo.chat.dto;

/**
 * 发送消息请求DTO
 */
public class SendMessageRequest {
    
    private Long sessionId;
    private String role;
    private String content;
    
    // getter and setter methods
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
}