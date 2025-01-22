package com.eirs.fcm;

import com.eirs.fcm.scheduler.Scheduler;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDate;

@SpringBootApplication
@EnableScheduling
@EnableEncryptableProperties
public class EagleFileCreationModuleApplication {

    private static final Logger logger = LoggerFactory.getLogger(EagleFileCreationModuleApplication.class);

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(EagleFileCreationModuleApplication.class, args);
        try {
            if ("Weekly".equalsIgnoreCase(args[0])) {
                context.getBean(Scheduler.class).weeklyScheduler(LocalDate.now());
            } else if ("Daily".equalsIgnoreCase(args[0])) {
                context.getBean(Scheduler.class).dailyScheduler(LocalDate.now());
            } else {
                logger.error("Please Pass Weekly OR Daily to Run FCM Not this:{} ", args[0]);
            }
        } catch (Exception e) {
            logger.error("Error while running FCM {}", e.getMessage(), e);
        }

        System.exit(0);
    }

}
