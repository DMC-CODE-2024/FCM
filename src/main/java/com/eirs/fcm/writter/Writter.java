package com.eirs.fcm.writter;

import com.eirs.fcm.constants.FileType;
import com.eirs.fcm.utils.DateFormatterConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Writter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public boolean createFile(String filepath) {
        try {
            File file = new File(filepath);
            file.getParentFile().mkdirs();
            file.createNewFile();
            return true;
        } catch (IOException e) {
            log.info("Error while creating file :{} Error:{}", filepath, e.getMessage());
            return false;
        }
    }

    public boolean moveFile(String sourcePath, String destinationPath) {
        try {
            Files.move(Paths.get(sourcePath), Paths.get(destinationPath), REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            log.error("Error while Move source file:{} destination:{} Error:{}", sourcePath, destinationPath, e.getMessage());
            return false;
        }
    }

    public boolean copyFile(String sourcePath, String destinationPath) {
        try {
            Files.copy(Paths.get(sourcePath), Paths.get(destinationPath), REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            log.error("Error while Copy source file:{} destination:{} Error:{}", sourcePath, destinationPath, e.getMessage());
            return false;
        }
    }

    public boolean deleteFile(String sourcePath) {
        try {
            Files.delete(Paths.get(sourcePath));
            return true;
        } catch (IOException e) {
            log.error("Error while deleting file:{} Error:{}", sourcePath, e.getMessage());
            return false;
        }
    }

    private String getFilename(LocalDateTime startDate, LocalDateTime endDate, FileType fileType, String filePrefix) {
        String filename = "";
        switch (fileType) {
            case DAILY_FULL -> {
                filename = filePrefix + "_FULL_" + endDate.format(DateFormatterConstants.fileSuffixDateFormat);
            }
            case DAILY_INCREMENTAL -> {
                filename = filePrefix + "_INCREMENTAL_" + endDate.format(DateFormatterConstants.fileSuffixDateFormat);
            }
            case WEEKLY_FULL -> {
                filename = filePrefix + "_FULL_" + fileType.getValue().toLowerCase() + "_" + endDate.format(DateFormatterConstants.fileSuffixDateFormat);
            }
            case WEEKLY_INCREMENTAL -> {
                filename = filePrefix + "_INCREMENTAL_" + fileType.getValue().toLowerCase() + "_" + endDate.format(DateFormatterConstants.fileSuffixDateFormat);
            }
        }
        return filename + ".csv";
    }

    public String getFilename(LocalDateTime startDate, LocalDateTime endDate, FileType fileType, String filePrefix, String shortCode) {
        if (StringUtils.isBlank(shortCode))
            return getFilename(startDate, endDate, fileType, filePrefix);
        return shortCode + "_" + getFilename(startDate, endDate, fileType, filePrefix);
    }
}
