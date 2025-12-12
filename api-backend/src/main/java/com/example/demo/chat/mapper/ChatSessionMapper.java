package com.example.demo.chat.mapper;

import com.example.demo.chat.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天会话数据访问接口
 */
@Mapper
public interface ChatSessionMapper {
    
    /**
     * 根据ID查找会话
     */
    ChatSession selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID查找所有会话（未删除的）
     */
    List<ChatSession> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 插入新会话
     */
    int insert(ChatSession chatSession);
    
    /**
     * 更新会话信息
     */
    int update(ChatSession chatSession);
    
    /**
     * 更新会话标题
     */
    int updateTitle(@Param("id") Long id, @Param("title") String title);
    
    /**
     * 软删除会话
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 批量软删除会话
     */
    int deleteBatchByIds(@Param("ids") List<Long> ids, @Param("userId") Long userId);
    
    /**
     * 统计用户的会话数量
     */
    Long countByUserId(@Param("userId") Long userId);
}