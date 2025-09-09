package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;

@RestController
@SpringBootApplication
@EnableScheduling  // 启用定时任务功能
public class MyApplication {

	@RequestMapping("/")
	String home() {
		return "Hello World! Spring Boot 任务管理演示！访问以下路径查看不同功能：\n" +
				"- 用户API: /api/v1/users\n" +
				"- 管理员API: /api/v1/admin\n" +
				"- 公共API: /api/v1/public\n" +
				"- 认证API: /api/v1/auth\n" +
				"- 任务管理API: /api/v1/tasks (定时任务、异步任务、常驻任务)\n" +
				"\n任务管理API示例：\n" +
				"- POST /api/v1/tasks/async?taskName=测试任务 - 触发异步任务\n" +
				"- POST /api/v1/tasks/async/batch?batchSize=10 - 触发批量异步任务\n" +
				"- GET /api/v1/tasks/statistics - 获取任务统计信息\n" +
				"- GET /api/v1/tasks/info - 获取任务说明文档\n" +
				"\n定时任务自动运行中：\n" +
				"- 固定频率任务：每5秒执行\n" +
				"- 固定延迟任务：每次完成后延迟3秒\n" +
				"- Cron任务：每分钟执行\n" +
				"- 常驻任务：每10秒执行\n" +
				"- 监控任务：每30秒检查系统状态";
	}

	public static void main(String[] args) {
		SpringApplication.run(MyApplication.class, args);
	}
}