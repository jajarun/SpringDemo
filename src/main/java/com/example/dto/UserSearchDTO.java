package com.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 用户搜索条件DTO
 */
public class UserSearchDTO {
    
    private String name;
    private String email;
    private String phone;
    private String keyword;
    private Boolean hasPhone;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    // 分页参数
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
    private String sortDir = "asc";
    
    // 构造函数
    public UserSearchDTO() {}
    
    // Getter和Setter方法
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public Boolean getHasPhone() {
        return hasPhone;
    }
    
    public void setHasPhone(Boolean hasPhone) {
        this.hasPhone = hasPhone;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDir() {
        return sortDir;
    }
    
    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
    
    @Override
    public String toString() {
        return "UserSearchDTO{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", keyword='" + keyword + '\'' +
                ", hasPhone=" + hasPhone +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDir='" + sortDir + '\'' +
                '}';
    }
} 