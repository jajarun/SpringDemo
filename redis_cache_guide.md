# Redis缓存配置完全指南

## 🎯 缓存存储位置说明

### 1. **配置前（默认情况）**
```java
@Cacheable("findUsersByEmail")
Optional<User> findByEmail(String email);
```
- **存储位置**: 应用内存（ConcurrentHashMap）
- **生命周期**: 应用重启后丢失
- **集群支持**: ❌ 不支持
- **性能**: ⚡ 极快

### 2. **配置后（Redis存储）**
```java
@Cacheable("findUsersByEmail")
Optional<User> findByEmail(String email);
```
- **存储位置**: Redis数据库
- **生命周期**: 持久化，支持TTL过期
- **集群支持**: ✅ 完全支持
- **性能**: 🚀 很快

## 🔧 完整配置说明

### 1. Maven依赖（已添加）
```xml
<!-- Spring Cache支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- Redis支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 2. application.properties配置（已更新）
```properties
# Redis 配置
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
spring.redis.timeout=2000ms

# Spring Cache 配置
spring.cache.type=redis                    # 指定缓存类型为Redis
spring.cache.redis.time-to-live=1800000    # 默认过期时间30分钟
spring.cache.redis.cache-null-values=false # 不缓存null值
spring.cache.redis.key-prefix=springdemo:: # 缓存key前缀
```

### 3. RedisCacheConfig.java（已创建）
- 🎯 配置JSON序列化器
- ⏰ 为不同缓存设置不同过期时间
- 🔑 自定义缓存key前缀
- 🔄 支持事务

## 📊 缓存配置详情

| 缓存名称 | 过期时间 | Key前缀 | 用途 |
|---------|----------|---------|------|
| `findUsersByEmail` | 2小时 | `user::email::` | 用户邮箱查询 |
| `userList` | 15分钟 | `user::list::` | 用户列表 |
| `userDetails` | 45分钟 | `user::detail::` | 用户详情 |
| `userSearch` | 10分钟 | `search::` | 搜索结果 |
| `userPage` | 8分钟 | `page::` | 分页数据 |

## 🧪 测试缓存是否生效

### 1. 启动Redis
```bash
# 使用Docker启动Redis（如果还没启动）
docker run -d -p 6379:6379 --name redis redis:7-alpine

# 或使用项目的docker-compose
docker-compose up redis
```

### 2. 测试API接口
```bash
# 第一次查询（从数据库，较慢）
curl http://localhost:8080/api/cache/user/email/test@example.com

# 第二次查询（从Redis缓存，很快）
curl http://localhost:8080/api/cache/user/email/test@example.com

# 查看Redis中的缓存键
curl http://localhost:8080/api/cache/redis/keys

# 查看缓存统计
curl http://localhost:8080/api/cache/stats
```

### 3. 使用Redis CLI查看
```bash
# 连接Redis
redis-cli

# 查看所有键
KEYS *

# 查看用户邮箱缓存
KEYS user::email::*

# 查看缓存内容
GET "user::email::test@example.com"

# 查看键的过期时间
TTL "user::email::test@example.com"
```

## 🎛️ 缓存管理操作

### 1. 清除特定缓存
```bash
# 清除特定邮箱的缓存
curl -X DELETE http://localhost:8080/api/cache/user/email/test@example.com

# 清除所有用户列表缓存
curl -X DELETE http://localhost:8080/api/cache/users
```

### 2. 清除所有缓存
```bash
curl -X DELETE http://localhost:8080/api/cache/all
```

### 3. 预热缓存
```bash
curl -X POST http://localhost:8080/api/cache/warmup
```

## 🚀 在您的代码中使用

### 1. Repository中（已有）
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 这个缓存现在会存储到Redis
    @Cacheable("findUsersByEmail")
    Optional<User> findByEmail(String email);
}
```

### 2. Service中添加更多缓存
```java
@Service
public class UserService {
    
    // 缓存用户详情
    @Cacheable(value = "userDetails", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    // 缓存用户列表
    @Cacheable(value = "userList", key = "'all'")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // 更新时清除相关缓存
    @CacheEvict(value = {"userDetails", "userList"}, allEntries = true)
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
```

## 🔍 验证缓存工作状态

### 方法1: 查看查询时间
- 第一次查询: 通常 > 50ms（数据库查询）
- 后续查询: 通常 < 10ms（Redis缓存）

### 方法2: 查看Redis键
```bash
# 在Redis CLI中
KEYS *
# 应该看到类似这样的键：
# cache::user::email::test@example.com
# cache::user::list::all
```

### 方法3: 查看应用日志
启用SQL日志，第二次查询时不应该有SQL输出：
```properties
spring.jpa.show-sql=true
```

## 🎯 针对读写分离延时的缓存策略

### 1. 写后读缓存
```java
@Transactional
public User updateUser(User user) {
    User updated = userRepository.save(user);
    
    // 立即更新缓存，避免读取从库的旧数据
    cacheManager.getCache("userDetails").put(user.getId(), updated);
    
    return updated;
}
```

### 2. 强制主库读取时的缓存处理
```java
@ForceMaster  // 强制读主库
@Cacheable(value = "userDetails", key = "#id", condition = "#useCache")
public User getUserById(Long id, boolean useCache) {
    return userRepository.findById(id).orElse(null);
}
```

## 🛡️ 缓存最佳实践

1. **合理设置过期时间**: 根据数据更新频率设置TTL
2. **避免缓存穿透**: 使用`cache-null-values=false`
3. **缓存预热**: 系统启动时预加载热点数据
4. **监控缓存命中率**: 定期检查缓存效果
5. **及时清除失效缓存**: 数据更新时清除相关缓存

## 🔧 故障排查

### 问题1: 缓存没有生效
- 检查Redis是否启动
- 确认`@EnableCaching`注解已添加
- 查看是否有异常日志

### 问题2: 序列化错误
- 确保实体类可序列化
- 检查Jackson配置

### 问题3: 缓存不过期
- 检查TTL配置
- 确认Redis内存策略

现在您的缓存已经完全配置为存储到Redis中了！🎉
