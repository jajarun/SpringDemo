package com.example.repository;

import com.example.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CustomUserRepository {
    
    /**
     * 根据动态条件查询用户
     */
    List<User> findUsersByDynamicConditions(Map<String, Object> conditions);
    
    /**
     * 根据动态条件分页查询用户
     */
    Page<User> findUsersByDynamicConditionsWithPagination(Map<String, Object> conditions, Pageable pageable);
    
    /**
     * 使用原生SQL进行动态查询
     */
    List<User> findUsersByNativeSql(String name, String email, String phone, 
                                   LocalDateTime startTime, LocalDateTime endTime);
} 