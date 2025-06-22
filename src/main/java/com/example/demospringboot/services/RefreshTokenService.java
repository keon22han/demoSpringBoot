package com.example.demospringboot.services;

import com.example.demospringboot.entities.RefreshToken;
import com.example.demospringboot.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    public void save(Long userId, String refreshToken) {
        // 기존 토큰이 있다면 삭제
        repository.deleteByUserId(userId);
        
        // 새 토큰 저장 (24시간 후 만료)
        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(token);
    }

    public boolean isValid(Long userId, String refreshToken) {
        Optional<RefreshToken> tokenOpt = repository.findByUserId(userId);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        RefreshToken token = tokenOpt.get();
        
        // 토큰 값이 일치하고 만료되지 않았는지 확인
        return token.getToken().equals(refreshToken) && 
               token.getExpiresAt().isAfter(LocalDateTime.now());
    }

    public void deleteByUserId(Long userId) {
        repository.deleteByUserId(userId);
    }

    public void deleteByToken(String refreshToken) {
        repository.deleteByToken(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String refreshToken) {
        return repository.findByToken(refreshToken);
    }
}

