package com.example.demo.ai.dto;

/**
 * 聊天消息DTO，包含角色和内容
 */
public class ChatMessageDTO {

    private String role; // 角色，可以是 "user" 或 "assistant"
    private String content; // 消息内容

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