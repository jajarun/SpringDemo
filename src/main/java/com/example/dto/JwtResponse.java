package com.example.dto;

import com.example.entity.User;
import java.util.Set;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String name;
    private Set<User.Role> roles;
    
    public JwtResponse(String accessToken, Long id, String email, String name, Set<User.Role> roles) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.name = name;
        this.roles = roles;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Set<User.Role> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<User.Role> roles) {
        this.roles = roles;
    }
} 