# READ权限判断完整指南

## 概述

本指南详细说明了在Spring Security中如何实现和判断READ权限，包括角色权限和细粒度权限的区别与使用。

## 权限系统架构

### 1. 角色 vs 权限
- **角色（Role）**：粗粒度的权限分组，如 `ADMIN`、`USER`、`MODERATOR`
- **权限（Permission）**：细粒度的具体操作权限，如 `READ`、`WRITE`、`DELETE`

### 2. 存储格式
- **角色存储**：`ROLE_ADMIN`、`ROLE_USER`（自动添加ROLE_前缀）
- **权限存储**：`READ`、`WRITE`、`READ_USER`（不添加前缀）

## READ权限判断方式

### 方式1：使用hasAuthority()判断细粒度权限

```java
@GetMapping("/data")
@PreAuthorize("hasAuthority('READ')")
public ResponseEntity<String> readData() {
    return ResponseEntity.ok("需要READ权限的数据");
}
```

### 方式2：资源特定的READ权限

```java
@GetMapping("/users")
@PreAuthorize("hasAuthority('READ_USER')")
public ResponseEntity<List<User>> getUsers() {
    // 只有拥有READ_USER权限的用户才能访问
}
```

### 方式3：组合权限判断

```java
// OR条件：READ权限或ADMIN权限都可以
@PreAuthorize("hasAuthority('READ') or hasAuthority('ADMIN')")

// AND条件：必须同时拥有READ和WRITE权限
@PreAuthorize("hasAuthority('READ') and hasAuthority('WRITE')")

// 角色和权限混合：ADMIN角色或READ权限
@PreAuthorize("hasRole('ADMIN') or hasAuthority('READ')")
```

### 方式4：在SecurityConfig中配置URL级别权限

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authz -> authz
        .requestMatchers(HttpMethod.GET, "/api/v1/data/**").hasAuthority("READ")
        .requestMatchers(HttpMethod.POST, "/api/v1/data/**").hasAuthority("WRITE")
        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
    );
    return http.build();
}
```

## 权限检查方法对比

| 方法 | 用途 | 示例 | 说明 |
|-----|------|------|------|
| `hasRole()` | 检查角色 | `hasRole('ADMIN')` | 自动添加ROLE_前缀 |
| `hasAuthority()` | 检查权限 | `hasAuthority('READ')` | 不添加前缀，直接匹配 |
| `hasAnyRole()` | 检查多个角色 | `hasAnyRole('ADMIN','USER')` | 任意一个角色匹配即可 |
| `hasAnyAuthority()` | 检查多个权限 | `hasAnyAuthority('READ','WRITE')` | 任意一个权限匹配即可 |

## 实际使用示例

### 1. 用户数据访问权限控制

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    // 读取用户列表 - 需要READ_USER权限
    @GetMapping
    @PreAuthorize("hasAuthority('READ_USER')")
    public List<User> getUsers() { }
    
    // 创建用户 - 需要WRITE_USER权限
    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_USER')")
    public User createUser(@RequestBody User user) { }
    
    // 更新用户 - 需要UPDATE_USER权限
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public User updateUser(@PathVariable Long id, @RequestBody User user) { }
    
    // 删除用户 - 需要DELETE_USER权限或ADMIN角色
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_USER') or hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) { }
}
```

### 2. 动态权限检查

```java
@Service
public class PermissionService {
    
    public boolean hasReadPermission(String resource) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("READ_" + resource.toUpperCase()));
    }
    
    public boolean canAccessResource(String operation, String resource) {
        String permission = operation.toUpperCase() + "_" + resource.toUpperCase();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(permission));
    }
}
```

## 权限初始化示例

在DataInitializer中初始化权限数据：

```java
@Component
public class DataInitializer {
    
    @PostConstruct
    public void initPermissions() {
        // 创建基础权限
        createPermissionIfNotExists("READ", "读取权限");
        createPermissionIfNotExists("WRITE", "写入权限");
        createPermissionIfNotExists("UPDATE", "更新权限");
        createPermissionIfNotExists("DELETE", "删除权限");
        
        // 创建资源特定权限
        createPermissionIfNotExists("READ_USER", "读取用户权限");
        createPermissionIfNotExists("WRITE_USER", "创建用户权限");
        createPermissionIfNotExists("UPDATE_USER", "更新用户权限");
        createPermissionIfNotExists("DELETE_USER", "删除用户权限");
        
        // 为用户分配权限
        assignPermissionsToUser("admin@example.com", 
            Arrays.asList("READ", "WRITE", "UPDATE", "DELETE", 
                         "READ_USER", "WRITE_USER", "UPDATE_USER", "DELETE_USER"));
        
        assignPermissionsToUser("user@example.com", 
            Arrays.asList("READ", "READ_USER"));
    }
}
```

## 测试READ权限

### 1. 获取JWT Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }'
```

### 2. 测试READ权限接口
```bash
# 测试基础READ权限
curl -X GET http://localhost:8080/api/v1/permissions/data \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 测试READ_USER权限
curl -X GET http://localhost:8080/api/v1/permissions/users/data \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 测试组合权限
curl -X GET http://localhost:8080/api/v1/permissions/mixed-access \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 常见问题

### Q1: 为什么角色要加ROLE_前缀，权限不用？
A1: 这是Spring Security的约定：
- 角色用于粗粒度控制，统一加ROLE_前缀便于区分
- 权限用于细粒度控制，直接使用名称更灵活

### Q2: hasRole()和hasAuthority()的区别？
A2: 
- `hasRole('ADMIN')` 等价于 `hasAuthority('ROLE_ADMIN')`
- `hasAuthority('READ')` 直接检查READ权限
- hasRole()会自动添加ROLE_前缀，hasAuthority()不会

### Q3: 如何实现动态权限控制？
A3: 可以通过自定义PermissionEvaluator或在Service层进行权限检查：

```java
@PreAuthorize("@permissionService.hasReadPermission(#resource)")
public void readResource(String resource) { }
```

## 最佳实践

1. **权限命名规范**：使用`操作_资源`格式，如`READ_USER`、`WRITE_PRODUCT`
2. **权限粒度**：根据业务需求确定合适的权限粒度
3. **权限继承**：ADMIN角色通常拥有所有权限
4. **权限缓存**：对频繁检查的权限进行缓存优化
5. **权限审计**：记录权限变更和访问日志
