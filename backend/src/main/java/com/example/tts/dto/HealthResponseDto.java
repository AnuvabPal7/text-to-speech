package com.example.tts.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public class HealthResponseDto {

    private String status;
    private String provider;
    private boolean providerConfigured;
    private String databaseStatus;
    private OffsetDateTime timestamp;
    private Map<String, Object> details;

    public HealthResponseDto() {
        this.timestamp = OffsetDateTime.now();
    }

    public HealthResponseDto(String status, String provider, boolean providerConfigured, String databaseStatus, Map<String, Object> details) {
        this.status = status;
        this.provider = provider;
        this.providerConfigured = providerConfigured;
        this.databaseStatus = databaseStatus;
        this.timestamp = OffsetDateTime.now();
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isProviderConfigured() {
        return providerConfigured;
    }

    public void setProviderConfigured(boolean providerConfigured) {
        this.providerConfigured = providerConfigured;
    }

    public String getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(String databaseStatus) {
        this.databaseStatus = databaseStatus;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
