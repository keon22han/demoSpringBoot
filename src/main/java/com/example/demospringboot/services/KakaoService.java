package com.example.demospringboot.services;

import com.example.demospringboot.dto.KakaoUserInfo;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class KakaoService {

    private final RestTemplate restTemplate = new RestTemplate();

    public KakaoUserInfo getUserInfo(String accessToken) {
        // 개발/테스트용 Mock 데이터 (실제 카카오 토큰이 없을 때)
        if ("test-token".equals(accessToken)) {
            return new KakaoUserInfo(123456789L, "test@example.com", "테스트사용자", "https://test-profile.jpg");
        }
        
        String kakaoUserInfoUrl = "https://kapi.kakao.com/v2/user/me";

        // 1. 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);  // Authorization: Bearer {accessToken}
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        // 2. 카카오 API 호출 (제네릭 명확히 지정)
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                kakaoUserInfoUrl,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {}
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("카카오 사용자 정보 조회 실패");
        }

        // 3. 응답 파싱
        Map<String, Object> body = response.getBody();
        Long id = ((Number) body.get("id")).longValue();

        Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
        String email = (String) kakaoAccount.get("email");

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = (String) profile.get("nickname");
        String profileImage = (String) profile.get("profile_image_url");

        // 4. 사용자 정보 반환
        return new KakaoUserInfo(id, email, nickname, profileImage);
    }
}
