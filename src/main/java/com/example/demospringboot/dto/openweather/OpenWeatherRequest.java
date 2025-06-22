package com.example.demospringboot.dto.openweather;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherRequest {
    private String city;
    private String countryCode;
} 