package com.example.demo.ai.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 多轮对话测试请求DTO
 */
public class MultiChatRequest {

    private Long sessionId; // 会话ID，可选，如果提供则在现有会话上继续对话
    
    @NotEmpty(message = "消息列表不能为空")
    private List<ChatMessageDTO> messages;

    private String systemMessage;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public List<ChatMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessageDTO> messages) {
        this.messages = messages;
    }

    public String getSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
    }
}