package com.example.config;

import com.example.entity.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查是否已存在管理员用户
        if (!userRepository.existsByEmail("admin@example.com")) {
            User admin = new User("管理员", "admin@example.com", passwordEncoder.encode("admin123"));
            admin.setRoles(Set.of(User.Role.ADMIN, User.Role.USER));
            userRepository.save(admin);
            System.out.println("默认管理员用户已创建: admin@example.com / admin123");
        }
        
        // 创建普通用户示例
        if (!userRepository.existsByEmail("user@example.com")) {
            User user = new User("普通用户", "user@example.com", passwordEncoder.encode("user123"));
            user.setPhone("13800138000");
            userRepository.save(user);
            System.out.println("默认普通用户已创建: user@example.com / user123");
        }
    }
} 