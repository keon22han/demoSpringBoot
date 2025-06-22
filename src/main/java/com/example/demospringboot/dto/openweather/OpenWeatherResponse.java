package com.example.demospringboot.dto.openweather;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherResponse {
    private Main main;
    private Weather[] weather;
    private String name;
    private Sys sys;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Main {
        private double temp;
        private double feels_like;
        private double temp_min;
        private double temp_max;
        private int humidity;
        private int pressure;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Weather {
        private int id;
        private String main;
        private String description;
        private String icon;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sys {
        private String country;
        private long sunrise;
        private long sunset;
    }
} 