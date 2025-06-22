package com.example.demospringboot.services;

import com.example.demospringboot.dto.openweather.OpenWeatherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenWeatherService {

    private final RestTemplate restTemplate;

    @Value("${openweather.api.key}")
    private String apiKey;

    @Value("${openweather.api.url:https://api.openweathermap.org/data/2.5/weather}")
    private String apiUrl;

    public OpenWeatherResponse getCurrentWeather(String city, String countryCode) {
        try {
            log.debug("OpenWeather API 호출 시작: city={}, countryCode={}", city, countryCode);
            
            // API URL 구성
            String url = String.format("%s?q=%s,%s&appid=%s&units=metric&lang=kr", 
                apiUrl, city, countryCode, apiKey);
            
            // OpenWeather API 호출
            OpenWeatherResponse response = restTemplate.getForObject(url, OpenWeatherResponse.class);
            
            if (response != null) {
                log.debug("OpenWeather API 응답 성공: {}", response.getName());
                return response;
            } else {
                log.warn("OpenWeather API 응답이 비어있음");
                throw new RuntimeException("날씨 정보를 가져올 수 없습니다.");
            }
            
        } catch (Exception e) {
            log.error("OpenWeather API 호출 중 오류 발생", e);
            throw new RuntimeException("날씨 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public String formatWeatherResponse(OpenWeatherResponse weather) {
        if (weather == null || weather.getMain() == null || weather.getWeather() == null || weather.getWeather().length == 0) {
            return "날씨 정보를 가져올 수 없습니다.";
        }

        OpenWeatherResponse.Main main = weather.getMain();
        OpenWeatherResponse.Weather weatherInfo = weather.getWeather()[0];

        return String.format(
            "📍 %s의 현재 날씨\n" +
            "🌡️ 기온: %.1f°C (체감온도: %.1f°C)\n" +
            "🌤️ 날씨: %s\n" +
            "💧 습도: %d%%\n" +
            "🌪️ 기압: %d hPa\n" +
            "📊 최저/최고: %.1f°C / %.1f°C",
            weather.getName(),
            main.getTemp(),
            main.getFeels_like(),
            weatherInfo.getDescription(),
            main.getHumidity(),
            main.getPressure(),
            main.getTemp_min(),
            main.getTemp_max()
        );
    }
} 