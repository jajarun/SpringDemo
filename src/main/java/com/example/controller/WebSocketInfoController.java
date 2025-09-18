package com.example.controller;

import com.example.websocket.WebSocketChannelHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket信息控制器
 * 提供WebSocket服务器状态和统计信息
 */
@RestController
@RequestMapping("/api/websocket")
public class WebSocketInfoController {

    /**
     * 获取WebSocket服务器信息
     */
    @GetMapping("/info")
    public Map<String, Object> getWebSocketInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("serverStatus", "运行中");
        info.put("serverPort", 9999);
        info.put("websocketPath", "/ws");
        info.put("websocketUrl", "ws://localhost:9999/ws");
        info.put("onlineCount", WebSocketChannelHandler.getOnlineCount());
        info.put("onlineUsers", WebSocketChannelHandler.getOnlineUsers());
        info.put("testPageUrl", "http://localhost:8080/websocket-test");
        
        return info;
    }

    /**
     * 获取WebSocket统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getWebSocketStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalConnections", WebSocketChannelHandler.getOnlineCount());
        stats.put("onlineUsers", WebSocketChannelHandler.getOnlineUsers());
        stats.put("serverInfo", Map.of(
            "framework", "Netty",
            "protocol", "WebSocket",
            "features", new String[]{"群聊", "私聊", "用户注册", "心跳检测", "二进制消息"}
        ));
        
        return stats;
    }
}
