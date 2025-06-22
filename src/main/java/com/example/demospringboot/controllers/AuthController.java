package com.example.demospringboot.controllers;

import com.example.demospringboot.dto.KakaoTokenRequest;
import com.example.demospringboot.dto.KakaoUserInfo;
import com.example.demospringboot.dto.request.LogoutRequest;
import com.example.demospringboot.dto.request.RefreshTokenRequest;
import com.example.demospringboot.dto.response.TokenResponse;
import com.example.demospringboot.entities.User;
import com.example.demospringboot.services.KakaoService;
import com.example.demospringboot.services.RefreshTokenService;
import com.example.demospringboot.services.UserService;
import com.example.demospringboot.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final KakaoService kakaoService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @PostMapping("/kakao")
    public ResponseEntity<TokenResponse> kakaoLogin(@RequestBody KakaoTokenRequest request) {
        try {
            // 1. 카카오 사용자 정보 조회
            KakaoUserInfo kakaoUser = kakaoService.getUserInfo(request.getAccessToken());

            // 2. 사용자 DB 저장 또는 조회
            User user = userService.findOrCreateUser(kakaoUser);

            // 3. JWT 토큰 생성
            String accessToken = jwtProvider.createAccessToken(user.getId());
            String refreshToken = jwtProvider.createRefreshToken(user.getId());

            // 4. 리프레시 토큰 저장
            refreshTokenService.save(user.getId(), refreshToken);

            // 5. 토큰 응답
            return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
        } catch (Exception e) {
            log.error("카카오 로그인 중 오류 발생", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            // 1. 리프레시 토큰에서 사용자 ID 추출
            Long userId = jwtProvider.getUserIdFromToken(request.getRefreshToken());

            // 2. 리프레시 토큰 유효성 검증
            if (!refreshTokenService.isValid(userId, request.getRefreshToken())) {
                return ResponseEntity.badRequest().build();
            }

            // 3. 새로운 액세스 토큰 생성
            String newAccessToken = jwtProvider.createAccessToken(userId);

            // 4. 새로운 리프레시 토큰 생성 (선택사항)
            String newRefreshToken = jwtProvider.createRefreshToken(userId);
            refreshTokenService.save(userId, newRefreshToken);

            return ResponseEntity.ok(new TokenResponse(newAccessToken, newRefreshToken));
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        try {
            // 1. 리프레시 토큰에서 사용자 ID 추출
            Long userId = jwtProvider.getUserIdFromToken(request.getRefreshToken());

            // 2. 리프레시 토큰 삭제
            refreshTokenService.deleteByUserId(userId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        try {
            log.debug("getCurrentUser 호출됨");
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.debug("Authentication: {}", authentication);
            
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getName())) {
                
                String userIdStr = authentication.getName();
                log.debug("User ID: {}", userIdStr);
                
                try {
                    Long userId = Long.valueOf(userIdStr);
                    
                    return userService.findById(userId)
                            .map(user -> {
                                log.debug("사용자 정보 조회 성공: {}", user);
                                return ResponseEntity.ok(user);
                            })
                            .orElse(ResponseEntity.notFound().build());
                } catch (NumberFormatException e) {
                    log.error("User ID 파싱 오류: {}", userIdStr, e);
                    return ResponseEntity.badRequest().build();
                }
            } else {
                log.debug("인증되지 않은 사용자");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            log.error("getCurrentUser 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
