package com.example.entity;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * 权限实体类 - 用于细粒度权限控制
 */
@Entity
@Table(name = "permissions")
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Column
    private String description;
    
    // 构造函数
    public Permission() {}
    
    public Permission(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(name, that.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
    
    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
    
    // 常用权限常量
    public static class Names {
        public static final String READ = "READ";
        public static final String WRITE = "WRITE";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String ADMIN = "ADMIN";
        
        // 资源特定权限
        public static final String READ_USER = "READ_USER";
        public static final String WRITE_USER = "WRITE_USER";
        public static final String UPDATE_USER = "UPDATE_USER";
        public static final String DELETE_USER = "DELETE_USER";
        
        public static final String READ_PRODUCT = "READ_PRODUCT";
        public static final String WRITE_PRODUCT = "WRITE_PRODUCT";
        public static final String UPDATE_PRODUCT = "UPDATE_PRODUCT";
        public static final String DELETE_PRODUCT = "DELETE_PRODUCT";
    }
}
