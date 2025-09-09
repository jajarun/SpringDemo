package com.example.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Map;

@Component
public class PublicHandler {

    public ServerResponse health(ServerRequest request) {
        Map<String, Object> response = Map.of(
            "status", "UP",
            "message", "应用运行正常"
        );
        return ServerResponse.ok().body(response);
    }

    public ServerResponse info(ServerRequest request) {
        Map<String, Object> response = Map.of(
            "application", "Spring Boot Demo",
            "version", "1.0.0",
            "description", "Spring Boot 路由分组演示"
        );
        return ServerResponse.ok().body(response);
    }

    public ServerResponse version(ServerRequest request) {
        Map<String, Object> response = Map.of(
            "version", "1.0.0",
            "buildTime", "2024-01-01"
        );
        return ServerResponse.ok().body(response);
    }

    public ServerResponse login(ServerRequest request) {
        Map<String, Object> response = Map.of(
            "message", "登录功能 - 这里应该实现具体的登录逻辑",
            "token", "demo-token-123"
        );
        return ServerResponse.ok().body(response);
    }

    public ServerResponse logout(ServerRequest request) {
        Map<String, Object> response = Map.of(
            "message", "登出成功"
        );
        return ServerResponse.ok().body(response);
    }

    public ServerResponse profile(ServerRequest request) {
        Map<String, Object> response = Map.of(
            "message", "用户资料 - 这里应该返回当前用户信息",
            "user", "demo-user"
        );
        return ServerResponse.ok().body(response);
    }
} 