package com.eirs.fcm.service;

import java.util.List;

public interface SystemConfigurationService {

    String blankOperator = "";

    List<String> getOperators();

    String getShortCode(String operator);

    String findByKey(String key) throws RuntimeException;

    Integer findByKey(String key, int defaultValue);

    String findByKey(String key, String defaultValue);

    Float findByKey(String key, float defaultValue);

    boolean isEnabled(String key);
}
