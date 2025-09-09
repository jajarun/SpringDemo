package com.example.repository;

import com.example.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<User> findUsersByDynamicConditions(Map<String, Object> conditions) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = buildPredicates(cb, root, conditions);
        
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Page<User> findUsersByDynamicConditionsWithPagination(Map<String, Object> conditions, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // 查询数据
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        List<Predicate> predicates = buildPredicates(cb, root, conditions);
        
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<User> users = typedQuery.getResultList();

        // 查询总数
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, conditions);
        
        countQuery.select(cb.count(countRoot));
        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }

        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(users, pageable, total);
    }

    @Override
    public List<User> findUsersByNativeSql(String name, String email, String phone, 
                                          LocalDateTime startTime, LocalDateTime endTime) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        List<Object> parameters = new ArrayList<>();
        int paramIndex = 1;

        if (StringUtils.hasText(name)) {
            sql.append(" AND name LIKE ?").append(paramIndex);
            parameters.add("%" + name + "%");
            paramIndex++;
        }

        if (StringUtils.hasText(email)) {
            sql.append(" AND email = ?").append(paramIndex);
            parameters.add(email);
            paramIndex++;
        }

        if (StringUtils.hasText(phone)) {
            sql.append(" AND phone = ?").append(paramIndex);
            parameters.add(phone);
            paramIndex++;
        }

        if (startTime != null) {
            sql.append(" AND created_at >= ?").append(paramIndex);
            parameters.add(startTime);
            paramIndex++;
        }

        if (endTime != null) {
            sql.append(" AND created_at <= ?").append(paramIndex);
            parameters.add(endTime);
            paramIndex++;
        }

        Query query = entityManager.createNativeQuery(sql.toString(), User.class);
        
        for (int i = 0; i < parameters.size(); i++) {
            query.setParameter(i + 1, parameters.get(i));
        }

        @SuppressWarnings("unchecked")
        List<User> result = query.getResultList();
        return result;
    }

    /**
     * 构建查询条件
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<User> root, Map<String, Object> conditions) {
        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                continue;
            }

            switch (key) {
                case "name":
                    if (StringUtils.hasText(value.toString())) {
                        predicates.add(cb.like(cb.lower(root.get("name")), 
                                             "%" + value.toString().toLowerCase() + "%"));
                    }
                    break;
                case "email":
                    if (StringUtils.hasText(value.toString())) {
                        predicates.add(cb.equal(root.get("email"), value));
                    }
                    break;
                case "phone":
                    if (StringUtils.hasText(value.toString())) {
                        predicates.add(cb.equal(root.get("phone"), value));
                    }
                    break;
                case "hasPhone":
                    if ((Boolean) value) {
                        predicates.add(cb.isNotNull(root.get("phone")));
                    }
                    break;
                case "startTime":
                    if (value instanceof LocalDateTime) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), (LocalDateTime) value));
                    }
                    break;
                case "endTime":
                    if (value instanceof LocalDateTime) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), (LocalDateTime) value));
                    }
                    break;
                case "keyword":
                    if (StringUtils.hasText(value.toString())) {
                        String pattern = "%" + value.toString().toLowerCase() + "%";
                        predicates.add(cb.or(
                            cb.like(cb.lower(root.get("name")), pattern),
                            cb.like(cb.lower(root.get("email")), pattern)
                        ));
                    }
                    break;
            }
        }

        return predicates;
    }
} 