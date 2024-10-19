package com.eirs.fcm.client;

import com.eirs.fcm.alert.AlertConfig;
import com.eirs.fcm.client.dto.SftpDestinationDto;
import com.eirs.fcm.client.dto.SftpFileDto;
import com.eirs.fcm.config.AppConfig;
import com.eirs.fcm.constants.AlertIds;
import com.eirs.fcm.constants.AlertMessagePlaceholders;
import com.eirs.fcm.constants.CopyStatus;
import com.eirs.fcm.constants.FileType;
import com.eirs.fcm.repository.entity.ListFileManagement;
import com.eirs.fcm.repository.entity.SystemConfigKeys;
import com.eirs.fcm.service.ListFileManagementService;
import com.eirs.fcm.service.ModuleAlertService;
import com.eirs.fcm.service.SystemConfigurationService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SftpFileService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SystemConfigurationService systemConfigurationService;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ModuleAlertService alertService;
    private String serverName = null;

    @Autowired
    ListFileManagementService listFileManagementService;

    @Value("${sftp.copy-url.retry-time-in-min:1}")
    private Integer sftpUrlRetryTime;

    @Autowired
    AppConfig appConfig;

    @PostConstruct
    public void init() {
        try {
            serverName = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> syncNotCopied()).start();
    }

    private CopyStatus callUrl(SftpFileDto sftpFileDto, String operator) {
        CopyStatus copyStatus = CopyStatus.NEW;
        String URL = null;
        try {
            URL = systemConfigurationService.findByKey(SystemConfigKeys.SFTP_URL.replaceAll("<OPERATOR>", operator));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SftpFileDto> request = new HttpEntity<SftpFileDto>(sftpFileDto, headers);
            log.info("Calling URL for Sftp File Request:{}, Url:{}", sftpFileDto, URL);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(URL, request, String.class);
            log.info("Response URL for Sftp File Request:{}, Response:{}", sftpFileDto, responseEntity);
            copyStatus = CopyStatus.COPIED;
        } catch (Exception e) {
            log.error("Error while URL for Sftp File Error:{} Request:{}", e.getMessage(), sftpFileDto, e);
            sendAlert(URL == null ? "Url not configured for operator " + operator : URL, e.getMessage());
        }
        return copyStatus;
    }

    public void sendCopyFileInfo(List<ListFileManagement> listFileManagementList) {
        for (ListFileManagement listFileManagement : listFileManagementList) {
            log.info("Going to call SFTP URL for record:{}", listFileManagement);
            SftpDestinationDto destinationDto = new SftpDestinationDto();
            destinationDto.setDestServerName(listFileManagement.getDestinationServer());
            destinationDto.setDestFilePath(listFileManagement.getDestinationPath());
            SftpFileDto sftpFileDto = SftpFileDto.builder()
                    .txnId(String.valueOf(listFileManagement.getId()))
                    .sourceFileName(listFileManagement.getFileName())
                    .applicationName(appConfig.getFeatureName())
                    .destination(Collections.singletonList(destinationDto))
                    .sourceFilePath(listFileManagement.getFilePath())
                    .serverName(serverName)
                    .fileType(FileType.getByIndex(listFileManagement.getFileType()).name())
                    .remarks("")
                    .sourceServerName(listFileManagement.getSourceServer()).build();
            CopyStatus copyStatus = callUrl(sftpFileDto, listFileManagement.getOperatorName());
            listFileManagement.setCopyStatus(copyStatus.getIndex());
            listFileManagementService.save(listFileManagement);
        }
    }

    public void sendAlert(String url, String exception) {
        Map<AlertMessagePlaceholders, String> map = new HashMap<>();
        map.put(AlertMessagePlaceholders.EXCEPTION, exception);
        map.put(AlertMessagePlaceholders.URL, url);
        alertService.sendAlert(AlertIds.FILE_COPY_URL_EXCEPTION, map);
    }

    private void syncNotCopied() {
        log.info("Syncing process started");
        while (true) {
            try {
                List<ListFileManagement> list = listFileManagementService.getAllNotCopiedFiles();
                for (ListFileManagement listFileManagement : list) {
                    log.info("Going to call SFTP URL for record:{}", listFileManagement);
                    SftpDestinationDto destinationDto = new SftpDestinationDto();
                    destinationDto.setDestServerName(listFileManagement.getDestinationServer());
                    destinationDto.setDestFilePath(listFileManagement.getDestinationPath());
                    SftpFileDto sftpFileDto = SftpFileDto.builder()
                            .txnId(String.valueOf(listFileManagement.getId()))
                            .sourceFileName(listFileManagement.getFileName())
                            .applicationName(appConfig.getFeatureName())
                            .destination(Collections.singletonList(destinationDto))
                            .sourceFilePath(listFileManagement.getFilePath())
                            .serverName(serverName)
                            .fileType(FileType.getByIndex(listFileManagement.getFileType()).name())
                            .remarks("")
                            .sourceServerName(listFileManagement.getSourceServer()).build();
                    CopyStatus copyStatus = callUrl(sftpFileDto, listFileManagement.getOperatorName());
                    listFileManagement.setCopyStatus(copyStatus.getIndex());
                    listFileManagementService.save(listFileManagement);
                    try {
                        TimeUnit.MINUTES.sleep(sftpUrlRetryTime);
                    } catch (Exception e) {
                        log.error("Error while SFTP not copied files Error:{}", e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                log.error("Error while SFTP not copied files Error:{}", e.getMessage(), e);
            }
            try {
                TimeUnit.MINUTES.sleep(sftpUrlRetryTime);
            } catch (Exception e) {
                log.error("Error while SFTP not copied files Error:{}", e.getMessage(), e);
            }
        }
    }
}
