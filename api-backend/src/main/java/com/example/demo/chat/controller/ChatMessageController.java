package com.example.demo.chat.controller;

import com.example.demo.chat.dto.SendMessageRequest;
import com.example.demo.chat.entity.ChatMessage;
import com.example.demo.chat.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天消息控制器
 */
@RestController
@RequestMapping("/api/chat/messages")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 发送消息
     */
    @PostMapping
    public ChatMessage sendMessage(@Validated @RequestBody SendMessageRequest request, 
                                   @RequestAttribute("userId") Long userId) {
        return chatMessageService.sendMessage(request.getSessionId(), userId, 
                                            request.getRole(), request.getContent());
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/session/{sessionId}")
    public List<ChatMessage> getSessionMessages(@PathVariable("sessionId") Long sessionId, 
                                               @RequestAttribute("userId") Long userId) {
        return chatMessageService.getSessionMessages(sessionId, userId);
    }

    /**
     * 删除会话中的所有消息
     */
    @DeleteMapping("/session/{sessionId}")
    public void deleteSessionMessages(@PathVariable("sessionId") Long sessionId, 
                                     @RequestAttribute("userId") Long userId) {
        chatMessageService.deleteSessionMessages(sessionId, userId);
    }

    /**
     * 统计会话消息数量
     */
    @GetMapping("/session/{sessionId}/count")
    public Long countSessionMessages(@PathVariable("sessionId") Long sessionId, 
                                    @RequestAttribute("userId") Long userId) {
        return chatMessageService.countSessionMessages(sessionId, userId);
    }
}