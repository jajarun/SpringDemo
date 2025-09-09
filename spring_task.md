# Spring 任务管理使用指南

## 概述

Spring 提供了强大的任务管理功能，支持定时任务、异步任务和常驻任务。本项目演示了这些功能的完整使用方法。

## 任务类型

### 1. 定时任务（@Scheduled）

定时任务会按照预设的时间规律自动执行，适用于：
- 数据清理
- 报表生成
- 系统监控
- 数据同步

#### 定时任务类型

**固定频率执行（fixedRate）**
```java
@Scheduled(fixedRate = 5000)  // 每5秒执行一次
public void fixedRateTask() {
    // 任务逻辑
}
```

**固定延迟执行（fixedDelay）**
```java
@Scheduled(fixedDelay = 3000)  // 上次执行完成后延迟3秒再执行
public void fixedDelayTask() {
    // 任务逻辑
}
```

**Cron表达式定时任务**
```java
@Scheduled(cron = "0 * * * * ?")  // 每分钟执行一次
public void cronTask() {
    // 任务逻辑
}
```

#### 常用Cron表达式

| 表达式 | 说明 |
|--------|------|
| `0 * * * * ?` | 每分钟执行 |
| `0 0 * * * ?` | 每小时执行 |
| `0 0 0 * * ?` | 每天午夜执行 |
| `0 0 10 * * ?` | 每天上午10点执行 |
| `0 0 10 * * MON-FRI` | 工作日上午10点执行 |
| `0 0/30 * * * ?` | 每30分钟执行 |
| `0 0 9-17 * * MON-FRI` | 工作日9-17点每小时执行 |

### 2. 异步任务（@Async）

异步任务不阻塞主线程，适用于：
- 耗时的数据处理
- 文件上传/下载
- 邮件发送
- 外部API调用

```java
@Async("taskExecutor")
public CompletableFuture<String> asyncTask(String taskName) {
    // 异步任务逻辑
    return CompletableFuture.completedFuture(result);
}
```

### 3. 常驻任务

常驻任务是持续运行的后台服务，适用于：
- 消息队列处理
- 实时数据监控
- 长连接维护
- 系统状态检查

```java
@Scheduled(fixedDelay = 10000, initialDelay = 5000)
public void residentTask() {
    // 常驻任务逻辑
}
```

## 配置说明

### 1. 启用定时任务

在主应用类上添加 `@EnableScheduling` 注解：

```java
@SpringBootApplication
@EnableScheduling
public class MyApplication {
    // ...
}
```

### 2. 启用异步任务

在配置类上添加 `@EnableAsync` 注解：

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    // ...
}
```

### 3. 线程池配置

**异步任务线程池**
```java
@Bean(name = "taskExecutor")
public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);        // 核心线程数
    executor.setMaxPoolSize(8);         // 最大线程数
    executor.setQueueCapacity(100);     // 队列容量
    executor.setThreadNamePrefix("Async-");
    executor.initialize();
    return executor;
}
```

**定时任务线程池**
```java
@Bean
public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(10);
    scheduler.setThreadNamePrefix("Scheduled-");
    scheduler.setAwaitTerminationSeconds(60);
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    scheduler.initialize();
    return scheduler;
}
```

## API 接口

### 手动触发异步任务

**触发单个异步任务**
```bash
POST /api/v1/tasks/async?taskName=测试任务
```

**触发批量异步任务**
```bash
POST /api/v1/tasks/async/batch?batchSize=10
```

### 查看任务状态

**获取任务统计信息**
```bash
GET /api/v1/tasks/statistics
```

**获取任务说明文档**
```bash
GET /api/v1/tasks/info
```

## 运行效果

启动应用后，你会在控制台看到以下日志：

```
固定频率任务执行 - 当前时间: 2024-01-01 10:00:00
固定延迟任务执行 - 当前时间: 2024-01-01 10:00:01
Cron定时任务执行 - 每分钟执行一次: 2024-01-01 10:01:00
常驻任务执行 - 任务ID: 1
系统监控任务执行
内存使用情况 - 总内存: 512MB, 已用: 256MB, 空闲: 256MB
```

## 最佳实践

### 1. 任务隔离
- 使用不同的线程池处理不同类型的任务
- 避免长时间运行的任务阻塞其他任务

### 2. 异常处理
```java
@Scheduled(fixedRate = 5000)
public void safeTask() {
    try {
        // 任务逻辑
    } catch (Exception e) {
        logger.error("任务执行异常", e);
    }
}
```

### 3. 条件执行
```java
@Scheduled(fixedRate = 5000)
public void conditionalTask() {
    if (shouldExecute()) {
        // 任务逻辑
    }
}
```

### 4. 性能监控
- 记录任务执行时间
- 监控线程池使用情况
- 设置任务超时时间

### 5. 优雅关闭
```java
@PreDestroy
public void shutdown() {
    // 清理资源
    // 等待任务完成
}
```

## 注意事项

1. **定时任务默认是单线程的**，如果任务执行时间过长，会影响后续任务的执行
2. **异步任务需要返回 `CompletableFuture`** 类型才能真正异步执行
3. **Cron表达式要仔细验证**，错误的表达式可能导致任务不执行或频繁执行
4. **注意内存泄漏**，长时间运行的任务要及时清理资源
5. **线程安全**，多个任务可能同时访问共享资源，需要考虑并发问题

## 扩展功能

### 1. 分布式任务调度
可以集成 Quartz 或 XXL-JOB 实现分布式任务调度

### 2. 任务持久化
将任务信息存储到数据库，支持动态配置

### 3. 任务监控
集成监控系统，实时查看任务执行状态

### 4. 任务重试
实现任务失败重试机制

这个任务管理系统为你提供了完整的定时任务、异步任务和常驻任务解决方案，可以根据具体需求进行扩展和优化。 