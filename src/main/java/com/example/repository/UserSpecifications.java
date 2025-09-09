package com.example.repository;

import com.example.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {

    /**
     * 根据名称模糊查询
     */
    public static Specification<User> nameContains(String name) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(name)) {
                return criteriaBuilder.conjunction(); // 返回true，不添加条件
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")), 
                "%" + name.toLowerCase() + "%"
            );
        };
    }

    /**
     * 根据邮箱精确查询
     */
    public static Specification<User> emailEquals(String email) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(email)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("email"), email);
        };
    }

    /**
     * 根据手机号查询
     */
    public static Specification<User> phoneEquals(String phone) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(phone)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("phone"), phone);
        };
    }

    /**
     * 查询有手机号的用户
     */
    public static Specification<User> hasPhone() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.isNotNull(root.get("phone"));
    }

    /**
     * 根据创建时间范围查询
     */
    public static Specification<User> createdBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (startTime != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startTime));
            }
            
            if (endTime != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endTime));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 综合动态查询条件
     */
    public static Specification<User> dynamicQuery(String name, String email, String phone, 
                                                   LocalDateTime startTime, LocalDateTime endTime) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 名称模糊查询
            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), 
                    "%" + name.toLowerCase() + "%"
                ));
            }

            // 邮箱精确查询
            if (StringUtils.hasText(email)) {
                predicates.add(criteriaBuilder.equal(root.get("email"), email));
            }

            // 手机号查询
            if (StringUtils.hasText(phone)) {
                predicates.add(criteriaBuilder.equal(root.get("phone"), phone));
            }

            // 创建时间范围查询
            if (startTime != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startTime));
            }

            if (endTime != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endTime));
            }

            // 如果没有任何条件，返回true（查询所有）
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // 使用AND连接所有条件
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 关键词搜索（在名称或邮箱中搜索）
     */
    public static Specification<User> searchByKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(keyword)) {
                return criteriaBuilder.conjunction();
            }
            
            String pattern = "%" + keyword.toLowerCase() + "%";
            
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern)
            );
        };
    }
} 