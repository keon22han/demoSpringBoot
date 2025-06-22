package com.example.demospringboot.services;

import com.example.demospringboot.dto.gemini.GeminiFunctionCallRequest;
import com.example.demospringboot.dto.gemini.GeminiFunctionCallResponse;
import com.example.demospringboot.dto.gemini.GeminiRequest;
import com.example.demospringboot.dto.gemini.GeminiResponse;
import com.example.demospringboot.dto.openweather.OpenWeatherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiLLMService implements LLMService {

    private final RestTemplate restTemplate;
    private final OpenWeatherService openWeatherService;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String apiUrl;

    @Override
    public String generateResponse(String userQuestion) {
        try {
            log.debug("Gemini API 호출 시작: {}", userQuestion);

            // 날씨 관련 질문인지 확인
            if (isWeatherQuestion(userQuestion)) {
                return handleWeatherQuestion(userQuestion);
            }

            // 일반 질문 처리
            return handleGeneralQuestion(userQuestion);

        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생", e);
            return "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    private boolean isWeatherQuestion(String question) {
        String lowerQuestion = question.toLowerCase();
        return lowerQuestion.contains("날씨") || 
               lowerQuestion.contains("weather") || 
               lowerQuestion.contains("기온") || 
               lowerQuestion.contains("온도");
    }

    private String handleWeatherQuestion(String userQuestion) {
        try {
            log.debug("날씨 질문 처리 시작: {}", userQuestion);

            // 1. Function Calling 요청 생성
            GeminiFunctionCallRequest request = createFunctionCallRequest(userQuestion);

            // 2. Function Calling API 호출
            GeminiFunctionCallResponse response = callFunctionCallAPI(request);

            // 3. Function Call 결과 처리
            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                GeminiFunctionCallResponse.Candidate candidate = response.getCandidates().get(0);
                
                if (candidate.getContent() != null && candidate.getContent().getParts() != null) {
                    for (GeminiFunctionCallResponse.Part part : candidate.getContent().getParts()) {
                        if (part.getFunctionCall() != null) {
                            return executeWeatherFunction(part.getFunctionCall());
                        }
                    }
                }
            }

            // Function Call이 없으면 일반 응답
            return handleGeneralQuestion(userQuestion);

        } catch (Exception e) {
            log.error("날씨 질문 처리 중 오류 발생", e);
            return "날씨 정보를 가져오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    private GeminiFunctionCallRequest createFunctionCallRequest(String userQuestion) {
        // Function Declaration 생성
        GeminiFunctionCallRequest.FunctionDeclaration functionDeclaration = 
            new GeminiFunctionCallRequest.FunctionDeclaration(
                "get_current_weather",
                "특정 도시의 현재 날씨 정보를 가져옵니다.",
                new GeminiFunctionCallRequest.Schema(
                    "object",
                    new GeminiFunctionCallRequest.Properties(
                        new GeminiFunctionCallRequest.Property("string", "도시 이름 (예: 서울, 부산)"),
                        new GeminiFunctionCallRequest.Property("string", "국가 코드 (예: KR, US)")
                    ),
                    Arrays.asList("city", "countryCode")
                )
            );

        // Tool 생성
        GeminiFunctionCallRequest.Tool tool = 
            new GeminiFunctionCallRequest.Tool(Collections.singletonList(functionDeclaration));

        // Content 생성
        GeminiFunctionCallRequest.Content content = 
            new GeminiFunctionCallRequest.Content(
                Collections.singletonList(new GeminiFunctionCallRequest.Part(userQuestion))
            );

        // Request 생성
        return new GeminiFunctionCallRequest(
            Collections.singletonList(content),
            Collections.singletonList(tool)
        );
    }

    private GeminiFunctionCallResponse callFunctionCallAPI(GeminiFunctionCallRequest request) {
        // 1. 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. HTTP 엔티티 생성
        HttpEntity<GeminiFunctionCallRequest> entity = new HttpEntity<>(request, headers);

        // 3. API URL에 API 키 추가
        String urlWithKey = apiUrl + "?key=" + apiKey;

        // 4. Gemini Function Calling API 호출
        return restTemplate.postForObject(urlWithKey, entity, GeminiFunctionCallResponse.class);
    }

    private String executeWeatherFunction(GeminiFunctionCallResponse.FunctionCall functionCall) {
        try {
            String city = functionCall.getArgs().getCity();
            String countryCode = functionCall.getArgs().getCountryCode();

            log.debug("날씨 함수 실행: city={}, countryCode={}", city, countryCode);

            // OpenWeather API 호출
            OpenWeatherResponse weather = openWeatherService.getCurrentWeather(city, countryCode);

            // 날씨 정보 포맷팅
            String weatherInfo = openWeatherService.formatWeatherResponse(weather);

            log.debug("날씨 정보 조회 완료: {}", weatherInfo);
            return weatherInfo;

        } catch (Exception e) {
            log.error("날씨 함수 실행 중 오류 발생", e);
            return "죄송합니다. 해당 도시의 날씨 정보를 가져올 수 없습니다.";
        }
    }

    private String handleGeneralQuestion(String userQuestion) {
        // 1. 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. 요청 바디 생성
        GeminiRequest request = createRequest(userQuestion);

        // 3. HTTP 엔티티 생성
        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

        // 4. API URL에 API 키 추가
        String urlWithKey = apiUrl + "?key=" + apiKey;

        // 5. Gemini API 호출
        GeminiResponse response = restTemplate.postForObject(urlWithKey, entity, GeminiResponse.class);

        // 6. 응답 처리
        if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
            String generatedText = response.getCandidates().get(0).getContent().getParts().get(0).getText();
            log.debug("Gemini API 응답 성공: {}", generatedText);
            return generatedText;
        } else {
            log.warn("Gemini API 응답이 비어있음");
            return "죄송합니다. 응답을 생성할 수 없습니다.";
        }
    }

    private GeminiRequest createRequest(String userQuestion) {
        // 사용자 질문을 Part로 생성
        GeminiRequest.Part part = new GeminiRequest.Part(userQuestion);
        
        // Content 생성
        GeminiRequest.Content content = new GeminiRequest.Content(Collections.singletonList(part));
        
        // Request 생성
        return new GeminiRequest(Collections.singletonList(content));
    }
} 