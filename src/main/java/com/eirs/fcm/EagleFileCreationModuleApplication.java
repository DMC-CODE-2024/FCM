package com.eirs.fcm;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@EnableScheduling
@EnableEncryptableProperties
public class EagleFileCreationModuleApplication {


	public static void main(String[] args) {
		SpringApplication.run(EagleFileCreationModuleApplication.class, args);
	}

}
