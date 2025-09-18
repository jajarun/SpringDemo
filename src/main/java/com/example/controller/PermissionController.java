package com.example.controller;

import com.example.entity.Permission;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 权限控制器 - 演示READ权限的使用方式
 */
@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {
    
    // ==================== READ权限判断示例 ====================
    
    /**
     * 方式1：使用hasAuthority()判断READ权限
     * 只有拥有READ权限的用户才能访问
     */
    @GetMapping("/data")
    @PreAuthorize("hasAuthority('READ')")
    public ResponseEntity<String> readData() {
        return ResponseEntity.ok("这是需要READ权限才能访问的数据");
    }
    
    /**
     * 方式2：使用hasAuthority()判断特定资源的READ权限
     * 只有拥有READ_USER权限的用户才能访问
     */
    @GetMapping("/users/data")
    @PreAuthorize("hasAuthority('READ_USER')")
    public ResponseEntity<String> readUserData() {
        return ResponseEntity.ok("这是需要READ_USER权限才能访问的用户数据");
    }
    
    /**
     * 方式3：组合权限判断 - READ或ADMIN权限都可以访问
     */
    @GetMapping("/protected-data")
    @PreAuthorize("hasAuthority('READ') or hasAuthority('ADMIN')")
    public ResponseEntity<String> readProtectedData() {
        return ResponseEntity.ok("拥有READ权限或ADMIN权限都可以访问此数据");
    }
    
    /**
     * 方式4：角色和权限组合判断
     * 拥有ADMIN角色或READ权限的用户都可以访问
     */
    @GetMapping("/mixed-access")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ')")
    public ResponseEntity<String> mixedAccess() {
        return ResponseEntity.ok("ADMIN角色或READ权限都可以访问");
    }
    
    /**
     * 方式5：多个权限的AND条件
     * 必须同时拥有READ和WRITE权限
     */
    @GetMapping("/read-write-data")
    @PreAuthorize("hasAuthority('READ') and hasAuthority('WRITE')")
    public ResponseEntity<String> readWriteData() {
        return ResponseEntity.ok("需要同时拥有READ和WRITE权限");
    }
    
    // ==================== 权限管理接口 ====================
    
    /**
     * 获取所有可用权限列表 - 需要ADMIN权限
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAllPermissions() {
        List<String> permissions = Arrays.asList(
            Permission.Names.READ,
            Permission.Names.WRITE,
            Permission.Names.UPDATE,
            Permission.Names.DELETE,
            Permission.Names.READ_USER,
            Permission.Names.WRITE_USER,
            Permission.Names.UPDATE_USER,
            Permission.Names.DELETE_USER,
            Permission.Names.READ_PRODUCT,
            Permission.Names.WRITE_PRODUCT,
            Permission.Names.UPDATE_PRODUCT,
            Permission.Names.DELETE_PRODUCT
        );
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * 检查当前用户是否有指定权限
     */
    @GetMapping("/check/{permission}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> checkPermission(@PathVariable String permission) {
        // 这个方法本身只需要认证，具体权限检查在方法内部
        // 实际项目中可以通过SecurityContextHolder获取当前用户权限进行检查
        return ResponseEntity.ok("权限检查功能 - 检查权限: " + permission);
    }
    
    // ==================== 不同权限级别的示例 ====================
    
    /**
     * 公开数据 - 无需权限
     */
    @GetMapping("/public")
    public ResponseEntity<String> publicData() {
        return ResponseEntity.ok("公开数据，无需权限即可访问");
    }
    
    /**
     * 需要认证但不需要特定权限
     */
    @GetMapping("/authenticated")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> authenticatedData() {
        return ResponseEntity.ok("需要登录认证，但不需要特定权限");
    }
    
    /**
     * 需要特定权限的敏感数据
     */
    @GetMapping("/sensitive")
    @PreAuthorize("hasAuthority('READ') and hasRole('ADMIN')")
    public ResponseEntity<String> sensitiveData() {
        return ResponseEntity.ok("敏感数据 - 需要READ权限且必须是ADMIN角色");
    }
}
