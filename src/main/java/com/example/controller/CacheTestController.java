package com.example.controller;

import com.example.entity.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 缓存测试控制器
 * 演示Redis缓存的使用和管理
 */
@RestController
@RequestMapping("/api/cache")
public class CacheTestController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 测试用户邮箱缓存（使用Repository中的缓存）
     * 第一次查询会从数据库获取并缓存到Redis
     * 后续查询直接从Redis获取
     */
    @GetMapping("/user/email/{email}")
    public Map<String, Object> getUserByEmail(@PathVariable String email) {
        long startTime = System.currentTimeMillis();
        
        Optional<User> user = userRepository.findByEmail(email);
        
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("user", user.orElse(null));
        result.put("queryTime", endTime - startTime + "ms");
        result.put("source", "查询时间可判断是否来自缓存（<10ms通常是缓存）");
        
        return result;
    }

    /**
     * 缓存所有用户列表
     */
    @GetMapping("/users")
    @Cacheable(value = "userList", key = "'all'")
    public Map<String, Object> getAllUsers() {
        long startTime = System.currentTimeMillis();
        
        List<User> users = userRepository.findAll();
        
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("count", users.size());
        result.put("queryTime", endTime - startTime + "ms");
        result.put("source", "第一次查询数据库，后续查询Redis缓存");
        
        return result;
    }

    /**
     * 缓存用户搜索结果
     */
    @GetMapping("/users/search")
    @Cacheable(value = "userSearch", key = "#keyword")
    public Map<String, Object> searchUsers(@RequestParam String keyword) {
        long startTime = System.currentTimeMillis();
        
        List<User> users = userRepository.searchByKeyword(keyword);
        
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("keyword", keyword);
        result.put("count", users.size());
        result.put("queryTime", endTime - startTime + "ms");
        
        return result;
    }

    /**
     * 清除特定用户的邮箱缓存
     */
    @DeleteMapping("/user/email/{email}")
    @CacheEvict(value = "findUsersByEmail", key = "#email")
    public Map<String, String> evictUserEmailCache(@PathVariable String email) {
        Map<String, String> result = new HashMap<>();
        result.put("message", "已清除邮箱缓存: " + email);
        result.put("status", "success");
        return result;
    }

    /**
     * 清除所有用户列表缓存
     */
    @DeleteMapping("/users")
    @CacheEvict(value = "userList", allEntries = true)
    public Map<String, String> evictAllUserListCache() {
        Map<String, String> result = new HashMap<>();
        result.put("message", "已清除所有用户列表缓存");
        result.put("status", "success");
        return result;
    }

    /**
     * 查看Redis中的缓存键
     */
    @GetMapping("/redis/keys")
    public Map<String, Object> getRedisKeys() {
        Set<String> keys = redisTemplate.keys("*");
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalKeys", keys != null ? keys.size() : 0);
        result.put("keys", keys);
        result.put("cacheNames", cacheManager.getCacheNames());
        
        return result;
    }

    /**
     * 查看特定缓存的统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取所有缓存名称
        stats.put("availableCaches", cacheManager.getCacheNames());
        
        // 获取Redis键统计
        Set<String> allKeys = redisTemplate.keys("*");
        stats.put("totalRedisKeys", allKeys != null ? allKeys.size() : 0);
        
        // 按前缀分组统计
        Map<String, Integer> keysByPrefix = new HashMap<>();
        if (allKeys != null) {
            for (String key : allKeys) {
                String prefix = key.split("::")[0];
                keysByPrefix.merge(prefix, 1, Integer::sum);
            }
        }
        stats.put("keysByPrefix", keysByPrefix);
        
        return stats;
    }

    /**
     * 手动预热缓存
     */
    @PostMapping("/warmup")
    public Map<String, Object> warmupCache() {
        long startTime = System.currentTimeMillis();
        
        // 预热用户列表缓存
        getAllUsers();
        
        // 预热一些常用的用户邮箱缓存
        List<User> users = userRepository.findAll();
        int warmedUp = 0;
        for (User user : users.subList(0, Math.min(10, users.size()))) {
            if (user.getEmail() != null) {
                userRepository.findByEmail(user.getEmail());
                warmedUp++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "缓存预热完成");
        result.put("warmedUpUsers", warmedUp);
        result.put("totalTime", (endTime - startTime) + "ms");
        result.put("status", "success");
        
        return result;
    }
}
