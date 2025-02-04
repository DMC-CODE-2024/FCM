package com.eirs.fcm.alert;

import com.eirs.fcm.constants.AlertIds;
import com.eirs.fcm.constants.AlertMessagePlaceholders;

import java.util.Map;

public interface AlertService {

    void sendAlertNow(AlertIds alertIds, Map<AlertMessagePlaceholders, String> placeHolderMap);
    void sendAlert(AlertIds alertIds, Map<AlertMessagePlaceholders, String> placeHolderMap);
}
