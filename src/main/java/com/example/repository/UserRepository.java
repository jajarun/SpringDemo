package com.example.repository;

import com.example.entity.User;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>, CustomUserRepository {
    
    // 根据邮箱查找用户 - 用于认证，不缓存
    Optional<User> findByEmail(String email);
    
    // 根据邮箱查找用户 - 用于其他查询，可缓存
    // @Cacheable("findUsersByEmail")
    default Optional<User> findByEmailCached(String email) {
        return findByEmail(email);
    }
    
    // 根据邮箱检查用户是否存在
    boolean existsByEmail(String email);
    
    // 根据姓名模糊查询
    List<User> findByNameContaining(String name);
    
    // 根据邮箱和姓名查询
    List<User> findByEmailAndName(String email, String name);
    
    // 根据创建时间范围查询
    List<User> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 查询有电话号码的用户
    @Query("SELECT u FROM User u WHERE u.phone IS NOT NULL AND u.phone != ''")
    List<User> findUsersWithPhone();
    
    // 根据关键词搜索用户（姓名或邮箱）
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchByKeyword(@Param("keyword") String keyword);
    
    // 根据时间范围查询用户
    @Query("SELECT u FROM User u WHERE (:startTime IS NULL OR u.createdAt >= :startTime) " +
           "AND (:endTime IS NULL OR u.createdAt <= :endTime)")
    List<User> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                              @Param("endTime") LocalDateTime endTime);
} 