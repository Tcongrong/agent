package com.example.demo.ai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 单轮聊天测试请求DTO
 */
public class TestChatRequest {

    @NotBlank(message = "提示词不能为空")
    private String prompt;

    private String systemMessage;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
    }
}