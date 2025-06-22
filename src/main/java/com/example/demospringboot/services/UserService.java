package com.example.demospringboot.services;

import com.example.demospringboot.dto.KakaoUserInfo;
import com.example.demospringboot.entities.User;
import com.example.demospringboot.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findOrCreateUser(KakaoUserInfo kakaoUserInfo) {
        // 기존 사용자 조회
        Optional<User> existingUser = userRepository.findByProviderAndProviderId("kakao", kakaoUserInfo.getId().toString());
        
        if (existingUser.isPresent()) {
            // 기존 사용자 정보 업데이트
            User user = existingUser.get();
            User updatedUser = User.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(kakaoUserInfo.getNickname())
                    .profileImage(kakaoUserInfo.getProfileImage())
                    .provider(user.getProvider())
                    .providerId(user.getProviderId())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return userRepository.save(updatedUser);
        } else {
            // 새 사용자 생성
            User newUser = User.builder()
                    .email(kakaoUserInfo.getEmail())
                    .nickname(kakaoUserInfo.getNickname())
                    .profileImage(kakaoUserInfo.getProfileImage())
                    .provider("kakao")
                    .providerId(kakaoUserInfo.getId().toString())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return userRepository.save(newUser);
        }
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
}
