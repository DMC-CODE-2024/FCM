package com.eirs.fcm.config;

import com.eirs.fcm.constants.DBType;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
public class AppConfig {

    @Value("${feature-name}")
    private String featureName;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    public DBType getDbType() {
        return driverClassName.startsWith("com.mysql") ? DBType.MYSQL : driverClassName.startsWith("oracle") ? DBType.ORACLE :
                DBType.NONE;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
