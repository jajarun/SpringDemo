package com.example.service;

import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.repository.UserSpecifications;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;
    
    // 创建用户
    public User createUser(User user) {
        // entityManager.persist(user);
        return userRepository.save(user);
    }
    
    // 获取所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // 根据ID获取用户
    public Optional<User> getUserById(Long id) {
        // User user = entityManager.find(User.class, id);
        // return Optional.ofNullable(user);
        return userRepository.findById(id);
    }
    
    // 根据邮箱获取用户
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailCached(email);
    }
    
    // 搜索用户
    public List<User> searchUsers(String keyword) {
        return userRepository.searchByKeyword(keyword);
    }
    
    // 更新用户
    public User updateUser(Long id, User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setPhone(userDetails.getPhone());
            return userRepository.save(user);
        }
        return null;
    }
    
    // 删除用户
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // 检查邮箱是否存在
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    // 获取有电话号码的用户
    public List<User> getUsersWithPhone() {
        return userRepository.findUsersWithPhone();
    }

    // ==================== 动态查询方法 ====================

    /**
     * 使用Specification进行动态查询
     */
    public List<User> findUsersBySpecification(String name, String email, String phone, 
                                             LocalDateTime startTime, LocalDateTime endTime) {
        Specification<User> spec = UserSpecifications.dynamicQuery(name, email, phone, startTime, endTime);
        return userRepository.findAll(spec);
    }

    /**
     * 使用Specification进行分页动态查询
     */
    public Page<User> findUsersBySpecificationWithPagination(String name, String email, String phone, 
                                                           LocalDateTime startTime, LocalDateTime endTime, 
                                                           Pageable pageable) {
        Specification<User> spec = UserSpecifications.dynamicQuery(name, email, phone, startTime, endTime);
        return userRepository.findAll(spec, pageable);
    }

    /**
     * 组合多个Specification条件
     */
    public List<User> findUsersByMultipleConditions(String name, String email, boolean hasPhone, String keyword) {
        Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        
        if (name != null && !name.trim().isEmpty()) {
            spec = spec.and(UserSpecifications.nameContains(name));
        }
        
        if (email != null && !email.trim().isEmpty()) {
            spec = spec.and(UserSpecifications.emailEquals(email));
        }
        
        if (hasPhone) {
            spec = spec.and(UserSpecifications.hasPhone());
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and(UserSpecifications.searchByKeyword(keyword));
        }
        
        return userRepository.findAll(spec);
    }

    /**
     * 使用自定义Repository进行动态查询
     */
    public List<User> findUsersByDynamicConditions(String name, String email, String phone, 
                                                  LocalDateTime startTime, LocalDateTime endTime, 
                                                  String keyword, Boolean hasPhone) {
        Map<String, Object> conditions = new HashMap<>();
        
        if (name != null && !name.trim().isEmpty()) {
            conditions.put("name", name);
        }
        
        if (email != null && !email.trim().isEmpty()) {
            conditions.put("email", email);
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            conditions.put("phone", phone);
        }
        
        if (startTime != null) {
            conditions.put("startTime", startTime);
        }
        
        if (endTime != null) {
            conditions.put("endTime", endTime);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            conditions.put("keyword", keyword);
        }
        
        if (hasPhone != null && hasPhone) {
            conditions.put("hasPhone", true);
        }
        
        return userRepository.findUsersByDynamicConditions(conditions);
    }

    /**
     * 使用自定义Repository进行分页动态查询
     */
    public Page<User> findUsersByDynamicConditionsWithPagination(String name, String email, String phone, 
                                                               LocalDateTime startTime, LocalDateTime endTime, 
                                                               String keyword, Boolean hasPhone, 
                                                               Pageable pageable) {
        Map<String, Object> conditions = new HashMap<>();
        
        if (name != null && !name.trim().isEmpty()) {
            conditions.put("name", name);
        }
        
        if (email != null && !email.trim().isEmpty()) {
            conditions.put("email", email);
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            conditions.put("phone", phone);
        }
        
        if (startTime != null) {
            conditions.put("startTime", startTime);
        }
        
        if (endTime != null) {
            conditions.put("endTime", endTime);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            conditions.put("keyword", keyword);
        }
        
        if (hasPhone != null && hasPhone) {
            conditions.put("hasPhone", true);
        }
        
        return userRepository.findUsersByDynamicConditionsWithPagination(conditions, pageable);
    }

    /**
     * 使用原生SQL进行动态查询
     */
    public List<User> findUsersByNativeSql(String name, String email, String phone, 
                                         LocalDateTime startTime, LocalDateTime endTime) {
        return userRepository.findUsersByNativeSql(name, email, phone, startTime, endTime);
    }

    /**
     * 根据关键词搜索用户（在名称和邮箱中搜索）
     */
    public List<User> searchUsersByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll();
        }
        
        Specification<User> spec = UserSpecifications.searchByKeyword(keyword);
        return userRepository.findAll(spec);
    }

    /**
     * 根据时间范围查询用户
     */
    public List<User> findUsersByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        Specification<User> spec = UserSpecifications.createdBetween(startTime, endTime);
        return userRepository.findAll(spec);
    }
} 