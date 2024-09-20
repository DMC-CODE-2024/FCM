package com.eirs.fcm.alert;

import com.eirs.fcm.constants.AlertIds;
import com.eirs.fcm.constants.AlertMessagePlaceholders;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class AlertServiceImpl implements AlertService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private RestTemplate restTemplate = null;

    private BlockingQueue<AlertDto> queue = null;

    @Autowired
    AlertRequestMapper mapper;
    @Autowired
    AlertConfig alertConfig;

    @PostConstruct
    public void init() {
        if (alertConfig.getUrl() == null) {
            log.error("Alert Service is not enabled configuration missing alerts.postUrl");
            System.exit(0);
        } else {
            SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
            clientHttpRequestFactory.setConnectTimeout(1000);
            clientHttpRequestFactory.setReadTimeout(1000);
            restTemplate = new RestTemplate(clientHttpRequestFactory);
            queue = new LinkedBlockingQueue();
            new Thread(() -> sendAlertConsumer(), "sendAlertsConsumerThread").start();
        }
    }


    @Override
    public void sendAlert(AlertIds alertIds, Map<AlertMessagePlaceholders, String> placeHolderMap) {
        AlertConfigDto configDto = alertConfig.getAlertsMapping().get(alertIds);
        if (configDto == null) {
            log.error("Message not configured for AlertId:{}", alertIds);
        } else {
            String alertId = configDto.getAlertId();
            putToQueue(AlertDto.builder().alertId(alertId).placeHolderMap(placeHolderMap).alertProcess(alertConfig.getProcessId()).build());
        }
    }

    private void putToQueue(AlertDto alertDto) {
        try {
            queue.put(alertDto);
            log.info("Alert Added Request:{} QueueSize:{}", alertDto, queue.size());
        } catch (InterruptedException e) {
            log.error("Error:{} while adding Alert to Queue Request:{}", e.getMessage(), alertDto, e);
        }

    }

    private void sendAlertConsumer() {
        log.info("Started Thread:{}", Thread.currentThread().getName());
        while (true) {
            try {
                AlertDto alertDto = queue.take();
                log.info("Alert taken from Queue Request:{} QueueSize:{}", alertDto, queue.size());
                callAlertUrl(alertDto);
            } catch (InterruptedException e) {
                log.error("Error while Taking Request from Queue Error:{} ", e.getMessage(), e);
            }
        }
    }

    public void callAlertUrl(AlertDto alertDto) {
        long start = System.currentTimeMillis();
        Map<String, String> requestDto = mapper.toAlertRequest(alertDto);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<Map<String, String>>(mapper.toAlertRequest(alertDto), headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(alertConfig.getUrl(), request, String.class);
            log.info("Alert Sent Request:{}, TimeTaken:{} Response:{}", requestDto, responseEntity, (System.currentTimeMillis() - start));
        } catch (org.springframework.web.client.ResourceAccessException resourceAccessException) {
            log.error("Error while Sending Alert resourceAccessException:{} Request:{}", resourceAccessException.getMessage(), requestDto, resourceAccessException);
        } catch (Exception e) {
            log.error("Error while Sending Alert Error:{} Request:{}", e.getMessage(), requestDto, e);
        }
    }
}
