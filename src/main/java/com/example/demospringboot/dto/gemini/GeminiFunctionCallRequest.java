package com.example.demospringboot.dto.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiFunctionCallRequest {
    private List<Content> contents;
    private List<Tool> tools;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private List<Part> parts;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tool {
        private List<FunctionDeclaration> functionDeclarations;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionDeclaration {
        private String name;
        private String description;
        private Schema parameters;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Schema {
        private String type;
        private Properties properties;
        private List<String> required;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Properties {
        private Property city;
        private Property countryCode;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Property {
        private String type;
        private String description;
    }
} 