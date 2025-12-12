package com.example.demo.chat.service;

import com.example.demo.chat.entity.ChatSession;
import com.example.demo.chat.mapper.ChatSessionMapper;
import com.example.demo.chat.service.ChatMessageService;
import com.example.demo.common.BusinessException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 聊天会话服务类
 */
@Service
public class ChatSessionService {

    @Autowired
    private ChatSessionMapper chatSessionMapper;
    
    @Autowired
    @Lazy
    private ChatMessageService chatMessageService;

    /**
     * 创建新会话
     */
    public ChatSession createSession(Long userId, String title) {
        // 如果标题为空，生成默认标题
        if (title == null || title.trim().isEmpty()) {
            title = "新会话 " + UUID.randomUUID().toString().substring(0, 8);
        }
        
        ChatSession chatSession = new ChatSession();
        chatSession.setUserId(userId);
        chatSession.setTitle(title);
        chatSession.setIsDeleted(0); // 0表示未删除
        
        // 保存到数据库
        int result = chatSessionMapper.insert(chatSession);
        if (result != 1) {
            throw new BusinessException(500, "创建会话失败");
        }
        
        return chatSession;
    }

    /**
     * 根据ID获取会话
     */
    public ChatSession getSessionById(Long id, Long userId) {
        ChatSession chatSession = chatSessionMapper.selectById(id);
        if (chatSession == null) {
            throw new BusinessException(404, "会话不存在");
        }
        
        // 验证会话所属权
        if (!chatSession.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权访问此会话");
        }
        
        return chatSession;
    }

    /**
     * 获取用户的所有会话
     */
    public List<ChatSession> getUserSessions(Long userId) {
        return chatSessionMapper.selectByUserId(userId);
    }

    /**
     * 更新会话标题
     */
    public ChatSession updateSessionTitle(Long id, Long userId, String title) {
        // 验证会话存在且属于当前用户
        ChatSession chatSession = getSessionById(id, userId);
        
        // 更新标题
        int result = chatSessionMapper.updateTitle(id, title);
        if (result != 1) {
            throw new BusinessException(500, "更新会话标题失败");
        }
        
        // 返回更新后的会话
        chatSession.setTitle(title);
        return chatSession;
    }

    /**
     * 删除会话（软删除）
     */
    @Transactional
    public void deleteSession(Long id, Long userId) {
        // 验证会话存在且属于当前用户
        ChatSession chatSession = getSessionById(id, userId);
        
        // 先删除相关的所有消息
        chatMessageService.deleteSessionMessages(id, userId);
        
        int result = chatSessionMapper.deleteById(id);
        if (result != 1) {
            throw new BusinessException(500, "删除会话失败");
        }
    }

    /**
     * 批量删除会话（软删除）
     */
    @Transactional
    public void batchDeleteSessions(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        // 验证每个会话的所有权并删除相关消息
        for (Long id : ids) {
            getSessionById(id, userId);
            chatMessageService.deleteSessionMessages(id, userId);
        }
        
        int result = chatSessionMapper.deleteBatchByIds(ids, userId);
        if (result != ids.size()) {
            throw new BusinessException(500, "批量删除会话失败");
        }
    }

    /**
     * 统计用户会话数量
     */
    public Long countUserSessions(Long userId) {
        return chatSessionMapper.countByUserId(userId);
    }
}