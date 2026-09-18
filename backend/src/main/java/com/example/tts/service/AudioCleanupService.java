package com.example.tts.service;

import com.example.tts.config.TtsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Periodically deletes generated audio files older than the configured
 * retention window (tts.retention-hours), so storage/audio doesn't grow
 * forever on disk.
 * <p>
 * Note: this only removes the physical file, not the speech_history row
 * in the database. Old history entries will still show up in the list,
 * but their "play again" link will 404 once the file has expired - same
 * trade-off most TTS demo apps make to avoid storing audio permanently.
 */
@Service
public class AudioCleanupService {

    private static final Logger log = LoggerFactory.getLogger(AudioCleanupService.class);

    private final TtsProperties properties;

    public AudioCleanupService(TtsProperties properties) {
        this.properties = properties;
    }

    // Runs once an hour. fixedRate is measured from the start of the previous
    // run, so a lightweight scan like this one will never overlap itself.
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanupExpiredAudioFiles() {
        int retentionHours = properties.getRetentionHours();
        if (retentionHours <= 0) {
            log.debug("Audio retention disabled (tts.retention-hours <= 0); skipping cleanup.");
            return;
        }

        Path storageDir = Paths.get(properties.getStorageDir());
        if (!Files.isDirectory(storageDir)) {
            return;
        }

        Instant cutoff = Instant.now().minus(retentionHours, ChronoUnit.HOURS);
        int deletedCount = 0;

        try (DirectoryStream<Path> files = Files.newDirectoryStream(storageDir)) {
            for (Path file : files) {
                if (Files.isRegularFile(file) && isOlderThan(file, cutoff)) {
                    try {
                        Files.delete(file);
                        deletedCount++;
                    } catch (IOException e) {
                        log.warn("Could not delete expired audio file {}: {}", file, e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to scan audio storage directory for cleanup: {}", e.getMessage(), e);
        }

        if (deletedCount > 0) {
            log.info("Audio cleanup: removed {} file(s) older than {} hour(s).", deletedCount, retentionHours);
        }
    }

    private boolean isOlderThan(Path file, Instant cutoff) {
        try {
            Instant lastModified = Files.getLastModifiedTime(file).toInstant();
            return lastModified.isBefore(cutoff);
        } catch (IOException e) {
            log.warn("Could not read last-modified time for {}: {}", file, e.getMessage());
            return false;
        }
    }
}