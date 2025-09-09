package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulingConfig {
    
    /**
     * 配置定时任务专用的线程池
     * 与异步任务的线程池分开，避免相互影响
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);                      // 线程池大小
        scheduler.setThreadNamePrefix("Scheduled-");    // 线程名前缀
        scheduler.setAwaitTerminationSeconds(60);       // 等待任务完成的时间
        scheduler.setWaitForTasksToCompleteOnShutdown(true); // 关闭时等待任务完成
        scheduler.initialize();
        return scheduler;
    }
} 