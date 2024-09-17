package com.eirs.fcm.scheduler;

import com.eirs.fcm.constants.FileType;
import com.eirs.fcm.constants.ListType;
import com.eirs.fcm.repository.FileCreationPointerRepository;
import com.eirs.fcm.repository.entity.FileCreationPointer;
import com.eirs.fcm.writter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Component
public class Scheduler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BlacklistDeviceWriter blacklistDeviceWriter;

    @Autowired
    private ExceptionlistDeviceWriter exceptionlistDeviceWriter;

    @Autowired
    private TrackedlistDeviceWriter trackedlistDeviceWriter;

    @Autowired
    private AllowedTacWriter allowedTacWriter;

    @Autowired
    private BlockedTacWriter blockedTacWriter;

    @Autowired
    FileCreationPointerRepository fileCreationPointerRepository;

    private final String dayType = "DAILY";

    private final String weekType = "WEEKLY";

    @Value("${scheduler.daily.enable:true}")
    private Boolean isDailyCronEnabled = false;

    @Value("${scheduler.weekly.enable:true}")
    private Boolean isWeeklyCronEnabled = false;

    @Scheduled(cron = "${scheduler.daily.cronjob}")
    public void dailyScheduler() {
        if (!isDailyCronEnabled)
            return;
        log.info("Daily Cronjob started at {} WeekDay:{}", LocalDateTime.now(), LocalDateTime.now().getDayOfWeek().getValue());
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
        Optional<FileCreationPointer> fileCreationPointer = fileCreationPointerRepository.findByType(dayType);
        if (fileCreationPointer.isPresent()) {
            LocalDateTime createdTill = fileCreationPointer.get().getCreatedTill();
            createdTill = createdTill.plusDays(1);
            while (createdTill.toLocalDate().isBefore(endDate.toLocalDate())) {
                LocalDateTime queryEndDate = createdTill.plusDays(1);
                log.info("Daily Cronjob started at {} for createdTill[{}] and endDate[{}]", LocalDateTime.now(), createdTill, queryEndDate);
                writeFullFiles(createdTill, queryEndDate, FileType.DAILY_FULL);
                writeIncrementalFiles(createdTill, queryEndDate, FileType.DAILY_INCREMENTAL);
                fileCreationPointer.get().setCreatedTill(createdTill);
                fileCreationPointerRepository.save(fileCreationPointer.get());
                createdTill = createdTill.plusDays(1);

            }
        } else {
            LocalDateTime startDate = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(0, 0, 0));
            log.info("Daily Cronjob started at {} for startDate[{}] and endDate[{}]", LocalDateTime.now(), startDate, endDate);
            writeFullFiles(startDate, endDate, FileType.DAILY_FULL);
            writeIncrementalFiles(startDate, endDate, FileType.DAILY_INCREMENTAL);
            FileCreationPointer fileCreationPointer1 = new FileCreationPointer();
            fileCreationPointer1.setCreatedTill(startDate);
            fileCreationPointer1.setType(dayType);
            fileCreationPointerRepository.save(fileCreationPointer1);
        }
    }

    @Scheduled(cron = "${scheduler.weekly.cronjob}")
    public void weeklyScheduler() {
        if (!isWeeklyCronEnabled)
            return;
        log.info("Weekly Cronjob started at {}", LocalDateTime.now());
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
        Optional<FileCreationPointer> fileCreationPointer = fileCreationPointerRepository.findByType(weekType);
        if (fileCreationPointer.isPresent()) {
            LocalDateTime createdTill = fileCreationPointer.get().getCreatedTill();
            createdTill = createdTill.plusDays(7);
            while (createdTill.toLocalDate().isBefore(endDate.toLocalDate())) {
                LocalDateTime queryEndDate = createdTill.plusDays(7);
                log.info("Weekly Cronjob started at {} for createdTill[{}] and endDate[{}]", LocalDateTime.now(), createdTill, queryEndDate);
                writeFullFiles(createdTill, queryEndDate, FileType.WEEKLY_FULL);
                writeIncrementalFiles(createdTill, queryEndDate, FileType.WEEKLY_INCREMENTAL);
                fileCreationPointer.get().setCreatedTill(createdTill);
                fileCreationPointerRepository.save(fileCreationPointer.get());
                createdTill = createdTill.plusDays(7);

            }
        } else {
            LocalDateTime startDate = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.of(0, 0, 0));
            log.info("Weekly Cronjob started at {} for startDate[{}] and endDate[{}]", LocalDateTime.now(), startDate, endDate);
            writeFullFiles(startDate, endDate, FileType.WEEKLY_FULL);
            writeIncrementalFiles(startDate, endDate, FileType.WEEKLY_INCREMENTAL);
            FileCreationPointer fileCreationPointer1 = new FileCreationPointer();
            fileCreationPointer1.setCreatedTill(startDate);
            fileCreationPointer1.setType(weekType);
            fileCreationPointerRepository.save(fileCreationPointer1);
        }
    }

    public void writeFullFiles(LocalDateTime startDate, LocalDateTime endDate, FileType type) {
        try {
            blacklistDeviceWriter.writeFullData(startDate, endDate, type);
        } catch (Exception e) {
            log.error("Getting Exception while Black List devices Files Writing Error:{}", e.getMessage(), e);
        }

        try {
            exceptionlistDeviceWriter.writeFullData(startDate, endDate, type);
        } catch (Exception e) {
            log.error("Getting Exception while Exception List Files Writing Error:{}", e.getMessage());
        }

        try {
            allowedTacWriter.writeFullData(startDate, endDate, type);
        } catch (Exception e) {
            log.error("Getting Exception while Allowed Tac Files Writing Error:{}", e.getMessage());
        }

        try {
            trackedlistDeviceWriter.writeFullData(startDate, endDate, type);
        } catch (Exception e) {
            log.error("Getting Exception while Tracked List Files Writing Error:{}", e.getMessage());
        }

        try {
            blockedTacWriter.writeFullData(startDate, endDate, type);
        } catch (
                Exception e) {
            log.error("Getting Exception while Blocked Tacs Files Writing Error:{}", e.getMessage());
        }

    }

    public void writeIncrementalFiles(LocalDateTime startDate, LocalDateTime endDate, FileType type) {
        try {
            blacklistDeviceWriter.writeIncrementalData(startDate, endDate, type);
        } catch (Exception e) {
            log.error("Getting Exception while Black List devices Files Writing Error:{}", e.getMessage(), e);
        }

        try {
            exceptionlistDeviceWriter.writeIncrementalData(startDate, endDate, type);
        } catch (Exception e) {
            log.error("Getting Exception while Exception List Files Writing Error:{}", e.getMessage());
        }

        try {
            allowedTacWriter.writeIncrementalData(startDate, endDate, type);
        } catch (Exception e) {
            log.error("Getting Exception while Allowed Tac Files Writing Error:{}", e.getMessage());
        }

        try {
            trackedlistDeviceWriter.writeIncrementalData(startDate, endDate, type);
        } catch (Exception e) {
            log.error("Getting Exception while Tracked List Files Writing Error:{}", e.getMessage());
        }

        try {
            blockedTacWriter.writeIncrementalData(startDate, endDate, type);
        } catch (
                Exception e) {
            log.error("Getting Exception while Blocked Tacs Files Writing Error:{}", e.getMessage());
        }

    }
}
