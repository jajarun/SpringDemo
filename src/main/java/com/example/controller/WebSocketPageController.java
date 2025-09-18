package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * WebSocket测试页面控制器
 */
@Controller
public class WebSocketPageController {

    /**
     * WebSocket测试页面
     */
    @GetMapping("/websocket-test")
    public String websocketTest() {
        return "websocket-test.html";
    }
}
