package com.eirs.fcm.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
public class AppConfig {

    @Value("${module-name}")
    private String moduleName;
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
