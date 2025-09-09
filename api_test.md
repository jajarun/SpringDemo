# Spring Boot JWT 登录验证中间件 API 测试文档

## 概述

本项目已成功集成了JWT登录验证中间件，包含以下功能：

1. **JWT Token 认证**：基于JWT的无状态认证
2. **角色权限控制**：支持USER、ADMIN、MODERATOR三种角色
3. **方法级权限验证**：使用`@PreAuthorize`注解进行细粒度权限控制
4. **自动用户初始化**：启动时自动创建默认管理员和普通用户

## 默认用户账号

启动应用后，系统会自动创建以下测试账号：

- **管理员账号**：
  - 邮箱：`admin@example.com`
  - 密码：`admin123`
  - 角色：`ADMIN`, `USER`

- **普通用户账号**：
  - 邮箱：`user@example.com`
  - 密码：`user123`
  - 角色：`USER`

## API 接口说明

### 1. 认证相关接口（无需认证）

#### 用户登录
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}

# 响应示例
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "admin@example.com",
  "name": "管理员",
  "roles": ["ADMIN", "USER"]
}
```

#### 用户注册
```bash
POST /api/v1/auth/register
Content-Type: application/json

{
  "name": "新用户",
  "email": "newuser@example.com",
  "password": "password123",
  "phone": "13800138001"
}
```

#### 获取用户资料（需要认证）
```bash
GET /api/v1/auth/profile
Authorization: Bearer <your-jwt-token>
```

### 2. 公共接口（无需认证）

```bash
GET /api/v1/public/health
GET /api/v1/public/info
GET /api/v1/public/version
```

### 3. 用户管理接口（需要认证）

#### 获取所有用户（需要USER或ADMIN角色）
```bash
GET /api/users
Authorization: Bearer <your-jwt-token>
```

#### 根据ID获取用户（需要USER或ADMIN角色）
```bash
GET /api/users/{id}
Authorization: Bearer <your-jwt-token>
```

#### 创建用户（需要ADMIN角色）
```bash
POST /api/users
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "name": "测试用户",
  "email": "test@example.com",
  "password": "password123",
  "phone": "13800138002"
}
```

#### 更新用户（需要ADMIN角色）
```bash
PUT /api/users/{id}
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "name": "更新后的用户名",
  "email": "updated@example.com",
  "phone": "13800138003"
}
```

#### 删除用户（需要ADMIN角色）
```bash
DELETE /api/users/{id}
Authorization: Bearer <your-jwt-token>
```

### 4. 动态查询接口（需要认证）

所有动态查询接口都需要USER或ADMIN角色：

```bash
# 基本搜索
GET /api/users/search?name=张&email=test@example.com
Authorization: Bearer <your-jwt-token>

# 分页搜索
GET /api/users/search/paginated?name=张&page=0&size=10&sort=createdAt,desc
Authorization: Bearer <your-jwt-token>

# 关键词搜索
GET /api/users/search/keyword?q=张三
Authorization: Bearer <your-jwt-token>

# 时间范围查询
GET /api/users/search/timerange?startTime=2023-01-01T00:00:00&endTime=2023-12-31T23:59:59
Authorization: Bearer <your-jwt-token>

# 获取有电话号码的用户
GET /api/users/with-phone
Authorization: Bearer <your-jwt-token>
```

## 测试步骤

### 1. 启动应用
```bash
mvn spring-boot:run
```

### 2. 测试登录
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }'
```

### 3. 使用Token访问受保护的API
```bash
# 将上面返回的token替换到下面的<TOKEN>中
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer <TOKEN>"
```

### 4. 测试权限控制
```bash
# 使用普通用户登录
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "user123"
  }'

# 尝试创建用户（应该返回403 Forbidden，因为普通用户没有ADMIN权限）
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "测试用户",
    "email": "test@example.com",
    "password": "password123"
  }'
```

## 权限控制说明

- **无需认证**：公共接口、登录、注册
- **需要认证（USER或ADMIN）**：查看用户信息、搜索用户
- **仅ADMIN可访问**：创建、更新、删除用户
- **管理员接口**：`/api/v1/admin/**` 路径下的所有接口

## 错误处理

- **401 Unauthorized**：未提供Token或Token无效
- **403 Forbidden**：权限不足
- **400 Bad Request**：请求参数错误
- **404 Not Found**：资源不存在

## 注意事项

1. JWT Token默认有效期为24小时（86400000毫秒）
2. Token需要在请求头中以 `Bearer <token>` 的格式发送
3. 密码会自动使用BCrypt加密存储
4. H2数据库控制台：http://localhost:8080/h2-console（无需认证） 