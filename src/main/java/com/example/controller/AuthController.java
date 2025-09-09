package com.example.controller;

import com.example.dto.JwtResponse;
import com.example.dto.LoginRequest;
import com.example.entity.User;
import com.example.service.UserDetailsServiceImpl.UserPrincipal;
import com.example.service.UserService;
import com.example.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder encoder;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(loginRequest.getEmail());
            
            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getName(),
                    userService.getUserById(userDetails.getId()).get().getRoles()));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "登录失败");
            error.put("message", "邮箱或密码错误");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User signUpRequest) {
        try {
            if (userService.existsByEmail(signUpRequest.getEmail())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "注册失败");
                error.put("message", "邮箱已被使用");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 创建新用户
            User user = new User(signUpRequest.getName(),
                               signUpRequest.getEmail(),
                               encoder.encode(signUpRequest.getPassword()));
            
            if (signUpRequest.getPhone() != null) {
                user.setPhone(signUpRequest.getPhone());
            }
            
            User savedUser = userService.createUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "用户注册成功");
            response.put("user", Map.of(
                "id", savedUser.getId(),
                "name", savedUser.getName(),
                "email", savedUser.getEmail(),
                "roles", savedUser.getRoles()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "注册失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "登出成功");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            
            User user = userService.getUserById(userDetails.getId()).get();
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("name", user.getName());
            profile.put("email", user.getEmail());
            profile.put("phone", user.getPhone());
            profile.put("roles", user.getRoles());
            profile.put("createdAt", user.getCreatedAt());
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取用户信息失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 