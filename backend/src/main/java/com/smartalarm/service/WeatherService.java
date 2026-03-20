package com.smartalarm.service;

import com.smartalarm.dto.WeatherDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class WeatherService {

    private final Random random = new Random();

    /**
     * 获取当前天气
     */
    public WeatherDTO getCurrentWeather(String city, Double latitude, Double longitude) {
        // 模拟天气数据（实际应该调用天气 API）
        String cityName = city != null ? city : "北京市";
        
        return WeatherDTO.builder()
                .city(cityName)
                .description("晴")
                .temperature(25.0 + random.nextDouble() * 5)
                .feelsLike(26.0 + random.nextDouble() * 5)
                .humidity(40 + random.nextInt(30))
                .weatherIcon("☀️")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 获取天气预报
     */
    public WeatherDTO.Forecast getForecast(String city, Integer days) {
        String cityName = city != null ? city : "北京市";
        List<WeatherDTO.DailyForecast> dailyForecasts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        String[] descriptions = {"晴", "多云", "阴", "小雨", "大雨"};
        String[] icons = {"☀️", "⛅", "☁️", "🌧️", "⛈️"};
        
        for (int i = 0; i < days; i++) {
            String date = LocalDate.now().plusDays(i).format(formatter);
            int descIndex = random.nextInt(descriptions.length);
            
            dailyForecasts.add(WeatherDTO.DailyForecast.builder()
                    .date(date)
                    .description(descriptions[descIndex])
                    .tempMin(20.0 + random.nextDouble() * 5)
                    .tempMax(28.0 + random.nextDouble() * 5)
                    .humidity(40 + random.nextInt(40))
                    .weatherIcon(icons[descIndex])
                    .build());
        }
        
        return WeatherDTO.Forecast.builder()
                .daily(dailyForecasts)
                .build();
    }

    /**
     * 获取空气质量
     */
    public WeatherDTO.AirQuality getAirQuality(String city) {
        String cityName = city != null ? city : "北京市";
        int aqi = 30 + random.nextInt(120);
        
        String level;
        if (aqi <= 50) level = "优";
        else if (aqi <= 100) level = "良";
        else if (aqi <= 150) level = "轻度污染";
        else if (aqi <= 200) level = "中度污染";
        else level = "重度污染";
        
        return WeatherDTO.AirQuality.builder()
                .aqi(aqi)
                .level(level)
                .pm25(10.0 + random.nextDouble() * 50)
                .pm10(20.0 + random.nextDouble() * 80)
                .build();
    }
}
