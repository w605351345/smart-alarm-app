package com.smartalarm.controller;

import com.smartalarm.dto.ApiResponse;
import com.smartalarm.dto.WeatherDTO;
import com.smartalarm.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * 获取当前天气
     * @param city 城市名（可选，默认从用户位置获取）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<WeatherDTO>> getWeather(
            @RequestParam(required = false) String city,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude) {
        
        WeatherDTO weather = weatherService.getCurrentWeather(city, latitude, longitude);
        return ResponseEntity.ok(ApiResponse.success(weather));
    }

    /**
     * 获取天气预报
     */
    @GetMapping("/forecast")
    public ResponseEntity<ApiResponse<WeatherDTO.Forecast>> getWeatherForecast(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "3") Integer days) {
        
        WeatherDTO.Forecast forecast = weatherService.getForecast(city, days);
        return ResponseEntity.ok(ApiResponse.success(forecast));
    }

    /**
     * 获取空气质量
     */
    @GetMapping("/air-quality")
    public ResponseEntity<ApiResponse<WeatherDTO.AirQuality>> getAirQuality(
            @RequestParam(required = false) String city) {
        
        WeatherDTO.AirQuality airQuality = weatherService.getAirQuality(city);
        return ResponseEntity.ok(ApiResponse.success(airQuality));
    }
}
