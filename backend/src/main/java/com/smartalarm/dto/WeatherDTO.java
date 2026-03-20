package com.smartalarm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDTO {
    
    private String city;
    private String description;
    private Double temperature;
    private Double feelsLike;
    private Integer humidity;
    private String weatherIcon;
    private Long timestamp;
    
    private Forecast forecast;
    private AirQuality airQuality;
    
    @Data
    @Builder
    public static class Forecast {
        private List<DailyForecast> daily;
    }
    
    @Data
    @Builder
    public static class DailyForecast {
        private String date;
        private String description;
        private Double tempMin;
        private Double tempMax;
        private Integer humidity;
        private String weatherIcon;
    }
    
    @Data
    @Builder
    public static class AirQuality {
        private Integer aqi;
        private String level;
        private Double pm25;
        private Double pm10;
    }
}
