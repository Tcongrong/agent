package com.example.demo.ai.controller;

import com.example.demo.ai.dto.ChatMessageDTO;
import com.example.demo.ai.dto.MultiChatRequest;
import com.example.demo.ai.dto.TestChatRequest;
import com.example.demo.ai.service.DeepSeekService;
import com.example.demo.chat.entity.ChatSession;
import com.example.demo.chat.entity.ChatMessage;
import com.example.demo.chat.service.ChatSessionService;
import com.example.demo.chat.service.ChatMessageService;
import com.example.demo.common.ApiResponse;
import com.example.demo.common.BusinessException;
import com.example.demo.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AI测试控制器
 * 提供AI聊天测试接口，集成DeepSeek API并保存会话和消息到数据库
 */
@RestController
@RequestMapping("/api/ai/test")
public class AITestController {

    private static final Logger logger = LoggerFactory.getLogger(AITestController.class);
    
    private final DeepSeekService deepSeekService;
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AITestController(DeepSeekService deepSeekService,
                          ChatSessionService chatSessionService,
                          ChatMessageService chatMessageService,
                          JwtUtil jwtUtil) {
        this.deepSeekService = deepSeekService;
        this.chatSessionService = chatSessionService;
        this.chatMessageService = chatMessageService;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * 从请求头中获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(401, "未提供有效的认证token");
        }
        
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "token无效或已过期");
        }
        
        return jwtUtil.getUserIdFromToken(token);
    }
    
    /**
     * 从请求头中提取token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 健康检查接口
     * 测试AI服务是否正常运行
     * @return 健康状态响应
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("AI test service is running");
    }

    /**
     * 单轮对话接口
     * 处理单次聊天请求并将消息保存到数据库
     * @param req 测试聊天请求参数
     * @return 聊天响应
     */
    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(@RequestBody TestChatRequest req, HttpServletRequest request) {
        // 从请求头获取当前用户ID
        Long userId = getCurrentUserId(request);
        
        // 创建新会话
        String initialTitle = "Test Chat - " + LocalDateTime.now().toString();
        ChatSession session = chatSessionService.createSession(userId, initialTitle);

        // 使用标准对话
        String aiResponse = deepSeekService.chat(req.getPrompt(), req.getSystemMessage());

        // 保存用户消息
        chatMessageService.sendMessage(session.getId(), userId, "user", req.getPrompt());

        // 保存AI响应
        chatMessageService.sendMessage(session.getId(), userId, "assistant", aiResponse);

        // 更新会话标题（使用用户的前几个字符作为标题）
        String title = req.getPrompt().length() > 20 ? req.getPrompt().substring(0, 20) + "..." : req.getPrompt();
        chatSessionService.updateSessionTitle(session.getId(), userId, title);

        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", session.getId());
        response.put("prompt", req.getPrompt());
        response.put("response", aiResponse);

        return ApiResponse.ok(response);
    }

    /**
     * 多轮对话接口
     * 处理多轮聊天请求并将消息保存到数据库
     * @param req 多轮对话请求参数
     * @return 多轮对话响应
     */
    @PostMapping("/multi-chat")
    public ApiResponse<Map<String, Object>> multiChat(@RequestBody MultiChatRequest req, HttpServletRequest request) {
        // 从请求头获取当前用户ID
        Long userId = getCurrentUserId(request);
        ChatSession session;
        
        // 构建消息列表用于API调用
        List<Map<String, String>> apiMessages = new ArrayList<>();
        
        // 检查是否提供了会话ID
        Long sessionId = req.getSessionId();
        if (sessionId != null) {
            // 尝试获取现有会话
            session = chatSessionService.getSessionById(sessionId, userId);
            if (session == null) {
                // 会话不存在，创建新会话
                String initialTitle = "Multi Chat - " + LocalDateTime.now().toString();
                session = chatSessionService.createSession(userId, initialTitle);
            } else {
                // 从数据库加载历史消息
                List<ChatMessage> historyMessages = chatMessageService.getSessionMessages(sessionId, userId);
                for (ChatMessage msg : historyMessages) {
                    apiMessages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
                }
            }
        } else {
            // 没有提供会话ID，创建新会话
            String initialTitle = "Multi Chat - " + LocalDateTime.now().toString();
            session = chatSessionService.createSession(userId, initialTitle);
        }

        // 获取请求中的消息列表
        List<ChatMessageDTO> requestMessages = req.getMessages();
        
        // 处理消息列表，添加到API调用参数中
        for (ChatMessageDTO msg : requestMessages) {
            apiMessages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }
        
        // 查找最后一条用户消息作为最新问题
        String lastUserMessage = null;
        for (int i = requestMessages.size() - 1; i >= 0; i--) {
            if ("user".equals(requestMessages.get(i).getRole())) {
                lastUserMessage = requestMessages.get(i).getContent();
                break;
            }
        }
        
        // 保存请求中的消息到数据库（只保存尚未保存的新消息）
        if (sessionId == null || chatMessageService.getSessionMessages(session.getId(), userId).isEmpty()) {
            for (ChatMessageDTO msg : requestMessages) {
                chatMessageService.sendMessage(session.getId(), userId, msg.getRole(), msg.getContent());
            }
        } else {
            // 只保存最后一条用户消息（假设这是新消息）
            if (lastUserMessage != null) {
                chatMessageService.sendMessage(session.getId(), userId, "user", lastUserMessage);
            }
        }
        
        // 使用标准对话
        String aiResponse = deepSeekService.multiChat(apiMessages, req.getSystemMessage());
        
        // 保存AI响应
        chatMessageService.sendMessage(session.getId(), userId, "assistant", aiResponse);
        
        // 更新会话标题（使用第一条用户消息的前几个字符作为标题）
        if (lastUserMessage != null) {
            String title = lastUserMessage.length() > 20 ? lastUserMessage.substring(0, 20) + "..." : lastUserMessage;
            chatSessionService.updateSessionTitle(session.getId(), userId, title);
        }

        // 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getId());
        result.put("response", aiResponse);
        result.put("details", Map.of(
                "lastUserMessage", lastUserMessage,
                "aiResponse", aiResponse
        ));

        return ApiResponse.ok(result);
    }
    

}