package com.example.service;

import com.example.entity.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("用户未找到: " + email));
        
        return UserPrincipal.create(user);
    }
    
    // 内部类：用户主体
    public static class UserPrincipal implements UserDetails {
        private Long id;
        private String email;
        private String name;
        private String password;
        private List<GrantedAuthority> authorities;
        private boolean enabled;
        
        public UserPrincipal(Long id, String email, String name, String password, 
                            List<GrantedAuthority> authorities, boolean enabled) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.password = password;
            this.authorities = authorities;
            this.enabled = enabled;
        }
        
        public static UserPrincipal create(User user) {
            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                    .collect(Collectors.toList());
            
            return new UserPrincipal(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getPassword(),
                    authorities,
                    user.isEnabled()
            );
        }
        
        public Long getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        @Override
        public String getUsername() {
            return email;
        }
        
        @Override
        public String getPassword() {
            return password;
        }
        
        @Override
        public List<GrantedAuthority> getAuthorities() {
            return authorities;
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
        
        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
} 