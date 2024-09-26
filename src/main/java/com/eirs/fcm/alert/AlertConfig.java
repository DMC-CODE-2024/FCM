package com.eirs.fcm.alert;

import com.eirs.fcm.constants.AlertIds;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
public class AlertConfig {

    @Value("${eirs.alert.url}")
    private String url;

    private Map<AlertIds, AlertConfigDto> alertsMapping;

    @PostConstruct
    public void init() {
        alertsMapping = new HashMap<>();
        alertsMapping.put(AlertIds.CONFIGURATION_VALUE_MISSING, new AlertConfigDto("alert2201"));
        alertsMapping.put(AlertIds.CONFIGURATION_VALUE_WRONG, new AlertConfigDto("alert2202"));
        alertsMapping.put(AlertIds.DATABASE_EXCEPTION, new AlertConfigDto("alert2206"));
        alertsMapping.put(AlertIds.FILE_COPY_URL_EXCEPTION, new AlertConfigDto("alert2208"));
        alertsMapping.put(AlertIds.FILE_CREATION_ERROR, new AlertConfigDto("alert2207"));
    }
}
