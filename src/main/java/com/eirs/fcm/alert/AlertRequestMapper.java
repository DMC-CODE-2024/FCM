package com.eirs.fcm.alert;

import com.eirs.fcm.constants.AlertMessagePlaceholders;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AlertRequestMapper {

    public Map<String, String> toAlertRequest(AlertDto alertDto) {
        Map<String, String> map = new HashMap<>();
        map.put("alertId", alertDto.getAlertId());
        map.put("alertProcess", alertDto.getAlertProcess());
        map.put("priority", "High");
        try {
            map.put("serverName", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (alertDto.getPlaceHolderMap() != null) {
            map.put("featureName", alertDto.getAlertProcess());
            alertDto.getPlaceHolderMap().forEach((k, v) -> map.put(k.getPlaceholder(), v));
        }
        return map;
    }

}
