# Spring Boot JWT 인증 시스템

- Spring Boot와 JWT를 사용한 인증 시스템 구현.
  - 카카오 소셜 로그인 지원
  - Access Token과 Refresh Token을 사용한 보안 인증을 제공합니다.
- Gemini API를 활용한 챗봇 기능, Function Calling을 통한 날씨 정보 조회 기능*

## 주요 기능

- 카카오 소셜 로그인
- JWT Access Token / Refresh Token 발급
- 토큰 갱신 (Access Token 만료 시)
- 로그아웃
- JWT 기반 인증 필터
- **Gemini API 기반 챗봇 서비스**
- **Function Calling을 통한 실시간 날씨 정보 조회**

## 기술 스택

- Spring Boot 3.5.0
- Spring Security
- Spring Data JPA
- JWT (JSON Web Token)
- H2 Database (개발용)
- Lombok
- **Google Gemini API**
- **OpenWeather API**

## 프로젝트 구조

```
src/main/java/com/example/demospringboot/
├── controllers/          # 컨트롤러 레이어
│   ├── AuthController.java
│   └── ChatbotController.java
├── services/            # 서비스 레이어
│   ├── UserService.java
│   ├── RefreshTokenService.java
│   ├── KakaoService.java
│   ├── GeminiLLMService.java
│   └── OpenWeatherService.java
├── repositories/        # 리포지토리 레이어
│   ├── UserRepository.java
│   └── RefreshTokenRepository.java
├── entities/           # 엔티티
│   ├── User.java
│   └── RefreshToken.java
├── security/           # 보안 설정
│   ├── SecurityConfig.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
├── utils/              # 유틸리티
│   └── JwtProvider.java
├── dto/                # 데이터 전송 객체
│   ├── request/
│   ├── response/
│   ├── gemini/
│   └── openweather/
├── config/             # 설정
│   └── RestTemplateConfig.java
└── exceptions/         # 예외 처리
    ├── AuthException.java
    └── GlobalExceptionHandler.java
```

## API 엔드포인트

### 인증 관련 API

#### 1. 카카오 로그인
```
POST /api/auth/kakao
Content-Type: application/json

{
  "accessToken": "카카오_액세스_토큰"
}
```

응답:
```json
{
  "accessToken": "JWT_액세스_토큰",
  "refreshToken": "JWT_리프레시_토큰"
}
```

#### 2. 토큰 갱신
```
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "JWT_리프레시_토큰"
}
```

#### 3. 로그아웃
```
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "JWT_리프레시_토큰"
}
```

#### 4. 현재 사용자 정보 조회
```
GET /api/auth/me
Authorization: Bearer JWT_액세스_토큰
```

### 챗봇 API

#### 1. 챗봇 질문 (Function Calling 지원)
```
POST /api/chatbot/question
Content-Type: application/json

{
  "question": "서울의 날씨를 알려줘"
}
```

**일반 질문 응답:**
```json
{
  "answer": "안녕하세요! 어떤 도움이 필요하신가요?"
}
```

**날씨 질문 응답 (Function Calling):**
```json
{
  "answer": "📍 서울의 현재 날씨\n🌡️ 기온: 15.2°C (체감온도: 13.8°C)\n🌤️ 날씨: 맑음\n💧 습도: 65%\n🌪️ 기압: 1013 hPa\n📊 최저/최고: 12.1°C / 18.5°C"
}
```

#### 2. 챗봇 상태 확인
```
GET /api/chatbot/health
```

## 설정

### application.properties
```properties
# JWT 설정
jwt.secret=your-super-secret-jwt-key-that-is-at-least-256-bits-long-for-hs256-algorithm

# 데이터베이스 설정
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 콘솔
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Gemini API 설정
gemini.api.key=your-gemini-api-key-here
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent

# OpenWeather API 설정
openweather.api.key=your-openweather-api-key-here
openweather.api.url=https://api.openweathermap.org/data/2.5/weather
```

## 실행 방법

1. 프로젝트 클론
```bash
git clone <repository-url>
cd demoSpringBoot
```

2. API 키 설정
   - **Gemini API 키**: [Google AI Studio](https://makersuite.google.com/app/apikey)에서 발급
   - **OpenWeather API 키**: [OpenWeather](https://openweathermap.org/api)에서 발급
   - `application.properties`에 각각 설정

3. 애플리케이션 실행
```bash
./gradlew bootRun
```

4. H2 콘솔 접속 (개발용)
```
http://localhost:8080/h2-console
```

## 사용 예시

### Function Calling 테스트
```bash
# 날씨 질문 (Function Calling 자동 실행)
curl -X POST http://localhost:8080/api/chatbot/question \
  -H "Content-Type: application/json" \
  -d '{
    "question": "서울의 날씨를 알려줘"
  }'

# 다른 도시 날씨
curl -X POST http://localhost:8080/api/chatbot/question \
  -H "Content-Type: application/json" \
  -d '{
    "question": "부산의 현재 날씨는 어때?"
  }'

# 일반 질문
curl -X POST http://localhost:8080/api/chatbot/question \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Spring Boot란 무엇인가요?"
  }'
```

### 인증 테스트
```bash
# 카카오 로그인 (테스트용)
curl -X POST http://localhost:8080/api/auth/kakao \
  -H "Content-Type: application/json" \
  -d '{"accessToken": "test-token"}'

# 사용자 정보 조회
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer JWT_액세스_토큰"
```

## Function Calling 동작 과정

### 1. 사용자 질문 분석
```
사용자: "서울의 날씨를 알려줘"
↓
GeminiLLMService: 날씨 관련 키워드 감지
```

### 2. Function Declaration 전송
```json
{
  "contents": [{"parts": [{"text": "서울의 날씨를 알려줘"}]}],
  "tools": [{
    "functionDeclarations": [{
      "name": "get_current_weather",
      "description": "특정 도시의 현재 날씨 정보를 가져옵니다.",
      "parameters": {
        "type": "object",
        "properties": {
          "city": {"type": "string", "description": "도시 이름"},
          "countryCode": {"type": "string", "description": "국가 코드"}
        },
        "required": ["city", "countryCode"]
      }
    }]
  }]
}
```

### 3. Gemini Function Call 응답
```json
{
  "candidates": [{
    "content": {
      "parts": [{
        "functionCall": {
          "name": "get_current_weather",
          "args": {
            "city": "서울",
            "countryCode": "KR"
          }
        }
      }]
    }
  }]
}
```

### 4. OpenWeather API 호출
```
OpenWeather API: https://api.openweathermap.org/data/2.5/weather?q=서울,KR&appid=YOUR_API_KEY&units=metric&lang=kr
```

### 5. 포맷된 응답 반환
```
📍 서울의 현재 날씨
🌡️ 기온: 15.2°C (체감온도: 13.8°C)
🌤️ 날씨: 맑음
💧 습도: 65%
🌪️ 기압: 1013 hPa
📊 최저/최고: 12.1°C / 18.5°C
```

## 보안 특징

- **Access Token**: 1시간 유효, API 요청 시 사용
- **Refresh Token**: 24시간 유효, 토큰 갱신 시 사용
- **JWT 필터**: 모든 요청에 대해 JWT 토큰 검증
- **Stateless**: 세션 없이 토큰 기반 인증
- **토큰 저장**: Refresh Token은 데이터베이스에 저장하여 관리
- **챗봇 API**: 인증 없이 접근 가능 (public API)
- **Function Calling**: 실시간 외부 API 연동
