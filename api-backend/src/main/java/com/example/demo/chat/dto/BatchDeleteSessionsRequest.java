package com.example.demo.chat.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量删除会话请求DTO
 */
public class BatchDeleteSessionsRequest {
    
    @NotEmpty(message = "会话ID列表不能为空")
    private List<Long> ids;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}