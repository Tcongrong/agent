package com.example.demo.chat.service;

import com.example.demo.chat.entity.ChatMessage;
import com.example.demo.chat.mapper.ChatMessageMapper;
import com.example.demo.common.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息服务类
 */
@Service
public class ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);

    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * 发送消息
     */
    public ChatMessage sendMessage(Long sessionId, Long userId, String role, String content) {
        logger.info("发送消息请求 - userId: {}, sessionId: {}, role: {}, contentLength: {}", 
                    userId, sessionId, role, content != null ? content.length() : 0);
        
        try {
            // 参数验证
            logger.debug("验证参数是否为空");
            if (sessionId == null) {
                logger.warn("会话ID为空");
                throw new BusinessException(400, "会话ID不能为空");
            }
            if (role == null || role.trim().isEmpty()) {
                logger.warn("角色为空");
                throw new BusinessException(400, "角色不能为空");
            }
            if (content == null || content.trim().isEmpty()) {
                logger.warn("消息内容为空");
                throw new BusinessException(400, "消息内容不能为空");
            }
            
            // 验证会话是否存在且属于当前用户
            logger.debug("验证会话是否存在且属于当前用户 - sessionId: {}", sessionId);
            // getSessionById方法内部会检查会话是否存在和权限，如果不满足会直接抛出异常
            chatSessionService.getSessionById(sessionId, userId);
            
            // 验证角色有效性
            logger.debug("验证角色有效性 - role: {}", role);
            if (!role.equals("user") && !role.equals("assistant") && !role.equals("system")) {
                logger.warn("无效的角色类型 - role: {}", role);
                throw new BusinessException(400, "无效的角色类型");
            }
            
            // 创建消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setUserId(userId);
            chatMessage.setRole(role);
            chatMessage.setContent(content);
            chatMessage.setCreatedAt(LocalDateTime.now());
            logger.debug("创建消息对象 - sessionId: {}, role: {}, createdAt: {}", 
                        sessionId, role, chatMessage.getCreatedAt());
            
            // 保存到数据库
            logger.debug("保存消息到数据库 - 准备调用Mapper");
            int result = chatMessageMapper.insert(chatMessage);
            logger.debug("Mapper返回结果: {}", result);
            
            if (result != 1) {
                logger.error("发送消息失败 - 影响行数: {}", result);
                throw new BusinessException(500, "发送消息失败");
            }
            
            logger.info("消息发送成功 - messageId: {}", chatMessage.getId());
            return chatMessage;
        } catch (BusinessException e) {
            logger.error("业务异常 - code: {}, message: {}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("消息发送过程中发生未预期异常", e);
            throw e;
        }
    }

    /**
     * 获取会话中的所有消息
     */
    public List<ChatMessage> getSessionMessages(Long sessionId, Long userId) {
        // 验证会话存在且属于当前用户
        chatSessionService.getSessionById(sessionId, userId);
        
        return chatMessageMapper.selectBySessionId(sessionId);
    }

    /**
     * 删除会话中的所有消息
     */
    @Transactional
    public void deleteSessionMessages(Long sessionId, Long userId) {
        // 验证会话存在且属于当前用户
        chatSessionService.getSessionById(sessionId, userId);
        
        chatMessageMapper.deleteBySessionId(sessionId);
    }

    /**
     * 统计会话中的消息数量
     */
    public Long countSessionMessages(Long sessionId, Long userId) {
        // 验证会话存在且属于当前用户
        chatSessionService.getSessionById(sessionId, userId);
        
        return chatMessageMapper.countBySessionId(sessionId);
    }
}