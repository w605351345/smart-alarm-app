package com.smartalarm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * SmartAlarm 智能闹钟后端应用
 */
@SpringBootApplication
public class SmartAlarmApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartAlarmApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
