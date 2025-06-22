package com.example.demospringboot.controllers;

import com.example.demospringboot.dto.request.ChatbotQuestionRequest;
import com.example.demospringboot.dto.response.ChatbotResponse;
import com.example.demospringboot.services.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final LLMService llmService;

    @PostMapping("/question")
    public ResponseEntity<ChatbotResponse> askQuestion(@RequestBody ChatbotQuestionRequest request) {
        try {
            log.info("챗봇 질문 받음: {}", request.getQuestion());
            // LLM 서비스를 통해 응답 생성
            String response = llmService.generateResponse(request.getQuestion());
            
            ChatbotResponse chatbotResponse = new ChatbotResponse(response);
            log.info("챗봇 응답 생성 완료");
            
            return ResponseEntity.ok(chatbotResponse);
            
        } catch (Exception e) {
            log.error("챗봇 응답 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(new ChatbotResponse("죄송합니다. 일시적인 오류가 발생했습니다."));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chatbot service is running");
    }
} 