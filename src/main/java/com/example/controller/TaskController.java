package com.example.controller;

import com.example.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    
    @Autowired
    private TaskService taskService;
    
    /**
     * 触发异步任务
     */
    @PostMapping("/async")
    public ResponseEntity<Map<String, Object>> triggerAsyncTask(@RequestParam String taskName) {
        logger.info("收到异步任务请求: {}", taskName);
        
        CompletableFuture<String> future = taskService.triggerManualTask(taskName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "异步任务已启动");
        response.put("taskName", taskName);
        response.put("status", "running");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 触发批量异步任务
     */
    @PostMapping("/async/batch")
    public ResponseEntity<Map<String, Object>> triggerBatchAsyncTask(@RequestParam(defaultValue = "5") int batchSize) {
        logger.info("收到批量异步任务请求，批次大小: {}", batchSize);
        
        CompletableFuture<Void> future = taskService.batchAsyncTask(batchSize);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "批量异步任务已启动");
        response.put("batchSize", batchSize);
        response.put("status", "running");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取任务统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTaskStatistics() {
        String statistics = taskService.getTaskStatistics();
        
        Map<String, Object> response = new HashMap<>();
        response.put("statistics", statistics);
        response.put("message", "任务统计信息获取成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取任务说明文档
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getTaskInfo() {
        Map<String, Object> taskInfo = new HashMap<>();
        
        // 定时任务信息
        Map<String, String> scheduledTasks = new HashMap<>();
        scheduledTasks.put("fixedRateTask", "固定频率任务 - 每5秒执行一次");
        scheduledTasks.put("fixedDelayTask", "固定延迟任务 - 上次执行完成后延迟3秒再执行");
        scheduledTasks.put("cronTask", "Cron定时任务 - 每分钟执行一次");
        scheduledTasks.put("dailyTask", "每日任务 - 每天上午10点执行");
        scheduledTasks.put("residentTask", "常驻任务 - 每10秒执行，应用启动5秒后开始");
        scheduledTasks.put("monitoringTask", "监控任务 - 每30秒检查系统状态");
        
        // API接口信息
        Map<String, String> apiEndpoints = new HashMap<>();
        apiEndpoints.put("POST /api/v1/tasks/async", "触发异步任务，参数: taskName");
        apiEndpoints.put("POST /api/v1/tasks/async/batch", "触发批量异步任务，参数: batchSize");
        apiEndpoints.put("GET /api/v1/tasks/statistics", "获取任务统计信息");
        apiEndpoints.put("GET /api/v1/tasks/info", "获取任务说明文档");
        
        // Cron表达式说明
        Map<String, String> cronExamples = new HashMap<>();
        cronExamples.put("0 * * * * ?", "每分钟执行");
        cronExamples.put("0 0 * * * ?", "每小时执行");
        cronExamples.put("0 0 0 * * ?", "每天午夜执行");
        cronExamples.put("0 0 10 * * ?", "每天上午10点执行");
        cronExamples.put("0 0 10 * * MON-FRI", "工作日上午10点执行");
        cronExamples.put("0 0/30 * * * ?", "每30分钟执行");
        
        taskInfo.put("scheduledTasks", scheduledTasks);
        taskInfo.put("apiEndpoints", apiEndpoints);
        taskInfo.put("cronExamples", cronExamples);
        taskInfo.put("message", "Spring任务管理系统说明");
        
        return ResponseEntity.ok(taskInfo);
    }
} 