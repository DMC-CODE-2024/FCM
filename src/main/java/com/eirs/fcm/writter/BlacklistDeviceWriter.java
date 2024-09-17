package com.eirs.fcm.writter;

import com.eirs.fcm.client.SftpFileService;
import com.eirs.fcm.constants.AlertIds;
import com.eirs.fcm.constants.AlertMessagePlaceholders;
import com.eirs.fcm.constants.FileType;
import com.eirs.fcm.constants.ListType;
import com.eirs.fcm.repository.BlacklistDeviceHisRepository;
import com.eirs.fcm.repository.BlacklistDeviceRepository;
import com.eirs.fcm.repository.entity.BlacklistDevice;
import com.eirs.fcm.repository.entity.BlacklistDeviceHis;
import com.eirs.fcm.repository.entity.ListFileManagement;
import com.eirs.fcm.repository.entity.SystemConfigKeys;
import com.eirs.fcm.service.ListFileManagementService;
import com.eirs.fcm.service.ModuleAlertService;
import com.eirs.fcm.service.SystemConfigurationService;
import com.eirs.fcm.utils.DateFormatterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;


@Repository
public class BlacklistDeviceWriter extends Writter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SystemConfigurationService systemConfigurationService;

    private final String filePrefix = "BLOCKEDLIST";

    @Value("${files.path}")
    private String filePath;

    private final String fullFileHeader = "IMEI,IMSI,MSISDN";
    private final String incrementFileHeader = "Operation,IMEI,IMSI,MSISDN";

    @Autowired
    SftpFileService sftpFileService;

    @Autowired
    private BlacklistDeviceRepository blacklistDeviceRepository;

    @Autowired
    private BlacklistDeviceHisRepository blacklistDeviceHisRepository;

    @Autowired
    ListFileManagementService listFileManagementService;

    @Autowired
    ModuleAlertService alertService;

    @Transactional(readOnly = false)
    public void writeFullData(LocalDateTime startDate, LocalDateTime endDate, FileType fileType) throws Exception {
        if (systemConfigurationService.isEnabled(SystemConfigKeys.ENABLE_BLACKED_LIST_FULL_FILE)) {
            log.info("Going to write Black List devices for Full table startDate:{} EndDate:{} fileType:{}", startDate, endDate, fileType);
            List<String> operators = systemConfigurationService.getOperators();
            for (String operator : operators) {
                String shortCode = systemConfigurationService.getShortCode(operator);
                String filename = getFilename(startDate, endDate, fileType, filePrefix, shortCode);
                String filepath = filePath + "/" + operator.toLowerCase() + "/" + fileType.getValue() + "/";
                String tempFilepath = filePath + "/temp/" + filename;
                PrintWriter writer = null;
                AtomicLong atomicLong = new AtomicLong(0);
                try {
                    createFile(tempFilepath);

                    writer = new PrintWriter(tempFilepath);
                    writer.println(fullFileHeader);
                    PrintWriter finalWriter = writer;

                    try (Stream<BlacklistDevice> stream = blacklistDeviceRepository.streamByOperatorNameIgnoreCase(operator)) {
                        stream.forEach(data -> {
                            finalWriter.println(data.toCsv());
                            atomicLong.incrementAndGet();
                        });
                    }

                    blacklistDeviceRepository.findByOperatorNameIsNull().forEach(data -> {
                        finalWriter.println(data.toCsv());
                        atomicLong.incrementAndGet();
                    });

                } catch (DataAccessException e) {
                    alertService.sendDatabaseAlert(e.getMessage(), ListType.BLACKLIST);
                    log.error("Error While getting Data BlackList Error:{}", e.getMessage(), e);
                } catch (Exception e) {
                    log.error("Error While creating file BlackList Error:{}", e.getMessage(), e);
                    sendAlert(e.getMessage(), fileType);
                } finally {
                    if (writer != null) writer.close();
                    createFile(filepath + filename);
                    moveFile(tempFilepath, filepath + filename);
                    List<ListFileManagement> listFileManagementList = listFileManagementService.saveListManagement(operator, ListType.BLACKLIST, fileType, filepath, filename, atomicLong.get());
                    sftpFileService.sendCopyFileInfo(listFileManagementList);
                }
            }
        } else {
            log.info("Black List Full File is disabled Enable from Config");
        }
    }

    @Transactional(readOnly = false)
    public void writeIncrementalData(LocalDateTime startDate, LocalDateTime endDate, FileType fileType) throws Exception {
        if (systemConfigurationService.isEnabled(SystemConfigKeys.ENABLE_BLACKED_LIST_INCREMENT_FILE)) {
            log.info("Going to write Black List devices for Incremental table startDate:{} EndDate:{} fileType:{}", startDate, endDate, fileType);
            List<String> operators = systemConfigurationService.getOperators();
            for (String operator : operators) {
                String shortCode = systemConfigurationService.getShortCode(operator);
                String filename = getFilename(startDate, endDate, fileType, filePrefix, shortCode);
                String filepath = filePath + "/" + operator.toLowerCase() + "/" + fileType.getValue() + "/";
                String tempFilepath = filePath + "/temp/" + filename;
                PrintWriter writer = null;
                AtomicLong atomicLong = new AtomicLong(0);
                try {
                    createFile(tempFilepath);

                    writer = new PrintWriter(tempFilepath);
                    writer.println(incrementFileHeader);
                    PrintWriter finalWriter = writer;

                    try (Stream<BlacklistDeviceHis> stream = blacklistDeviceHisRepository.streamByOperatorNameAndCreatedOnBetween(operator, startDate, endDate)) {
                        stream.forEach(data -> {
                            finalWriter.println(data.toCsv());
                            atomicLong.incrementAndGet();
                        });
                    }

                    try (Stream<BlacklistDeviceHis> streamCommon = blacklistDeviceHisRepository.streamByCreatedOnBetween(startDate, endDate)) {
                        streamCommon.forEach(data -> {
                            finalWriter.println(data.toCsv());
                            atomicLong.incrementAndGet();
                        });
                    }
                } catch (DataAccessException e) {
                    alertService.sendDatabaseAlert(e.getMessage(), ListType.BLACKLIST);
                    log.error("Error While getting Data BlackList Error:{}", e.getMessage(), e);
                } catch (Exception e) {
                    log.error("Error While creating file BlackList Error:{}", e.getMessage(), e);
                    sendAlert(e.getMessage(), fileType);
                } finally {
                    if (writer != null) writer.close();
                    createFile(filepath + filename);
                    moveFile(tempFilepath, filepath + filename);
                    List<ListFileManagement> listFileManagementList = listFileManagementService.saveListManagement(operator, ListType.BLACKLIST, fileType, filepath, filename, atomicLong.get());
                    sftpFileService.sendCopyFileInfo(listFileManagementList);
                }
            }
        } else {
            log.info("Black List Incremental File is disabled Enable from Config");
        }
    }

    public void sendAlert(String exception, FileType fileType) {
        Map<AlertMessagePlaceholders, String> map = new HashMap<>();
        map.put(AlertMessagePlaceholders.EXCEPTION, exception);
        map.put(AlertMessagePlaceholders.LIST, ListType.BLACKLIST.name());
        map.put(AlertMessagePlaceholders.FILE_TYPE, fileType.name());
        alertService.sendAlert(AlertIds.FILE_CREATION_ERROR, map);
    }
}
