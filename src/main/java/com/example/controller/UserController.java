package com.example.controller;

import com.example.entity.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // 创建用户 - 需要管理员权限
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }
    
    // 获取所有用户 - 需要认证
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    // 根据ID获取用户 - 需要认证
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    // 更新用户 - 需要管理员权限
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }
    
    // 删除用户 - 需要管理员权限
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ==================== 动态查询API ====================

    /**
     * 使用Specification进行动态查询 - 需要认证
     * GET /api/users/search?name=张&email=test@example.com&phone=123&startTime=2023-01-01T00:00:00&endTime=2023-12-31T23:59:59
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<User> users = userService.findUsersBySpecification(name, email, phone, startTime, endTime);
        return ResponseEntity.ok(users);
    }

    /**
     * 使用Specification进行分页动态查询 - 需要认证
     * GET /api/users/search/paginated?name=张&page=0&size=10&sort=createdAt,desc
     */
    @GetMapping("/search/paginated")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<User>> searchUsersWithPagination(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> users = userService.findUsersBySpecificationWithPagination(
                name, email, phone, startTime, endTime, pageable);
        
        return ResponseEntity.ok(users);
    }

    /**
     * 组合多个条件查询 - 需要认证
     * GET /api/users/search/multiple?name=张&hasPhone=true&keyword=test
     */
    @GetMapping("/search/multiple")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<User>> searchUsersByMultipleConditions(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "false") boolean hasPhone,
            @RequestParam(required = false) String keyword) {
        
        List<User> users = userService.findUsersByMultipleConditions(name, email, hasPhone, keyword);
        return ResponseEntity.ok(users);
    }

    /**
     * 使用自定义Repository进行动态查询 - 需要认证
     * GET /api/users/search/custom?name=张&keyword=test&hasPhone=true
     */
    @GetMapping("/search/custom")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<User>> searchUsersByCustomRepository(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean hasPhone) {
        
        List<User> users = userService.findUsersByDynamicConditions(
                name, email, phone, startTime, endTime, keyword, hasPhone);
        
        return ResponseEntity.ok(users);
    }

    /**
     * 使用自定义Repository进行分页动态查询 - 需要认证
     * GET /api/users/search/custom/paginated?name=张&keyword=test&page=0&size=5
     */
    @GetMapping("/search/custom/paginated")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<User>> searchUsersByCustomRepositoryWithPagination(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean hasPhone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> users = userService.findUsersByDynamicConditionsWithPagination(
                name, email, phone, startTime, endTime, keyword, hasPhone, pageable);
        
        return ResponseEntity.ok(users);
    }

    /**
     * 使用原生SQL进行动态查询 - 需要认证
     * GET /api/users/search/native?name=张&email=test@example.com
     */
    @GetMapping("/search/native")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<User>> searchUsersByNativeSQL(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<User> users = userService.findUsersByNativeSql(name, email, phone, startTime, endTime);
        return ResponseEntity.ok(users);
    }

    /**
     * 根据关键词搜索 - 需要认证
     * GET /api/users/search/keyword?q=张三
     */
    @GetMapping("/search/keyword")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<User>> searchUsersByKeyword(@RequestParam("q") String keyword) {
        List<User> users = userService.searchUsersByKeyword(keyword);
        return ResponseEntity.ok(users);
    }

    /**
     * 根据时间范围查询 - 需要认证
     * GET /api/users/search/timerange?startTime=2023-01-01T00:00:00&endTime=2023-12-31T23:59:59
     */
    @GetMapping("/search/timerange")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<User>> searchUsersByTimeRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<User> users = userService.findUsersByTimeRange(startTime, endTime);
        return ResponseEntity.ok(users);
    }

    /**
     * 获取有电话号码的用户 - 需要认证
     * GET /api/users/with-phone
     */
    @GetMapping("/with-phone")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsersWithPhone() {
        List<User> users = userService.getUsersWithPhone();
        return ResponseEntity.ok(users);
    }
} 