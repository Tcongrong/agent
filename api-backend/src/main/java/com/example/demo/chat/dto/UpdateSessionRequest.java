package com.example.demo.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新会话请求DTO
 */
public class UpdateSessionRequest {
    
    @NotBlank(message = "会话标题不能为空")
    @Size(max = 100, message = "会话标题长度不能超过100个字符")
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}