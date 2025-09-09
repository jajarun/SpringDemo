# Spring Boot 动态SQL查询实现方案

本项目展示了在Spring Boot中实现动态SQL查询的几种方法，包括Criteria API、自定义Repository和原生SQL等。

## 📋 目录

1. [方案概述](#方案概述)
2. [Criteria API 方案](#criteria-api-方案)
3. [自定义Repository方案](#自定义repository方案)
4. [原生SQL方案](#原生sql方案)
5. [API使用示例](#api使用示例)
6. [性能对比](#性能对比)

## 🔍 方案概述

### 1. Criteria API (推荐)
- **优点**: 类型安全、易于维护、支持复杂查询
- **缺点**: 学习成本稍高
- **适用场景**: 复杂的动态查询条件

### 2. 自定义Repository
- **优点**: 灵活性高、可以混合使用JPQL和原生SQL
- **缺点**: 代码量较多
- **适用场景**: 需要复杂业务逻辑的查询

### 3. 原生SQL
- **优点**: 性能最高、可以使用数据库特有功能
- **缺点**: 数据库依赖、类型不安全
- **适用场景**: 性能要求极高或需要特殊SQL功能

## 🚀 Criteria API 方案

### 核心文件
- `UserSpecifications.java` - 定义查询规格
- `UserRepository.java` - 继承 `JpaSpecificationExecutor`

### 使用示例

```java
// 1. 简单条件查询
Specification<User> spec = UserSpecifications.nameContains("张");
List<User> users = userRepository.findAll(spec);

// 2. 组合条件查询
Specification<User> spec = Specification.where(null)
    .and(UserSpecifications.nameContains("张"))
    .and(UserSpecifications.hasPhone());
List<User> users = userRepository.findAll(spec);

// 3. 分页查询
Page<User> userPage = userRepository.findAll(spec, pageable);
```

### API调用示例

```bash
# 根据名称模糊查询
GET /api/users/search?name=张

# 组合条件查询
GET /api/users/search?name=张&email=test@example.com&startTime=2023-01-01T00:00:00

# 分页查询
GET /api/users/search/paginated?name=张&page=0&size=10&sortBy=createdAt&sortDir=desc
```

## 🛠️ 自定义Repository方案

### 核心文件
- `CustomUserRepository.java` - 自定义接口
- `CustomUserRepositoryImpl.java` - 实现类

### 使用示例

```java
// 使用Map传递动态条件
Map<String, Object> conditions = new HashMap<>();
conditions.put("name", "张");
conditions.put("hasPhone", true);
conditions.put("keyword", "test");

List<User> users = userRepository.findUsersByDynamicConditions(conditions);
```

### API调用示例

```bash
# 自定义条件查询
GET /api/users/search/custom?name=张&keyword=test&hasPhone=true

# 自定义分页查询
GET /api/users/search/custom/paginated?name=张&keyword=test&page=0&size=5
```

## 💾 原生SQL方案

### 使用示例

```java
// 动态构建原生SQL
public List<User> findUsersByNativeSql(String name, String email, String phone, 
                                      LocalDateTime startTime, LocalDateTime endTime) {
    StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
    List<Object> parameters = new ArrayList<>();
    int paramIndex = 1;

    if (StringUtils.hasText(name)) {
        sql.append(" AND name LIKE ?").append(paramIndex);
        parameters.add("%" + name + "%");
        paramIndex++;
    }
    // ... 其他条件
}
```

### API调用示例

```bash
# 原生SQL查询
GET /api/users/search/native?name=张&email=test@example.com
```

## 📊 API使用示例

### 1. 基础查询

```bash
# 获取所有用户
GET /api/users

# 根据ID获取用户
GET /api/users/1

# 创建用户
POST /api/users
Content-Type: application/json
{
    "name": "张三",
    "email": "zhangsan@example.com",
    "phone": "13800138000"
}
```

### 2. 动态查询

```bash
# 使用Specification进行动态查询
GET /api/users/search?name=张&email=test@example.com&phone=123&startTime=2023-01-01T00:00:00&endTime=2023-12-31T23:59:59

# 分页动态查询
GET /api/users/search/paginated?name=张&page=0&size=10&sortBy=createdAt&sortDir=desc

# 组合多个条件查询
GET /api/users/search/multiple?name=张&hasPhone=true&keyword=test

# 关键词搜索
GET /api/users/search/keyword?q=张三

# 时间范围查询
GET /api/users/search/timerange?startTime=2023-01-01T00:00:00&endTime=2023-12-31T23:59:59

# 获取有电话号码的用户
GET /api/users/with-phone
```

### 3. 响应示例

```json
{
    "content": [
        {
            "id": 1,
            "name": "张三",
            "email": "zhangsan@example.com",
            "phone": "13800138000",
            "createdAt": "2023-12-01T10:00:00",
            "updatedAt": "2023-12-01T10:00:00"
        }
    ],
    "pageable": {
        "sort": {
            "sorted": true,
            "unsorted": false
        },
        "pageNumber": 0,
        "pageSize": 10
    },
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
}
```

## 🔧 高级用法

### 1. 复杂条件组合

```java
// 在Service中组合多个Specification
public List<User> complexSearch(String name, String email, boolean hasPhone, String keyword) {
    Specification<User> spec = Specification.where(null);
    
    if (StringUtils.hasText(name)) {
        spec = spec.and(UserSpecifications.nameContains(name));
    }
    
    if (StringUtils.hasText(email)) {
        spec = spec.and(UserSpecifications.emailEquals(email));
    }
    
    if (hasPhone) {
        spec = spec.and(UserSpecifications.hasPhone());
    }
    
    if (StringUtils.hasText(keyword)) {
        spec = spec.and(UserSpecifications.searchByKeyword(keyword));
    }
    
    return userRepository.findAll(spec);
}
```

### 2. 动态排序

```java
// 支持多字段排序
Sort sort = Sort.by(
    Sort.Order.desc("createdAt"),
    Sort.Order.asc("name")
);

Pageable pageable = PageRequest.of(0, 10, sort);
```

### 3. 投影查询

```java
// 只查询特定字段
@Query("SELECT new com.example.dto.UserDTO(u.name, u.email) FROM User u WHERE u.name LIKE %:name%")
List<UserDTO> findUserProjection(@Param("name") String name);
```

## ⚡ 性能对比

| 方案 | 性能 | 类型安全 | 维护性 | 学习成本 |
|------|------|----------|--------|----------|
| Criteria API | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 自定义Repository | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |
| 原生SQL | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐ | ⭐ |

## 📝 最佳实践

1. **优先使用Criteria API**: 对于大多数动态查询场景
2. **合理使用索引**: 确保查询字段有适当的数据库索引
3. **避免N+1问题**: 使用`@EntityGraph`或JOIN FETCH
4. **分页查询**: 对于大数据量查询，始终使用分页
5. **缓存策略**: 对于频繁查询的数据，考虑使用缓存

## 🚀 扩展功能

### 1. 添加新的查询条件

在`UserSpecifications`中添加新方法：

```java
public static Specification<User> ageGreaterThan(Integer age) {
    return (root, query, criteriaBuilder) -> {
        if (age == null) {
            return criteriaBuilder.conjunction();
        }
        return criteriaBuilder.greaterThan(root.get("age"), age);
    };
}
```

### 2. 支持更多数据类型

```java
// 支持日期范围查询
public static Specification<User> birthdateBetween(LocalDate startDate, LocalDate endDate) {
    return (root, query, criteriaBuilder) -> {
        List<Predicate> predicates = new ArrayList<>();
        
        if (startDate != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("birthdate"), startDate));
        }
        
        if (endDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("birthdate"), endDate));
        }
        
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
}
```

这个实现提供了完整的动态SQL查询解决方案，您可以根据具体需求选择合适的方法。 