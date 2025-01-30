package com.eirs.fcm.writter;

import com.eirs.fcm.client.SftpFileService;
import com.eirs.fcm.constants.AlertIds;
import com.eirs.fcm.constants.AlertMessagePlaceholders;
import com.eirs.fcm.constants.FileType;
import com.eirs.fcm.constants.ListType;
import com.eirs.fcm.repository.entity.*;
import com.eirs.fcm.service.ListFileManagementService;
import com.eirs.fcm.service.ModuleAlertService;
import com.eirs.fcm.service.SystemConfigurationService;
import com.eirs.fcm.utils.DateFormatterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AllowedTacWriter extends Writter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${files.path}")
    private String filePath;
    private final String filePrefix = "ALLOWEDTACLIST";

    private final String fullFileHeader = "TAC";
    private final String incrementFileHeader = "Operation,TAC";

    @Autowired
    SftpFileService sftpFileService;

    @Autowired
    ListFileManagementService listFileManagementService;

    @Autowired
    SystemConfigurationService systemConfigurationService;

    @Autowired
    ModuleAlertService alertService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = false)
    public void writeFullData(LocalDateTime startDate, LocalDateTime endDate, FileType fileType) throws Exception {
        if (systemConfigurationService.isEnabled(SystemConfigKeys.ENABLE_ALLOWED_TAC_FULL_FILE)) {
            log.info("Going to write Allowed Tac for Full File startDate:{} EndDate:{} fileType:{}", startDate, endDate, fileType);
            String filename = getFilename(startDate, endDate, fileType, filePrefix, null);
            String tempFilepath = filePath + "/temp/common/" + filename;

            PrintWriter writer = null;
            AtomicLong atomicLong = new AtomicLong(0);
            try {
                createFile(tempFilepath);

                writer = new PrintWriter(tempFilepath);
                writer.println(fullFileHeader);
                PrintWriter finalWriter = writer;

                String query = "SELECT id,tac,created_on from allowed_tac";
                log.info("JDBC Template Selecting Records with Query:[{}]", query);
                jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
                jdbcTemplate.query(query, new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        TacData recordDataDto = new TacData();
                        recordDataDto.setTac(rs.getString("tac"));
                        recordDataDto.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
                        finalWriter.println(recordDataDto.toCsv());
                        atomicLong.incrementAndGet();
                    }
                });
            } catch (DataAccessException e) {
                alertService.sendDatabaseAlert(e.getMessage(), ListType.ALLOWEDTACLIST);
                log.error("Error While getting Data Allow tac Error:{}", e.getMessage(), e);
            } catch (Exception e) {
                log.error("Error While creating file Allow tac Error:{}", e.getMessage(), e);
                sendAlert(e.getMessage(), fileType);
            } finally {
                if (writer != null)
                    writer.close();
                List<String> operators = systemConfigurationService.getOperators();
                for (String operator : operators) {
                    String shortCode = systemConfigurationService.getShortCode(operator);
                    String operatorFile = getFilename(startDate, endDate, fileType, filePrefix, shortCode);
                    operator = operator.toLowerCase();
                    String filepath = filePath + "/" + operator + "/" + fileType.getValue() + "/";
                    createFile(filepath + operatorFile);
                    copyFile(tempFilepath, filepath + operatorFile);
                    List<ListFileManagement> listFileManagementList = listFileManagementService.saveListManagement(operator, ListType.ALLOWEDTACLIST, fileType, filepath, filename, atomicLong.get());
                    sftpFileService.sendCopyFileInfo(listFileManagementList);
                }
                deleteFile(tempFilepath);

            }
        } else {
            log.info("Allow Tac Full File is disabled Enable from Config");
        }
    }

    @Transactional(readOnly = false)
    public void writeIncrementalData(LocalDateTime startDate, LocalDateTime endDate, FileType fileType) throws
            Exception {
        if (systemConfigurationService.isEnabled(SystemConfigKeys.ENABLE_ALLOWED_TAC_INCREMENT_FILE)) {
            log.info("Going to write Allowed Tac for Incremental table startDate:{} EndDate:{} fileType:{}", startDate, endDate, fileType);
            String filename = getFilename(startDate, endDate, fileType, filePrefix, null);
            String tempFilepath = filePath + "/temp/common/" + filename;

            PrintWriter writer = null;
            AtomicLong atomicLong = new AtomicLong(0);
            try {
                createFile(tempFilepath);

                writer = new PrintWriter(tempFilepath);
                writer.println(incrementFileHeader);
                PrintWriter finalWriter = writer;

                String query = "SELECT id,tac,created_on,operation from allowed_tac_his where created_on >= '" + startDate.format(DateFormatterConstants.simpleDateFormat) + "' and created_on < '" + endDate.format(DateFormatterConstants.simpleDateFormat) + "'";
                log.info("JDBC Template Selecting Records with Query:[{}]", query);
                jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
                jdbcTemplate.query(query, new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        TacDataHis recordDataDto = new TacDataHis();
                        recordDataDto.setTac(rs.getString("tac"));
                        recordDataDto.setOperation(rs.getInt("operation"));
                        recordDataDto.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
                        finalWriter.println(recordDataDto.toCsv());
                        atomicLong.incrementAndGet();
                    }
                });
            } catch (DataAccessException e) {
                alertService.sendDatabaseAlert(e.getMessage(), ListType.ALLOWEDTACLIST);
                log.error("Error While getting Data Allow tac Error:{}", e.getMessage(), e);
            } catch (Exception e) {
                sendAlert(e.getMessage(), fileType);
                log.error("Error While creating file Allow Tac Error:{}", e.getMessage(), e);
            } finally {
                if (writer != null)
                    writer.close();

                List<String> operators = systemConfigurationService.getOperators();
                for (String operator : operators) {
                    String shortCode = systemConfigurationService.getShortCode(operator);
                    String operatorFile = getFilename(startDate, endDate, fileType, filePrefix, shortCode);
                    operator = operator.toLowerCase();
                    String filepath = filePath + "/" + operator + "/" + fileType.getValue() + "/";
                    createFile(filepath + operatorFile);
                    copyFile(tempFilepath, filepath + operatorFile);
                    List<ListFileManagement> listFileManagementList = listFileManagementService.saveListManagement(operator, ListType.ALLOWEDTACLIST, fileType, filepath, filename, atomicLong.get());
                    sftpFileService.sendCopyFileInfo(listFileManagementList);
                }
                deleteFile(tempFilepath);
            }
        } else {
            log.info("Allow Tac Incremental File is disabled Enable from Config");
        }
    }

    public void sendAlert(String exception, FileType fileType) {
        Map<AlertMessagePlaceholders, String> map = new HashMap<>();
        map.put(AlertMessagePlaceholders.EXCEPTION, exception);
        map.put(AlertMessagePlaceholders.LIST, ListType.ALLOWEDTACLIST.name());
        map.put(AlertMessagePlaceholders.FILE_TYPE, fileType.name());
        alertService.sendAlert(AlertIds.FILE_CREATION_ERROR, map);
    }
}
