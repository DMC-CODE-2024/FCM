package com.eirs.fcm.service;

import com.eirs.fcm.constants.CopyStatus;
import com.eirs.fcm.constants.FileState;
import com.eirs.fcm.constants.FileType;
import com.eirs.fcm.constants.ListType;
import com.eirs.fcm.repository.ListFileManagementRepository;
import com.eirs.fcm.repository.entity.ListFileManagement;
import com.eirs.fcm.repository.entity.SystemConfigKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListFileManagementService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SystemConfigurationService systemConfigurationService;
    @Autowired
    private ListFileManagementRepository listFileManagementRepository;

    public ListFileManagement save(ListFileManagement listFileManagement) {
        log.info("Going to Save listFileManagement {}", listFileManagement);
        ListFileManagement savedListFileManagement = listFileManagementRepository.save(listFileManagement);
        log.info("Saved listFileManagement {}", savedListFileManagement);
        return savedListFileManagement;
    }

    private List<ListFileManagement> saveListManagementEntity(String operator, ListType listType, FileType fileType, String sourceFilePath, String sourceFileName, Long totalCount) {
        String sourceServerName = systemConfigurationService.findByKey(SystemConfigKeys.SOURCE_SERVER);
        Integer noOfDestination = systemConfigurationService.findByKey(SystemConfigKeys.NO_OF_DEST.replaceAll("<OPERATOR>", operator), 0);
        List<ListFileManagement> list = new ArrayList<>();
        for (int i = 1; i <= noOfDestination; i++) {
            String destServerName = systemConfigurationService.findByKey(SystemConfigKeys.DEST_SERVER.replaceAll("<OPERATOR>", operator) + i, "NOT_FOUND");
            String destFilePath = null;
            if ("daily".equalsIgnoreCase(fileType.getValue()))
                destFilePath = systemConfigurationService.findByKey(SystemConfigKeys.DEST_DAILY_FILE_PATH.replaceAll("<OPERATOR>", operator) + i, "NOT_FOUND");
            else
                destFilePath = systemConfigurationService.findByKey(SystemConfigKeys.DEST_WEEKLY_FILE_PATH.replaceAll("<OPERATOR>", operator) + i, "NOT_FOUND");

            ListFileManagement listFileManagement = new ListFileManagement();
            listFileManagement.setFileType(fileType.getIndex());
            listFileManagement.setFileState(FileState.COMPLETED.getIndex());
            listFileManagement.setFileName(sourceFileName);
            listFileManagement.setFilePath(sourceFilePath);
            listFileManagement.setListType(listType);
            listFileManagement.setCopyStatus(CopyStatus.NEW.getIndex());
            listFileManagement.setCreatedOn(LocalDateTime.now());
            listFileManagement.setDestinationPath(destFilePath);
            listFileManagement.setModifiedOn(LocalDateTime.now());
            listFileManagement.setDestinationServer(destServerName);
            listFileManagement.setOperatorName(operator);
            listFileManagement.setRecordCount(totalCount);
            listFileManagement.setSourceServer(sourceServerName);
            list.add(save(listFileManagement));
        }
        return list;
    }

    public List<ListFileManagement> saveListManagement(String operator, ListType listType, FileType fileType, String sourceFilePath, String sourceFileName, Long totalCount) {
        List<ListFileManagement> list = new ArrayList<>();
        if (SystemConfigurationService.blankOperator.equals(operator)) {
            for (String op : systemConfigurationService.getOperators()) {
                list.addAll(saveListManagementEntity(op, listType, fileType, sourceFilePath, sourceFileName, totalCount));
            }
        } else {
            list.addAll(saveListManagementEntity(operator, listType, fileType, sourceFilePath, sourceFileName, totalCount));
        }
        return list;
    }


    public List<ListFileManagement> getAllNotCopiedFiles() {
        log.info("Getting all records which are not copied");
        return listFileManagementRepository.findByCopyStatus(0);
    }

}
