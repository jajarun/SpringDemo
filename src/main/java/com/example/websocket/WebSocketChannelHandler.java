package com.example.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息处理器
 * 处理客户端连接、消息收发、断开连接等事件
 */
@Component
@ChannelHandler.Sharable
public class WebSocketChannelHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketChannelHandler.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 存储所有连接的客户端
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    
    // 存储用户ID与Channel的映射
    private static final Map<String, Channel> userChannels = new ConcurrentHashMap<>();
    
    // 存储Channel与用户信息的映射
    private static final Map<String, String> channelUsers = new ConcurrentHashMap<>();

    /**
     * 客户端连接建立时调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channels.add(channel);
        
        String channelId = channel.id().asShortText();
        logger.info("🔗 新的WebSocket连接建立: {}", channelId);
        
        // 发送欢迎消息
        sendMessage(channel, createMessage("system", "连接成功", "欢迎使用WebSocket服务！"));
        
        // 广播连接通知（除了新连接的客户端）
        broadcastToOthers(channel, createMessage("system", "user_joined", 
                "新用户加入聊天室 (连接ID: " + channelId + ")"));
        
        super.channelActive(ctx);
    }

    /**
     * 客户端断开连接时调用
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String channelId = channel.id().asShortText();
        
        // 清理用户映射
        String userId = channelUsers.remove(channelId);
        if (userId != null) {
            userChannels.remove(userId);
            logger.info("🔌 用户断开连接: {} (ID: {})", userId, channelId);
            
            // 广播离线通知
            broadcastMessage(createMessage("system", "user_left", 
                    "用户 " + userId + " 离开了聊天室"));
        } else {
            logger.info("🔌 连接断开: {}", channelId);
        }
        
        channels.remove(channel);
        super.channelInactive(ctx);
    }

    /**
     * 处理WebSocket消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            // 处理文本消息
            handleTextMessage(ctx, (TextWebSocketFrame) frame);
        } else if (frame instanceof BinaryWebSocketFrame) {
            // 处理二进制消息
            handleBinaryMessage(ctx, (BinaryWebSocketFrame) frame);
        } else if (frame instanceof PingWebSocketFrame) {
            // 处理Ping消息
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
        } else if (frame instanceof CloseWebSocketFrame) {
            // 处理关闭消息
            ctx.close();
        }
    }

    /**
     * 处理文本消息
     */
    private void handleTextMessage(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String message = frame.text();
        Channel channel = ctx.channel();
        String channelId = channel.id().asShortText();
        
        logger.info("📨 收到消息来自 {}: {}", channelId, message);
        
        try {
            JsonNode messageNode = objectMapper.readTree(message);
            String type = messageNode.get("type").asText();
            
            switch (type) {
                case "register":
                    // 用户注册
                    handleUserRegister(channel, messageNode);
                    break;
                case "chat":
                    // 聊天消息
                    handleChatMessage(channel, messageNode);
                    break;
                case "private":
                    // 私聊消息
                    handlePrivateMessage(channel, messageNode);
                    break;
                case "ping":
                    // 心跳检测
                    sendMessage(channel, createMessage("system", "pong", "服务器在线"));
                    break;
                default:
                    // 未知消息类型
                    sendMessage(channel, createMessage("error", "unknown_type", 
                            "未知的消息类型: " + type));
            }
        } catch (Exception e) {
            logger.error("处理消息时出错", e);
            sendMessage(channel, createMessage("error", "parse_error", 
                    "消息格式错误: " + e.getMessage()));
        }
    }

    /**
     * 处理用户注册
     */
    private void handleUserRegister(Channel channel, JsonNode messageNode) {
        String userId = messageNode.get("userId").asText();
        String channelId = channel.id().asShortText();
        
        // 检查用户是否已存在
        if (userChannels.containsKey(userId)) {
            sendMessage(channel, createMessage("error", "user_exists", 
                    "用户ID已存在: " + userId));
            return;
        }
        
        // 注册用户
        userChannels.put(userId, channel);
        channelUsers.put(channelId, userId);
        
        logger.info("👤 用户注册成功: {} (连接ID: {})", userId, channelId);
        
        // 发送注册成功消息
        sendMessage(channel, createMessage("system", "register_success", 
                "注册成功！欢迎 " + userId));
        
        // 广播用户上线通知
        broadcastToOthers(channel, createMessage("system", "user_online", 
                "用户 " + userId + " 上线了"));
        
        // 发送在线用户列表
        sendMessage(channel, createMessage("system", "online_users", 
                "在线用户: " + String.join(", ", userChannels.keySet())));
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(Channel channel, JsonNode messageNode) {
        String channelId = channel.id().asShortText();
        String userId = channelUsers.get(channelId);
        
        if (userId == null) {
            sendMessage(channel, createMessage("error", "not_registered", 
                    "请先注册用户ID"));
            return;
        }
        
        String content = messageNode.get("content").asText();
        
        // 广播聊天消息
        String chatMessage = createMessage("chat", userId, content);
        broadcastMessage(chatMessage);
        
        logger.info("💬 用户 {} 发送聊天消息: {}", userId, content);
    }

    /**
     * 处理私聊消息
     */
    private void handlePrivateMessage(Channel senderChannel, JsonNode messageNode) {
        String senderChannelId = senderChannel.id().asShortText();
        String senderId = channelUsers.get(senderChannelId);
        
        if (senderId == null) {
            sendMessage(senderChannel, createMessage("error", "not_registered", 
                    "请先注册用户ID"));
            return;
        }
        
        String targetUserId = messageNode.get("targetUserId").asText();
        String content = messageNode.get("content").asText();
        
        Channel targetChannel = userChannels.get(targetUserId);
        if (targetChannel == null) {
            sendMessage(senderChannel, createMessage("error", "user_not_found", 
                    "用户不在线: " + targetUserId));
            return;
        }
        
        // 发送私聊消息给目标用户
        String privateMessage = createMessage("private", senderId, content);
        sendMessage(targetChannel, privateMessage);
        
        // 给发送者确认
        sendMessage(senderChannel, createMessage("system", "private_sent", 
                "私聊消息已发送给 " + targetUserId));
        
        logger.info("🔒 用户 {} 向 {} 发送私聊: {}", senderId, targetUserId, content);
    }

    /**
     * 处理二进制消息
     */
    private void handleBinaryMessage(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) {
        logger.info("📦 收到二进制消息，大小: {} bytes", frame.content().readableBytes());
        
        // 这里可以处理文件传输等二进制数据
        // 示例：回显二进制数据
        ctx.writeAndFlush(new BinaryWebSocketFrame(frame.content().retain()));
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("WebSocket连接异常", cause);
        ctx.close();
    }

    /**
     * 创建消息JSON
     */
    private String createMessage(String type, String from, String content) {
        try {
            Map<String, Object> message = Map.of(
                "type", type,
                "from", from,
                "content", content,
                "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "onlineCount", channels.size()
            );
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            logger.error("创建消息JSON失败", e);
            return "{\"type\":\"error\",\"content\":\"消息创建失败\"}";
        }
    }

    /**
     * 向指定通道发送消息
     */
    private void sendMessage(Channel channel, String message) {
        if (channel.isActive()) {
            channel.writeAndFlush(new TextWebSocketFrame(message));
        }
    }

    /**
     * 广播消息给所有连接的客户端
     */
    private void broadcastMessage(String message) {
        channels.writeAndFlush(new TextWebSocketFrame(message));
    }

    /**
     * 广播消息给除指定通道外的所有客户端
     */
    private void broadcastToOthers(Channel excludeChannel, String message) {
        for (Channel channel : channels) {
            if (channel != excludeChannel && channel.isActive()) {
                channel.writeAndFlush(new TextWebSocketFrame(message));
            }
        }
    }

    /**
     * 获取在线用户数量
     */
    public static int getOnlineCount() {
        return channels.size();
    }

    /**
     * 获取在线用户列表
     */
    public static java.util.Set<String> getOnlineUsers() {
        return userChannels.keySet();
    }
}
