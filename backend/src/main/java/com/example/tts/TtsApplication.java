package com.example.tts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application Entry Point for the Text-to-Speech service.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling // required for AudioCleanupService's @Scheduled job to run
public class TtsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TtsApplication.class, args);
    }
}