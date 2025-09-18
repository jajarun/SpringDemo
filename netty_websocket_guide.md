# Netty WebSocket 服务器完整指南

## 🎯 项目概述

我为您创建了一个基于**Spring Boot + Netty**的高性能WebSocket服务器，具备以下特性：

### ✨ 核心功能
- 🚀 **高性能**: 基于Netty NIO框架，支持高并发连接
- 💬 **群聊功能**: 支持多用户实时聊天
- 🔒 **私聊功能**: 支持用户间一对一私聊
- 👤 **用户管理**: 用户注册、在线状态管理
- 💓 **心跳检测**: 保持连接活跃，自动检测断线
- 📦 **二进制支持**: 支持文件传输等二进制数据
- 🔔 **系统通知**: 用户上线/下线通知

### 🏗️ 架构组件

```
📁 WebSocket服务器架构
├── NettyWebSocketServer.java      # Netty服务器启动器
├── WebSocketChannelHandler.java   # 消息处理核心
├── WebSocketInfoController.java   # REST API接口
├── WebSocketPageController.java   # 测试页面路由
└── websocket-test.html            # 前端测试界面
```

## 🔧 技术栈

| 组件 | 技术 | 版本 | 作用 |
|------|------|------|------|
| **框架** | Spring Boot | 3.x | 应用框架 |
| **网络层** | Netty | 4.x | 高性能NIO框架 |
| **协议** | WebSocket | RFC 6455 | 双向通信协议 |
| **序列化** | Jackson | 2.x | JSON消息处理 |
| **前端** | HTML5 + JavaScript | - | 测试界面 |

## 🚀 启动和使用

### 1. 启动服务器
```bash
# 编译项目
mvn clean compile

# 启动Spring Boot应用
mvn spring-boot:run
```

启动后会看到日志：
```
🚀 Netty WebSocket服务器启动成功！
📡 WebSocket地址: ws://localhost:9999/ws
🌐 测试页面: http://localhost:8080/websocket-test
```

### 2. 访问测试页面
打开浏览器访问: `http://localhost:8080/websocket-test`

### 3. 服务器信息API
```bash
# 获取WebSocket服务器信息
GET http://localhost:8080/api/websocket/info

# 获取统计信息
GET http://localhost:8080/api/websocket/stats
```

## 📡 WebSocket通信协议

### 消息格式
所有消息都使用JSON格式：
```json
{
  "type": "消息类型",
  "content": "消息内容", 
  "from": "发送者",
  "timestamp": "2024-01-01T12:00:00",
  "onlineCount": 5
}
```

### 支持的消息类型

#### 1. 用户注册 (`register`)
```javascript
// 客户端发送
{
  "type": "register",
  "userId": "张三"
}

// 服务器响应
{
  "type": "system",
  "from": "system", 
  "content": "注册成功！欢迎 张三"
}
```

#### 2. 群聊消息 (`chat`)
```javascript
// 客户端发送
{
  "type": "chat",
  "content": "大家好！"
}

// 服务器广播
{
  "type": "chat",
  "from": "张三",
  "content": "大家好！"
}
```

#### 3. 私聊消息 (`private`)
```javascript
// 客户端发送
{
  "type": "private",
  "targetUserId": "李四",
  "content": "你好，这是私聊消息"
}

// 服务器发送给目标用户
{
  "type": "private",
  "from": "张三", 
  "content": "你好，这是私聊消息"
}
```

#### 4. 心跳检测 (`ping`)
```javascript
// 客户端发送
{
  "type": "ping"
}

// 服务器响应
{
  "type": "system",
  "from": "system",
  "content": "服务器在线"
}
```

## 🧪 测试场景

### 场景1: 多用户聊天室
1. 打开多个浏览器标签页
2. 每个标签页连接并注册不同用户ID
3. 在一个标签页发送群聊消息
4. 观察其他标签页实时收到消息

### 场景2: 私聊功能
1. 用户A注册为"alice"
2. 用户B注册为"bob"  
3. 用户A发送私聊给"bob"
4. 只有bob能收到私聊消息

### 场景3: 连接管理
1. 多个用户连接
2. 关闭某个用户的浏览器
3. 其他用户收到"用户离线"通知
4. 在线用户数量实时更新

## 🔧 自定义扩展

### 1. 添加新的消息类型
在`WebSocketChannelHandler.java`中添加：
```java
case "your_new_type":
    handleYourNewType(channel, messageNode);
    break;
```

