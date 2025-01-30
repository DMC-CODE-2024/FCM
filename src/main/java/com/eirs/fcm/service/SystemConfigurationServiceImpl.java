package com.eirs.fcm.service;

import com.eirs.fcm.config.AppConfig;
import com.eirs.fcm.repository.ConfigRepository;
import com.eirs.fcm.repository.entity.SystemConfig;
import com.eirs.fcm.repository.entity.SystemConfigKeys;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class SystemConfigurationServiceImpl implements SystemConfigurationService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigRepository repository;

    @Autowired
    private AppConfig appConfig;
    List<String> eirInstances = new ArrayList<>();

    Map<String, String> shortCodeWithOperatorMap = new HashMap<>();

    @Autowired
    private ModuleAlertService moduleAlertService;

    @PostConstruct
    public void init() {

        try {
            getOperators();
        } catch (Exception e) {
            Runtime.getRuntime().halt(1);
        }
    }

    @Override
    public synchronized List<String> getOperators() {
        if (CollectionUtils.isEmpty(eirInstances)) {
            Integer noOfOperators = findByKey(SystemConfigKeys.FCM_NO_OF_OPERATORS, 0);
            for (int i = 1; i <= noOfOperators; i++) {
                String operator = findByKey(SystemConfigKeys.FCM_OPERATOR.replaceAll("<NUMBER>", String.valueOf(i))).toUpperCase();
                String shortCode = findByKey(SystemConfigKeys.SHORT_CODE.replaceAll("<OPERATOR>", operator));
                eirInstances.add(operator);
                shortCodeWithOperatorMap.put(operator, shortCode);
            }
        }
        return new ArrayList<>(eirInstances);
    }

    @Override
    public String getShortCode(String operator) {
        return shortCodeWithOperatorMap.get(operator);
    }

    public String findByKey(String key) throws RuntimeException {
        Optional<SystemConfig> optional = repository.findByConfigKey(key);
        if (optional.isPresent()) {
            log.info("Filled key:{} value:{}", key, optional.get().getConfigValue());
            return optional.get().getConfigValue();
        } else {
            moduleAlertService.sendConfigurationMissingAlert(key);
            log.info("Value for key:{} Not Found", key);
            throw new RuntimeException("Config Key:" + key + ", value not found");
        }
    }

    public Integer findByKey(String key, int defaultValue) {
        String value = null;
        try {
            value = findByKey(key);
            try {
                return Integer.parseInt(value);
            } catch (RuntimeException e) {
                moduleAlertService.sendConfigurationWrongValueAlert(key, value);
                return defaultValue;
            }
        } catch (RuntimeException e) {
            return defaultValue;
        }

    }

    public String findByKey(String key, String defaultValue) {
        try {
            return findByKey(key);
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }

    public Float findByKey(String key, float defaultValue) {
        String value = null;
        try {
            value = findByKey(key);
            try {
                return Float.parseFloat(value);
            } catch (RuntimeException e) {
                moduleAlertService.sendConfigurationWrongValueAlert(key, value);
                return defaultValue;
            }
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean isEnabled(String key) {
        String isEnable = findByKey(key, "NO");
        if (StringUtils.equalsAnyIgnoreCase(isEnable, new String[]{"YES", "TRUE"})) {
            return true;
        }
        return false;
    }
}
