package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final AtomicInteger taskCounter = new AtomicInteger(0);
    
    @Autowired
    private UserService userService;
    
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
    public CompletableFuture<String> asyncTask(String taskName) {
        logger.info("异步任务开始执行: {}", taskName);
        
        try {
            // 模拟长时间运行的任务
            Thread.sleep(3000);
            
            String result = String.format("任务 %s 执行完成，线程: %s", 
                taskName, Thread.currentThread().getName());
            
            logger.info("异步任务完成: {}", result);
            return CompletableFuture.completedFuture(result);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("异步任务被中断: {}", taskName);
            return CompletableFuture.completedFuture("任务被中断");
        }
    }
    
    /**
     * 批量异步任务处理
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> batchAsyncTask(int batchSize) {
        logger.info("批量异步任务开始，批次大小: {}", batchSize);
        
        for (int i = 0; i < batchSize; i++) {
            try {
                // 模拟处理每个任务项
                Thread.sleep(500);
                logger.info("处理批次项 {}/{}", i + 1, batchSize);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        logger.info("批量异步任务完成");
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
     * 手动触发的异步任务
     */
    public CompletableFuture<String> triggerManualTask(String taskData) {
        return asyncTask("手动触发任务: " + taskData);
    }
    
    /**
     * 获取任务统计信息
     */
    public String getTaskStatistics() {
        return String.format("任务统计 - 总执行次数: %d, 当前时间: %s", 
            taskCounter.get(),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
} 