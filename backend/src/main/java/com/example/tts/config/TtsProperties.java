package com.example.tts.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tts")
public class TtsProperties {

    /**
     * TTS Provider: polly, google
     */
    private String provider = "polly";

    /**
     * Maximum allowed text character length
     */
    private int maxTextLength = 5000;

    /**
     * Audio output format (mp3, wav, ogg)
     */
    private String audioFormat = "mp3";

    /**
     * Storage directory for generated audio files
     */
    private String storageDir = "./storage/audio";

    /**
     * How many hours to keep generated audio files on disk before the
     * cleanup job deletes them. Set to 0 (or negative) to disable cleanup.
     */
    private int retentionHours = 24;

    // Getters and Setters
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public int getMaxTextLength() {
        return maxTextLength;
    }

    public void setMaxTextLength(int maxTextLength) {
        this.maxTextLength = maxTextLength;
    }

    public String getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(String audioFormat) {
        this.audioFormat = audioFormat;
    }

    public String getStorageDir() {
        return storageDir;
    }

    public void setStorageDir(String storageDir) {
        this.storageDir = storageDir;
    }

    public int getRetentionHours() {
        return retentionHours;
    }

    public void setRetentionHours(int retentionHours) {
        this.retentionHours = retentionHours;
    }
}