package com.example.demospringboot.security;

import com.example.demospringboot.entities.User;
import com.example.demospringboot.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 이 메서드는 JWT 기반 인증에서는 사용되지 않지만 구현해야 함
        throw new UsernameNotFoundException("Username not found: " + username);
    }

    public UserDetails loadUserById(Long userId) {
        return userService.findById(userId)
                .map(this::createUserDetails)
                .orElse(null);
    }

    private UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(user.getId()))
                .password("") // JWT에서는 패스워드가 필요 없음
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("USER")))
                .build();
    }
} 