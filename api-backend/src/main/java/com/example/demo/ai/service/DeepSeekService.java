package com.example.demo.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek API服务类
 * 负责与DeepSeek API进行交互
 */
@Service
public class DeepSeekService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public DeepSeekService(RestTemplate restTemplate,
                         @Value("${deepseek.api.key:}") String apiKey,
                         @Value("${deepseek.api.url:https://api.deepseek.com}") String baseUrl,
                         @Value("${deepseek.model:deepseek-chat}") String model) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    /**
     * 发送单轮对话请求到DeepSeek API
     * @param prompt 用户提示
     * @param systemMessage 系统消息（可选）
     * @return AI响应内容
     */
    public String chat(String prompt, String systemMessage) {
        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 添加系统消息（如果有）
        if (systemMessage != null && !systemMessage.isEmpty()) {
            messages.add(Map.of("role", "system", "content", systemMessage));
        }
        
        // 添加用户消息
        messages.add(Map.of("role", "user", "content", prompt));
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        // 发送请求并获取响应
        try {
            DeepSeekResponse response = restTemplate.postForObject(
                    baseUrl + "/chat/completions", 
                    request, 
                    DeepSeekResponse.class
            );
            
            if (response != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
            return "No response from AI service";
        } catch (Exception e) {
            // 处理异常
            return "Error calling AI service: " + e.getMessage();
        }
    }

    /**
     * 发送多轮对话请求到DeepSeek API
     * @param messages 消息列表，每个消息包含role和content
     * @param systemMessage 系统消息（可选）
     * @return AI响应内容
     */
    public String multiChat(List<Map<String, String>> messages, String systemMessage) {
        // 构建完整的消息列表
        List<Map<String, String>> fullMessages = new ArrayList<>();
        
        // 添加系统消息（如果有）
        if (systemMessage != null && !systemMessage.isEmpty()) {
            fullMessages.add(Map.of("role", "system", "content", systemMessage));
        }
        
        // 添加所有历史消息
        fullMessages.addAll(messages);
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", fullMessages);
        requestBody.put("stream", false);
        
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        // 发送请求并获取响应
        try {
            DeepSeekResponse response = restTemplate.postForObject(
                    baseUrl + "/chat/completions", 
                    request, 
                    DeepSeekResponse.class
            );
            
            if (response != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
            return "No response from AI service";
        } catch (Exception e) {
            // 处理异常
            return "Error calling AI service: " + e.getMessage();
        }
    }

    // DeepSeek API响应类
    public static class DeepSeekResponse {
        private List<Choice> choices;
        
        public List<Choice> getChoices() {
            return choices;
        }
        
        public void setChoices(List<Choice> choices) {
            this.choices = choices;
        }
        
        public static class Choice {
            private Message message;
            
            public Message getMessage() {
                return message;
            }
            
            public void setMessage(Message message) {
                this.message = message;
            }
        }
        
        public static class Message {
            private String content;
            
            public String getContent() {
                return content;
            }
            
            public void setContent(String content) {
                this.content = content;
            }
        }
    }
}