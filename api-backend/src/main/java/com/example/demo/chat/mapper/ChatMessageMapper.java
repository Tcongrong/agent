package com.example.demo.chat.mapper;

import com.example.demo.chat.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息Mapper接口
 */
@Mapper
public interface ChatMessageMapper {
    
    /**
     * 根据ID查询消息
     */
    ChatMessage selectById(@Param("id") Long id);
    
    /**
     * 根据会话ID查询消息列表
     */
    List<ChatMessage> selectBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * 插入消息
     */
    int insert(ChatMessage chatMessage);
    
    /**
     * 根据会话ID删除所有消息
     */
    int deleteBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * 统计会话中的消息数量
     */
    Long countBySessionId(@Param("sessionId") Long sessionId);
}