### 2. 集成数据库
```java
@Autowired
private UserRepository userRepository;

private void handleUserRegister(Channel channel, JsonNode messageNode) {
    // 验证用户身份
    User user = userRepository.findByUsername(userId);
    if (user == null) {
        sendMessage(channel, createMessage("error", "invalid_user", "用户不存在"));
        return;
    }
    // ... 其他逻辑
}
```

### 3. 添加房间功能
```java
// 房间管理
private static final Map<String, Set<Channel>> rooms = new ConcurrentHashMap<>();

private void joinRoom(Channel channel, String roomId) {
    rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(channel);
}

private void broadcastToRoom(String roomId, String message) {
    Set<Channel> roomChannels = rooms.get(roomId);
    if (roomChannels != null) {
        roomChannels.forEach(channel -> sendMessage(channel, message));
    }
}
```

## 🛡️ 安全考虑

### 1. 认证授权
```java
private void handleUserRegister(Channel channel, JsonNode messageNode) {
    String token = messageNode.get("token").asText();
    if (!jwtUtils.validateToken(token)) {
        sendMessage(channel, createMessage("error", "auth_failed", "认证失败"));
        ctx.close();
        return;
    }
    // ... 注册逻辑
}
```

### 2. 消息过滤
```java
private void handleChatMessage(Channel channel, JsonNode messageNode) {
    String content = messageNode.get("content").asText();
    
    // 内容过滤
    if (containsSensitiveWords(content)) {
        sendMessage(channel, createMessage("error", "content_blocked", "消息包含敏感词"));
        return;
    }
    
    // ... 处理逻辑
}
```

### 3. 连接限制
```java
private static final int MAX_CONNECTIONS_PER_IP = 10;
private static final Map<String, Integer> connectionCounts = new ConcurrentHashMap<>();

@Override
public void channelActive(ChannelHandlerContext ctx) throws Exception {
    String clientIp = getClientIp(ctx);
    int currentCount = connectionCounts.getOrDefault(clientIp, 0);
    
    if (currentCount >= MAX_CONNECTIONS_PER_IP) {
        ctx.close();
        return;
    }
    
    connectionCounts.put(clientIp, currentCount + 1);
    // ... 其他逻辑
}
```

## 📊 性能优化

### 1. 连接池配置
```java
// 在NettyWebSocketServer中
ServerBootstrap bootstrap = new ServerBootstrap();
bootstrap.option(ChannelOption.SO_BACKLOG, 1024)        // 连接队列大小
         .childOption(ChannelOption.SO_KEEPALIVE, true)   // 保持连接
         .childOption(ChannelOption.TCP_NODELAY, true)    // 禁用Nagle算法
         .childOption(ChannelOption.SO_RCVBUF, 32 * 1024) // 接收缓冲区
         .childOption(ChannelOption.SO_SNDBUF, 32 * 1024); // 发送缓冲区
```

### 2. 消息批处理
```java
private final Map<Channel, List<String>> pendingMessages = new ConcurrentHashMap<>();

private void batchSendMessages() {
    pendingMessages.forEach((channel, messages) -> {
        if (!messages.isEmpty()) {
            String batchMessage = String.join("\n", messages);
            sendMessage(channel, batchMessage);
            messages.clear();
        }
    });
}
```

## 🔍 故障排查

### 常见问题

#### 1. 连接失败
- 检查端口9999是否被占用
- 确认防火墙设置
- 查看服务器启动日志

#### 2. 消息丢失
- 检查网络连接稳定性
- 确认JSON格式正确
- 查看异常日志

#### 3. 内存泄漏
- 确保正确清理Channel映射
- 检查ThreadLocal使用
- 监控内存使用情况

### 监控指标
```java
// 添加监控
@Scheduled(fixedRate = 30000)
public void logStats() {
    logger.info("在线连接数: {}, 注册用户数: {}", 
               channels.size(), userChannels.size());
}
```

## 🎉 总结

现在您拥有了一个完整的Netty WebSocket服务器！

### 🎯 已实现功能
- ✅ 高性能Netty WebSocket服务器
- ✅ 用户注册和管理
- ✅ 群聊和私聊功能  
- ✅ 心跳检测机制
- ✅ 完整的测试界面
- ✅ REST API接口
- ✅ 实时在线状态

### 🚀 启动步骤
1. `mvn spring-boot:run` 启动应用
2. 访问 `http://localhost:8080/websocket-test`
3. 连接到 `ws://localhost:9999/ws`
4. 开始使用WebSocket功能！

这个WebSocket服务器可以轻松扩展为聊天室、实时游戏、在线协作等各种实时应用场景。🎊
