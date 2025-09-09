package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final AtomicInteger taskCounter = new AtomicInteger(0);
    
    // 任务状态跟踪
    private final ConcurrentHashMap<String, TaskInfo> taskStatus = new ConcurrentHashMap<>();
    
    // 任务信息类
    public static class TaskInfo {
        private final String taskId;
        private final String taskName;
        private final LocalDateTime startTime;
        private String status; // RUNNING, COMPLETED, FAILED
        private String result;
        private LocalDateTime endTime;
        
        public TaskInfo(String taskId, String taskName) {
            this.taskId = taskId;
            this.taskName = taskName;
            this.startTime = LocalDateTime.now();
            this.status = "RUNNING";
        }
        
        // Getters and setters
        public String getTaskId() { return taskId; }
        public String getTaskName() { return taskName; }
        public LocalDateTime getStartTime() { return startTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }
    
    // ==================== 定时任务示例 ====================
    
    /**
     * 固定频率执行 - 每5秒执行一次
     * fixedRate: 从上次开始执行时间点开始计算，每隔5秒执行一次
     */
    @Scheduled(fixedRate = 5000)
    public void fixedRateTask() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logger.info("固定频率任务执行 - 当前时间: {}", time);
    }
    
    /**
     * 固定延迟执行 - 上次执行完成后延迟3秒再执行
     * fixedDelay: 从上次执行完毕时间点开始计算，延迟3秒执行
     */
    @Scheduled(fixedDelay = 3000)
    public void fixedDelayTask() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logger.info("固定延迟任务执行 - 当前时间: {}", time);
        
        // 模拟任务执行时间
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Cron表达式定时任务 - 每分钟的第0秒执行
     * Cron表达式格式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 * * * * ?")
    public void cronTask() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logger.info("Cron定时任务执行 - 每分钟执行一次: {}", time);
    }
    
    /**
     * 复杂的Cron表达式示例 - 每天上午10点执行
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void dailyTask() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logger.info("每日定时任务执行 - 上午10点: {}", time);
        
        // 执行数据清理、报表生成等任务
        performDailyMaintenance();
    }
    
    // ==================== 异步任务示例 ====================
    
    /**
     * 异步任务 - 不阻塞主线程
     */
    @Async("taskExecutor")
    public CompletableFuture<String> asyncTask(String taskName, String taskId) {
        logger.info("异步任务开始执行: {} (ID: {})", taskName, taskId);
        
        TaskInfo taskInfo = taskStatus.get(taskId);
        if (taskInfo != null) {
            taskInfo.setStatus("RUNNING");
        }
        
        try {
            // 模拟长时间运行的任务
            Thread.sleep(3000);
            
            String result = String.format("任务 %s 执行完成，线程: %s, 任务ID: %s", 
                taskName, Thread.currentThread().getName(), taskId);
            
            logger.info("异步任务完成: {}", result);
            
            // 更新任务状态
            if (taskInfo != null) {
                taskInfo.setStatus("COMPLETED");
                taskInfo.setResult(result);
                taskInfo.setEndTime(LocalDateTime.now());
            }
            
            return CompletableFuture.completedFuture(result);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("异步任务被中断: {} (ID: {})", taskName, taskId);
            
            // 更新任务状态为失败
            if (taskInfo != null) {
                taskInfo.setStatus("FAILED");
                taskInfo.setResult("任务被中断");
                taskInfo.setEndTime(LocalDateTime.now());
            }
            
            return CompletableFuture.completedFuture("任务被中断");
        } catch (Exception e) {
            logger.error("异步任务执行异常: {} (ID: {})", taskName, taskId, e);
            
            // 更新任务状态为失败
            if (taskInfo != null) {
                taskInfo.setStatus("FAILED");
                taskInfo.setResult("任务执行异常: " + e.getMessage());
                taskInfo.setEndTime(LocalDateTime.now());
            }
            
            return CompletableFuture.completedFuture("任务执行异常");
        }
    }
    
    /**
     * 批量异步任务处理
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> batchAsyncTask(int batchSize, String taskId) {
        logger.info("批量异步任务开始，批次大小: {} (ID: {})", batchSize, taskId);
        
        TaskInfo taskInfo = taskStatus.get(taskId);
        if (taskInfo != null) {
            taskInfo.setStatus("RUNNING");
        }
        
        try {
            for (int i = 0; i < batchSize; i++) {
                // 模拟处理每个任务项
                Thread.sleep(500);
                logger.info("处理批次项 {}/{} (任务ID: {})", i + 1, batchSize, taskId);
            }
            
            logger.info("批量异步任务完成 (ID: {})", taskId);
            
            // 更新任务状态
            if (taskInfo != null) {
                taskInfo.setStatus("COMPLETED");
                taskInfo.setResult(String.format("批量任务完成，处理了 %d 个项目", batchSize));
                taskInfo.setEndTime(LocalDateTime.now());
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("批量异步任务被中断 (ID: {})", taskId);
            
            if (taskInfo != null) {
                taskInfo.setStatus("FAILED");
                taskInfo.setResult("批量任务被中断");
                taskInfo.setEndTime(LocalDateTime.now());
            }
        } catch (Exception e) {
            logger.error("批量异步任务执行异常 (ID: {})", taskId, e);
            
            if (taskInfo != null) {
                taskInfo.setStatus("FAILED");
                taskInfo.setResult("批量任务执行异常: " + e.getMessage());
                taskInfo.setEndTime(LocalDateTime.now());
            }
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    // ==================== 常驻任务示例 ====================
    
    /**
     * 常驻任务 - 应用启动后持续运行
     * 使用@Scheduled结合while循环实现
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void residentTask() {
        int taskId = taskCounter.incrementAndGet();
        logger.info("常驻任务执行 - 任务ID: {}", taskId);
        
        // 执行常驻任务逻辑
        performResidentTaskLogic(taskId);
    }
    
    /**
     * 监控任务 - 定期检查系统状态
     */
    @Scheduled(fixedRate = 30000)
    public void monitoringTask() {
        logger.info("系统监控任务执行");
        
        // 检查内存使用情况
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        logger.info("内存使用情况 - 总内存: {}MB, 已用: {}MB, 空闲: {}MB", 
            totalMemory / 1024 / 1024,
            usedMemory / 1024 / 1024, 
            freeMemory / 1024 / 1024);
        
        // 可以添加其他监控逻辑，如数据库连接检查、外部服务健康检查等
    }
    
    // ==================== 私有方法 ====================
    
    private void performDailyMaintenance() {
        logger.info("执行每日维护任务");
        
        // 示例：清理过期数据
        // userService.cleanExpiredData();
        
        // 示例：生成日报
        // generateDailyReport();
        
        // 示例：数据备份
        // backupData();
    }
    
    private void performResidentTaskLogic(int taskId) {
        try {
            // 模拟常驻任务处理逻辑
            logger.info("执行常驻任务逻辑 - ID: {}", taskId);
            
            // 例如：处理队列中的消息
            // processMessageQueue();
            
            // 例如：同步外部数据
            // syncExternalData();
            
            Thread.sleep(2000); // 模拟处理时间
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("常驻任务被中断 - ID: {}", taskId);
        } catch (Exception e) {
            logger.error("常驻任务执行异常 - ID: {}", taskId, e);
        }
    }
    
    // ==================== 手动触发的任务方法 ====================
    
    /**
     * 手动触发的异步任务 - 返回任务ID
     */
    public String triggerManualTask(String taskData) {
        // 生成唯一任务ID
        String taskId = generateTaskId();
        String taskName = "手动触发任务: " + taskData;
        
        // 创建任务信息并存储
        TaskInfo taskInfo = new TaskInfo(taskId, taskName);
        taskStatus.put(taskId, taskInfo);
        
        // 异步执行任务
        asyncTask(taskName, taskId);
        
        logger.info("手动任务已启动 - 任务ID: {}, 任务名称: {}", taskId, taskName);
        return taskId;
    }
    
    /**
     * 手动触发批量异步任务 - 返回任务ID
     */
    public String triggerBatchTask(int batchSize) {
        // 生成唯一任务ID
        String taskId = generateTaskId();
        String taskName = "批量异步任务 (大小: " + batchSize + ")";
        
        // 创建任务信息并存储
        TaskInfo taskInfo = new TaskInfo(taskId, taskName);
        taskStatus.put(taskId, taskInfo);
        
        // 异步执行批量任务
        batchAsyncTask(batchSize, taskId);
        
        logger.info("批量任务已启动 - 任务ID: {}, 批次大小: {}", taskId, batchSize);
        return taskId;
    }
    
    /**
     * 根据任务ID获取任务状态
     */
    public TaskInfo getTaskStatus(String taskId) {
        return taskStatus.get(taskId);
    }
    
    /**
     * 获取所有任务状态
     */
    public ConcurrentHashMap<String, TaskInfo> getAllTaskStatus() {
        return new ConcurrentHashMap<>(taskStatus);
    }
    
    /**
     * 生成唯一任务ID
     */
    private String generateTaskId() {
        return "TASK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + 
               "-" + System.currentTimeMillis();
    }
    
    /**
     * 获取任务统计信息
     */
    public String getTaskStatistics() {
        long runningTasks = taskStatus.values().stream()
            .filter(task -> "RUNNING".equals(task.getStatus()))
            .count();
        long completedTasks = taskStatus.values().stream()
            .filter(task -> "COMPLETED".equals(task.getStatus()))
            .count();
        long failedTasks = taskStatus.values().stream()
            .filter(task -> "FAILED".equals(task.getStatus()))
            .count();
            
        return String.format("任务统计 - 总任务数: %d, 运行中: %d, 已完成: %d, 失败: %d, 定时任务执行次数: %d, 当前时间: %s", 
            taskStatus.size(), runningTasks, completedTasks, failedTasks, taskCounter.get(),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
} 