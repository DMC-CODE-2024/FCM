package com.eirs.fcm.writter;

import com.eirs.fcm.client.SftpFileService;
import com.eirs.fcm.constants.AlertIds;
import com.eirs.fcm.constants.AlertMessagePlaceholders;
import com.eirs.fcm.constants.FileType;
import com.eirs.fcm.constants.ListType;
import com.eirs.fcm.repository.entity.ListDeviceData;
import com.eirs.fcm.repository.entity.ListDeviceDataHis;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
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
public class ExceptionlistDeviceWriter extends Writter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SystemConfigurationService systemConfigurationService;

    private final String filePrefix = "EXCEPTIONLIST";

    @Value("${files.path}")
    private String filePath;

    private final String fullFileHeader = "IMEI,IMSI,MSISDN";
    private final String incrementFileHeader = "Operation,IMEI,IMSI,MSISDN";

    @Autowired
    SftpFileService sftpFileService;

    @Autowired
    ListFileManagementService listFileManagementService;

    @Autowired
    ModuleAlertService alertService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = false)
    public void writeFullData(LocalDateTime startDate, LocalDateTime endDate, FileType fileType) throws Exception {
        if (systemConfigurationService.isEnabled(SystemConfigKeys.ENABLE_EXCEPTION_LIST_FULL_FILE)) {
            log.info("Going to write Exception List devices for Full table startDate:{} EndDate:{} fileType:{}", startDate, endDate, fileType);
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

                    String query = "SELECT id,actual_imei,imei,imsi,msisdn,operator_id,operator_name,created_on from exception_list where operator_name='" + operator + "'";
                    log.info("JDBC Template Selecting Records with Query:[{}]", query);
                    jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
                    jdbcTemplate.query(query, new RowCallbackHandler() {
                        @Override
                        public void processRow(ResultSet rs) throws SQLException {
                            ListDeviceData recordDataDto = new ListDeviceData();
                            recordDataDto.setActualImei(rs.getString("actual_imei"));
                            recordDataDto.setImsi(rs.getString("imsi"));
                            recordDataDto.setMsisdn(rs.getString("msisdn"));
                            recordDataDto.setOperatorName(rs.getString("operator_name"));
                            recordDataDto.setOperatorId(rs.getString("operator_id"));
                            recordDataDto.setImei(rs.getString("imei"));
                            recordDataDto.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
                            finalWriter.println(recordDataDto.toCsv());
                            atomicLong.incrementAndGet();
                        }
                    });

                    query = "SELECT id,actual_imei,imei,imsi,msisdn,operator_id,operator_name,created_on from exception_list where operator_name is NULL";
                    log.info("JDBC Template Selecting Records with Query:[{}]", query);
                    jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
                    jdbcTemplate.query(query, new RowCallbackHandler() {
                        @Override
                        public void processRow(ResultSet rs) throws SQLException {
                            ListDeviceData recordDataDto = new ListDeviceData();
                            recordDataDto.setActualImei(rs.getString("actual_imei"));
                            recordDataDto.setImsi(rs.getString("imsi"));
                            recordDataDto.setMsisdn(rs.getString("msisdn"));
                            recordDataDto.setOperatorName(rs.getString("operator_name"));
                            recordDataDto.setOperatorId(rs.getString("operator_id"));
                            recordDataDto.setImei(rs.getString("imei"));
                            recordDataDto.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
                            finalWriter.println(recordDataDto.toCsv());
                            atomicLong.incrementAndGet();
                        }
                    });
                } catch (DataAccessException e) {
                    alertService.sendDatabaseAlert(e.getMessage(), ListType.EXCEPTIONLIST);
                    log.error("Error While getting Data ExceptionList Error:{}", e.getMessage(), e);
                } catch (Exception e) {
                    log.error("Error While creating file Exception List Error:{}", e.getMessage(), e);
                    sendAlert(e.getMessage(), fileType);
                } finally {
                    if (writer != null) writer.close();
                    createFile(filepath + filename);
                    moveFile(tempFilepath, filepath + filename);
                    List<ListFileManagement> listFileManagementList = listFileManagementService.saveListManagement(operator, ListType.EXCEPTIONLIST, fileType, filepath, filename, atomicLong.get());
                    sftpFileService.sendCopyFileInfo(listFileManagementList);
                }
            }
        } else {
            log.info("Exception List Full File is disabled, Enable from Config");
        }
    }

    @Transactional(readOnly = false)
    public void writeIncrementalData(LocalDateTime startDate, LocalDateTime endDate, FileType fileType) throws Exception {
        if (systemConfigurationService.isEnabled(SystemConfigKeys.ENABLE_EXCEPTION_LIST_INCREMENT_FILE)) {
            log.info("Going to write Exception List devices for Incremental table startDate:{} EndDate:{} fileType:{}", startDate, endDate, fileType);
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

                    String query = "SELECT id,actual_imei,imei,imsi,msisdn,operator_id,operator_name,created_on,operation from exception_list_his where created_on >= '" + startDate.format(DateFormatterConstants.simpleDateFormat) + "' and created_on < '" + endDate.format(DateFormatterConstants.simpleDateFormat) + "' and operator_name='" + operator + "'";
                    log.info("JDBC Template Selecting Records with Query:[{}]", query);
                    jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
                    jdbcTemplate.query(query, new RowCallbackHandler() {
                        @Override
                        public void processRow(ResultSet rs) throws SQLException {
                            ListDeviceDataHis recordDataDto = new ListDeviceDataHis();
                            recordDataDto.setActualImei(rs.getString("actual_imei"));
                            recordDataDto.setImsi(rs.getString("imsi"));
                            recordDataDto.setMsisdn(rs.getString("msisdn"));
                            recordDataDto.setOperatorName(rs.getString("operator_name"));
                            recordDataDto.setOperatorId(rs.getString("operator_id"));
                            recordDataDto.setImei(rs.getString("imei"));
                            recordDataDto.setOperation(rs.getInt("operation"));
                            recordDataDto.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
                            finalWriter.println(recordDataDto.toCsv());
                            atomicLong.incrementAndGet();
                        }
                    });

                    query = "SELECT id,actual_imei,imei,imsi,msisdn,operator_id,operator_name,created_on,operation from exception_list_his where created_on >= '" + startDate.format(DateFormatterConstants.simpleDateFormat) + "' and created_on < '" + endDate.format(DateFormatterConstants.simpleDateFormat) + "' and operator_name is NULL";
                    log.info("JDBC Template Selecting Records with Query:[{}]", query);
                    jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
                    jdbcTemplate.query(query, new RowCallbackHandler() {
                        @Override
                        public void processRow(ResultSet rs) throws SQLException {
                            ListDeviceDataHis recordDataDto = new ListDeviceDataHis();
                            recordDataDto.setActualImei(rs.getString("actual_imei"));
                            recordDataDto.setImsi(rs.getString("imsi"));
                            recordDataDto.setMsisdn(rs.getString("msisdn"));
                            recordDataDto.setOperatorName(rs.getString("operator_name"));
                            recordDataDto.setOperatorId(rs.getString("operator_id"));
                            recordDataDto.setImei(rs.getString("imei"));
                            recordDataDto.setOperation(rs.getInt("operation"));
                            recordDataDto.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
                            finalWriter.println(recordDataDto.toCsv());
                            atomicLong.incrementAndGet();
                        }
                    });
                } catch (DataAccessException e) {
                    alertService.sendDatabaseAlert(e.getMessage(), ListType.EXCEPTIONLIST);
                    log.error("Error While getting Data ExceptionList Error:{}", e.getMessage(), e);
                } catch (Exception e) {
                    log.error("Error While creating file Exception List Error:{}", e.getMessage(), e);
                    sendAlert(e.getMessage(), fileType);
                } finally {
                    if (writer != null) writer.close();
                    createFile(filepath + filename);
                    moveFile(tempFilepath, filepath + filename);
                    List<ListFileManagement> listFileManagementList = listFileManagementService.saveListManagement(operator, ListType.EXCEPTIONLIST, fileType, filepath, filename, atomicLong.get());
                    sftpFileService.sendCopyFileInfo(listFileManagementList);
                }
            }
        } else {
            log.info("Exception List Incremental File is disabled, Enable from Config");
        }
    }

    public void sendAlert(String exception, FileType fileType) {
        Map<AlertMessagePlaceholders, String> map = new HashMap<>();
        map.put(AlertMessagePlaceholders.EXCEPTION, exception);
        map.put(AlertMessagePlaceholders.LIST, ListType.EXCEPTIONLIST.name());
        map.put(AlertMessagePlaceholders.FILE_TYPE, fileType.name());
        alertService.sendAlert(AlertIds.FILE_CREATION_ERROR, map);
    }
}
