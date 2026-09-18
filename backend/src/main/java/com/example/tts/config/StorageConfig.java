package com.example.tts.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StorageConfig {

    private static final Logger log = LoggerFactory.getLogger(StorageConfig.class);
    private final TtsProperties properties;

    public StorageConfig(TtsProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initStorage() {
        try {
            Path storagePath = Paths.get(properties.getStorageDir());
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
                log.info("Initialized TTS audio storage directory at: {}", storagePath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to initialize TTS storage directory: {}", e.getMessage(), e);
        }
    }
}
