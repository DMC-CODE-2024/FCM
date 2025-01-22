package com.eirs.fcm.scheduler;

import com.eirs.fcm.config.AppConfig;
import com.eirs.fcm.constants.FileType;
import com.eirs.fcm.repository.FileCreationPointerRepository;
import com.eirs.fcm.repository.entity.FileCreationPointer;
import com.eirs.fcm.repository.entity.ModuleAuditTrail;
import com.eirs.fcm.service.ModuleAlertService;
import com.eirs.fcm.service.ModuleAuditTrailService;
import com.eirs.fcm.writter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Autowired
    ModuleAuditTrailService moduleAuditTrailService;

    @Autowired
    ModuleAlertService moduleAlertService;

    @Autowired
    private AppConfig appConfig;
    final String MODULE_NAME = "eir";

    private final String dayType = "DAILY";

    private final String weekType = "WEEKLY";

    @Value("${scheduler.daily.enable:true}")
    private Boolean isDailyCronEnabled = false;

    @Value("${scheduler.weekly.enable:true}")
    private Boolean isWeeklyCronEnabled = false;

    //    @Scheduled(cron = "${scheduler.daily.cronjob}")
    public void dailyScheduler(LocalDate localDate) {
        String action = "DAILY";
        if (!moduleAuditTrailService.runProcess(localDate, appConfig.getFeatureName(), action)) {
            log.info("Process:{} will not execute it may already Running or Completed for the day {}", appConfig.getFeatureName(), localDate);
            return;
        }
        AtomicInteger counter = new AtomicInteger(0);
        moduleAuditTrailService.createAudit(ModuleAuditTrail.builder().action(action).createdOn(LocalDateTime.of(localDate, LocalTime.now())).moduleName(MODULE_NAME).featureName(appConfig.getFeatureName()).build());
        Long start = System.currentTimeMillis();
        ModuleAuditTrail updateModuleAuditTrail = ModuleAuditTrail.builder().moduleName(MODULE_NAME).featureName(appConfig.getFeatureName()).action(action).build();
        if (!isDailyCronEnabled) {
            updateModuleAuditTrail.setStatusCode(500);
            updateModuleAuditTrail.setTimeTaken(System.currentTimeMillis() - start);
            updateModuleAuditTrail.setCount(counter.get());
            moduleAuditTrailService.updateAudit(updateModuleAuditTrail);
            return;
        }
        try {
            log.info("Daily Cronjob started at {} WeekDay:{}", LocalDateTime.now(), LocalDateTime.now().getDayOfWeek().getValue());
            LocalDateTime endDate = LocalDateTime.of(localDate, LocalTime.of(0, 0, 0));
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
                LocalDateTime startDate = LocalDateTime.of(localDate.minusDays(1), LocalTime.of(0, 0, 0));
                log.info("Daily Cronjob started at {} for startDate[{}] and endDate[{}]", LocalDateTime.now(), startDate, endDate);
                writeFullFiles(startDate, endDate, FileType.DAILY_FULL);
                writeIncrementalFiles(startDate, endDate, FileType.DAILY_INCREMENTAL);
                FileCreationPointer fileCreationPointer1 = new FileCreationPointer();
                fileCreationPointer1.setCreatedTill(startDate);
                fileCreationPointer1.setType(dayType);
                fileCreationPointerRepository.save(fileCreationPointer1);
            }
        } catch (Exception e) {
            moduleAlertService.sendModuleExecutionAlert(e.getMessage(), appConfig.getFeatureName());
            log.error("Error while Processing dailyScheduler for Date:{} Error:{} ", localDate, e.getMessage(), e);
            updateModuleAuditTrail.setStatusCode(500);
        }
        updateModuleAuditTrail.setStatusCode(200);
        updateModuleAuditTrail.setTimeTaken(System.currentTimeMillis() - start);
        updateModuleAuditTrail.setCount(counter.get());
        moduleAuditTrailService.updateAudit(updateModuleAuditTrail);
    }

    //    @Scheduled(cron = "${scheduler.weekly.cronjob}")
    public void weeklyScheduler(LocalDate localDate) {
        String action = "WEEKLY";
        if (!moduleAuditTrailService.runProcess(localDate, appConfig.getFeatureName(), action)) {
            log.info("Process:{} will not execute it may already Running or Completed for the day {}", appConfig.getFeatureName(), localDate);
            return;
        }
        AtomicInteger counter = new AtomicInteger(0);
        moduleAuditTrailService.createAudit(ModuleAuditTrail.builder().action(action).createdOn(LocalDateTime.of(localDate, LocalTime.now())).moduleName(MODULE_NAME).featureName(appConfig.getFeatureName()).build());
        Long start = System.currentTimeMillis();
        ModuleAuditTrail updateModuleAuditTrail = ModuleAuditTrail.builder().action(action).moduleName(MODULE_NAME).featureName(appConfig.getFeatureName()).build();
        if (!isWeeklyCronEnabled) {
            updateModuleAuditTrail.setStatusCode(500);
            updateModuleAuditTrail.setTimeTaken(System.currentTimeMillis() - start);
            updateModuleAuditTrail.setCount(counter.get());
            moduleAuditTrailService.updateAudit(updateModuleAuditTrail);
            return;
        }
        try {
            log.info("Weekly Cronjob started at {}", LocalDateTime.now());
            LocalDateTime endDate = LocalDateTime.of(localDate, LocalTime.of(0, 0, 0));
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
                LocalDateTime startDate = LocalDateTime.of(localDate.minusDays(7), LocalTime.of(0, 0, 0));
                log.info("Weekly Cronjob started at {} for startDate[{}] and endDate[{}]", LocalDateTime.now(), startDate, endDate);
                writeFullFiles(startDate, endDate, FileType.WEEKLY_FULL);
                writeIncrementalFiles(startDate, endDate, FileType.WEEKLY_INCREMENTAL);
                FileCreationPointer fileCreationPointer1 = new FileCreationPointer();
                fileCreationPointer1.setCreatedTill(startDate);
                fileCreationPointer1.setType(weekType);
                fileCreationPointerRepository.save(fileCreationPointer1);
            }
        } catch (Exception e) {
            moduleAlertService.sendModuleExecutionAlert(e.getMessage(), appConfig.getFeatureName());
            log.error("Error while Processing dailyScheduler for Date:{} Error:{} ", localDate, e.getMessage(), e);
            updateModuleAuditTrail.setStatusCode(500);
        }
        updateModuleAuditTrail.setStatusCode(200);
        updateModuleAuditTrail.setTimeTaken(System.currentTimeMillis() - start);
        updateModuleAuditTrail.setCount(counter.get());
        moduleAuditTrailService.updateAudit(updateModuleAuditTrail);
    }

    public void writeFullFiles(LocalDateTime startDate, LocalDateTime endDate, FileType type) throws Exception {
        blacklistDeviceWriter.writeFullData(startDate, endDate, type);

        exceptionlistDeviceWriter.writeFullData(startDate, endDate, type);

        allowedTacWriter.writeFullData(startDate, endDate, type);

        trackedlistDeviceWriter.writeFullData(startDate, endDate, type);

        blockedTacWriter.writeFullData(startDate, endDate, type);
    }

    public void writeIncrementalFiles(LocalDateTime startDate, LocalDateTime endDate, FileType type) throws Exception {
        blacklistDeviceWriter.writeIncrementalData(startDate, endDate, type);

        exceptionlistDeviceWriter.writeIncrementalData(startDate, endDate, type);

        allowedTacWriter.writeIncrementalData(startDate, endDate, type);

        trackedlistDeviceWriter.writeIncrementalData(startDate, endDate, type);

        blockedTacWriter.writeIncrementalData(startDate, endDate, type);
    }
}
