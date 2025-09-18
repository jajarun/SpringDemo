package com.example.service;

import com.example.dto.MessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class RedisMessageService {

    private static final Logger logger = LoggerFactory.getLogger(RedisMessageService.class);
    
    // Redis队列名称
    private static final String USER_QUEUE = "queue:user";
    private static final String PROCESSING_QUEUE = "queue:processing";
    
    // Redis发布订阅频道
    private static final String USER_CHANNEL = "channel:user";
    private static final String NOTIFICATION_CHANNEL = "channel:notification";
    
    // Redis缓存前缀
    private static final String MESSAGE_CACHE_PREFIX = "message:";
    private static final String USER_MESSAGES_PREFIX = "user_messages:";
    private static final String QUEUE_STATS_PREFIX = "stats:queue:";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private RedisMessageListenerContainer messageListenerContainer;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void initMessageListeners() {
        // 订阅用户频道
        messageListenerContainer.addMessageListener((message, pattern) -> {
            try {
                String messageBody = new String(message.getBody());
                MessageDTO msg = objectMapper.readValue(messageBody, MessageDTO.class);
                handleUserChannelMessage(msg);
            } catch (Exception e) {
                logger.error("处理用户频道消息失败: {}", e.getMessage(), e);
            }
        }, new ChannelTopic(USER_CHANNEL));

        // 订阅通知频道
        messageListenerContainer.addMessageListener((message, pattern) -> {
            try {
                String messageBody = new String(message.getBody());
                MessageDTO msg = objectMapper.readValue(messageBody, MessageDTO.class);
                handleNotificationChannelMessage(msg);
            } catch (Exception e) {
                logger.error("处理通知频道消息失败: {}", e.getMessage(), e);
            }
        }, new ChannelTopic(NOTIFICATION_CHANNEL));
    }

    /**
     * 使用Redis List作为消息队列发送消息
     */
    public String sendToQueue(String queueName, MessageDTO message) {
        try {
            // 生成消息ID
            message.setId(UUID.randomUUID().toString());
            message.setTimestamp(LocalDateTime.now());
            
            // 序列化消息
            String messageJson = objectMapper.writeValueAsString(message);
            
            // 推送到Redis队列（List结构）
            redisTemplate.opsForList().leftPush(queueName, messageJson);
            
            // 缓存消息详情
            cacheMessage(message);
            
            // 更新队列统计
            updateQueueStats(queueName, "sent");
            
            logger.info("消息已发送到Redis队列: {} - {}", queueName, message.getId());
            return message.getId();
            
        } catch (Exception e) {
            logger.error("发送消息到队列失败: {}", e.getMessage(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

    /**
     * 使用Redis发布订阅发送消息
     */
    public void publishMessage(String channel, MessageDTO message) {
        try {
            message.setId(UUID.randomUUID().toString());
            message.setTimestamp(LocalDateTime.now());
            
            String messageJson = objectMapper.writeValueAsString(message);
            
            // 发布到Redis频道
            redisTemplate.convertAndSend(channel, messageJson);
            
            // 缓存消息
            cacheMessage(message);
            
            logger.info("消息已发布到Redis频道: {} - {}", channel, message.getId());
            
        } catch (Exception e) {
            logger.error("发布消息失败: {}", e.getMessage(), e);
            throw new RuntimeException("消息发布失败", e);
        }
    }

    /**
     * 从Redis队列消费消息
     */
    public MessageDTO consumeFromQueue(String queueName, int timeoutSeconds) {
        try {
            // 使用阻塞弹出从队列获取消息
            Object result = redisTemplate.opsForList().rightPop(queueName, timeoutSeconds, TimeUnit.SECONDS);
            
            if (result != null) {
                String messageJson = (String) result; // rightPop直接返回值
                MessageDTO message = objectMapper.readValue(messageJson, MessageDTO.class);
                
                // 移动到处理队列
                redisTemplate.opsForList().leftPush(PROCESSING_QUEUE, messageJson);
                
                // 更新统计
                updateQueueStats(queueName, "consumed");
                updateQueueStats(PROCESSING_QUEUE, "processing");
                
                logger.info("从队列消费消息: {} - {}", queueName, message.getId());
                return message;
            }
            
            return null;
        } catch (Exception e) {
            logger.error("从队列消费消息失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 发送用户消息到队列
     */
    public String sendUserMessage(String receiver, String content) {
        MessageDTO message = new MessageDTO(content, "system", "USER_MESSAGE");
        message.setReceiver(receiver);
        return sendToQueue(USER_QUEUE, message);
    }

    /**
     * 发送通知消息（发布订阅模式）
     */
    public void sendNotification(String receiver, String content) {
        MessageDTO message = new MessageDTO(content, "system", "NOTIFICATION");
        message.setReceiver(receiver);
        publishMessage(NOTIFICATION_CHANNEL, message);
    }

    /**
     * 批量处理队列消息
     */
    public List<MessageDTO> batchConsumeFromQueue(String queueName, int batchSize) {
        List<MessageDTO> messages = new ArrayList<>();
        
        try {
            for (int i = 0; i < batchSize; i++) {
                String messageJson = (String) redisTemplate.opsForList().rightPop(queueName);
                if (messageJson != null) {
                    MessageDTO message = objectMapper.readValue(messageJson, MessageDTO.class);
                    messages.add(message);
                    
                    // 移动到处理队列
                    redisTemplate.opsForList().leftPush(PROCESSING_QUEUE, messageJson);
                } else {
                    break; // 队列为空
                }
            }
            
            logger.info("批量消费消息: {} 条来自队列: {}", messages.size(), queueName);
            return messages;
            
        } catch (Exception e) {
            logger.error("批量消费消息失败: {}", e.getMessage(), e);
            return messages;
        }
    }

    /**
     * 获取队列长度
     */
    public Long getQueueSize(String queueName) {
        return redisTemplate.opsForList().size(queueName);
    }

    /**
     * 获取队列统计信息
     */
    public Map<String, Object> getQueueStats(String queueName) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("queueSize", getQueueSize(queueName));
        stats.put("sentCount", redisTemplate.opsForHash().get(QUEUE_STATS_PREFIX + queueName, "sent"));
        stats.put("consumedCount", redisTemplate.opsForHash().get(QUEUE_STATS_PREFIX + queueName, "consumed"));
        return stats;
    }

    /**
     * 缓存消息
     */
    private void cacheMessage(MessageDTO message) {
        String cacheKey = MESSAGE_CACHE_PREFIX + message.getId();
        redisTemplate.opsForValue().set(cacheKey, message, 24, TimeUnit.HOURS);
        
        // 添加到用户消息列表
        if (message.getReceiver() != null) {
            String userMessagesKey = USER_MESSAGES_PREFIX + message.getReceiver();
            redisTemplate.opsForList().leftPush(userMessagesKey, message.getId());
            redisTemplate.expire(userMessagesKey, 7, TimeUnit.DAYS);
        }
    }

    /**
     * 更新队列统计
     */
    private void updateQueueStats(String queueName, String operation) {
        String statsKey = QUEUE_STATS_PREFIX + queueName;
        redisTemplate.opsForHash().increment(statsKey, operation, 1);
        redisTemplate.expire(statsKey, 30, TimeUnit.DAYS);
    }

    /**
     * 处理用户频道消息
     */
    private void handleUserChannelMessage(MessageDTO message) {
        logger.info("处理用户频道消息: {}", message);
        // 这里可以添加具体的处理逻辑
        updateMessageStatus(message.getId(), "CHANNEL_PROCESSED");
    }

    /**
     * 处理通知频道消息
     */
    private void handleNotificationChannelMessage(MessageDTO message) {
        logger.info("处理通知频道消息: {}", message);
        // 这里可以添加具体的处理逻辑
        updateMessageStatus(message.getId(), "NOTIFICATION_SENT");
    }

    /**
     * 更新消息状态
     */
    private void updateMessageStatus(String messageId, String status) {
        String statusKey = MESSAGE_CACHE_PREFIX + messageId + ":status";
        redisTemplate.opsForValue().set(statusKey, status, 24, TimeUnit.HOURS);
    }

    /**
     * 获取消息状态
     */
    public String getMessageStatus(String messageId) {
        String statusKey = MESSAGE_CACHE_PREFIX + messageId + ":status";
        return (String) redisTemplate.opsForValue().get(statusKey);
    }

    /**
     * 获取缓存的消息
     */
    public MessageDTO getMessageFromCache(String messageId) {
        String cacheKey = MESSAGE_CACHE_PREFIX + messageId;
        return (MessageDTO) redisTemplate.opsForValue().get(cacheKey);
    }

    /**
     * 获取用户消息列表
     */
    public List<Object> getUserMessages(String userId, int limit) {
        String userMessagesKey = USER_MESSAGES_PREFIX + userId;
        return redisTemplate.opsForList().range(userMessagesKey, 0, limit - 1);
    }
} 