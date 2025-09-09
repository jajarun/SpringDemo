package com.example.controller;

import com.example.entity.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")  // 管理员路由分组
public class AdminController {

    @Autowired
    private UserService userService;

    // GET /api/v1/admin/users/stats - 获取用户统计信息
    @GetMapping("/users/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        List<User> users = userService.getAllUsers();
        Map<String, Object> stats = Map.of(
            "totalUsers", users.size(),
            "message", "管理员统计信息"
        );
        return ResponseEntity.ok(stats);
    }

    // GET /api/v1/admin/users/with-phone - 获取有电话号码的用户
    @GetMapping("/users/with-phone")
    public List<User> getUsersWithPhone() {
        return userService.getAllUsers().stream()
                .filter(user -> user.getPhone() != null && !user.getPhone().isEmpty())
                .toList();
    }

    // DELETE /api/v1/admin/users/batch - 批量删除用户
    @DeleteMapping("/users/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteUsers(@RequestBody List<Long> userIds) {
        int deletedCount = 0;
        for (Long id : userIds) {
            if (userService.deleteUser(id)) {
                deletedCount++;
            }
        }
        Map<String, Object> result = Map.of(
            "deletedCount", deletedCount,
            "totalRequested", userIds.size()
        );
        return ResponseEntity.ok(result);
    }
} 