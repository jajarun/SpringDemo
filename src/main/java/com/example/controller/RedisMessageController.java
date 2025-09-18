package com.example.controller;

import com.example.dto.MessageDTO;
import com.example.service.RedisMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/redis-messages")
@CrossOrigin(origins = "*")
public class RedisMessageController {

    @Autowired
    private RedisMessageService redisMessageService;

    /**
     * 发送用户消息到Redis队列
     */
    @PostMapping("/user")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> sendUserMessage(@RequestParam String receiver, 
                                           @RequestParam String content) {
        try {
            String messageId = redisMessageService.sendUserMessage(receiver, content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户消息发送成功");
            response.put("messageId", messageId);
            response.put("receiver", receiver);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "消息发送失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 发送通知消息（发布订阅模式）
     */
    @PostMapping("/notification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sendNotification(@RequestParam String receiver, 
                                            @RequestParam String content) {
        try {
            redisMessageService.sendNotification(receiver, content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "通知消息发送成功");
            response.put("receiver", receiver);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "通知发送失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 从队列消费单条消息
     */
    @PostMapping("/consume/{queueName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> consumeMessage(@PathVariable String queueName,
                                          @RequestParam(defaultValue = "5") int timeoutSeconds) {
        try {
            MessageDTO message = redisMessageService.consumeFromQueue(queueName, timeoutSeconds);
            
            Map<String, Object> response = new HashMap<>();
            if (message != null) {
                response.put("success", true);
                response.put("message", message);
                response.put("queueName", queueName);
            } else {
                response.put("success", false);
                response.put("message", "队列为空或超时");
                response.put("queueName", queueName);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "消费消息失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量消费消息
     */
    @PostMapping("/consume/{queueName}/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> batchConsumeMessages(@PathVariable String queueName,
                                                @RequestParam(defaultValue = "10") int batchSize) {
        try {
            List<MessageDTO> messages = redisMessageService.batchConsumeFromQueue(queueName, batchSize);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messages", messages);
            response.put("count", messages.size());
            response.put("queueName", queueName);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量消费失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取队列统计信息
     */
    @GetMapping("/stats/{queueName}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getQueueStats(@PathVariable String queueName) {
        try {
            Map<String, Object> stats = redisMessageService.getQueueStats(queueName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("queueName", queueName);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取缓存的消息
     */
    @GetMapping("/cache/{messageId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getMessageFromCache(@PathVariable String messageId) {
        try {
            MessageDTO message = redisMessageService.getMessageFromCache(messageId);
            
            if (message != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", message);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "消息未找到或已过期");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取消息失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户消息列表
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.name)")
    public ResponseEntity<?> getUserMessages(@PathVariable String userId, 
                                           @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Object> messageIds = redisMessageService.getUserMessages(userId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("messageIds", messageIds);
            response.put("count", messageIds.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取用户消息失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取消息状态
     */
    @GetMapping("/status/{messageId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getMessageStatus(@PathVariable String messageId) {
        try {
            String status = redisMessageService.getMessageStatus(messageId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messageId", messageId);
            response.put("status", status != null ? status : "UNKNOWN");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取消息状态失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 发送自定义消息到指定队列
     */
    @PostMapping("/custom/{queueName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sendCustomMessage(@PathVariable String queueName,
                                             @RequestBody MessageDTO message) {
        try {
            String messageId = redisMessageService.sendToQueue(queueName, message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "自定义消息发送成功");
            response.put("queueName", queueName);
            response.put("messageId", messageId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "自定义消息发送失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 发布消息到Redis频道
     */
    @PostMapping("/publish/{channel}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> publishMessage(@PathVariable String channel,
                                          @RequestBody MessageDTO message) {
        try {
            redisMessageService.publishMessage(channel, message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "消息发布成功");
            response.put("channel", channel);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "消息发布失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
} 