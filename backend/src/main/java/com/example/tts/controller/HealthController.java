package com.example.tts.controller;

import com.example.tts.config.AwsPollyProperties;
import com.example.tts.config.TtsProperties;
import com.example.tts.dto.HealthResponseDto;
import com.example.tts.service.TtsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final TtsService ttsService;
    private final TtsProperties ttsProperties;
    private final AwsPollyProperties awsPollyProperties;
    private final DataSource dataSource;

    public HealthController(TtsService ttsService,
                            TtsProperties ttsProperties,
                            AwsPollyProperties awsPollyProperties,
                            DataSource dataSource) {
        this.ttsService = ttsService;
        this.ttsProperties = ttsProperties;
        this.awsPollyProperties = awsPollyProperties;
        this.dataSource = dataSource;
    }

    /**
     * GET /api/health
     * Returns application health status, database connection, and TTS provider configuration.
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponseDto> getHealth() {
        String dbStatus = "CONNECTED";
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(2)) {
                dbStatus = "DEGRADED";
            }
        } catch (Exception e) {
            dbStatus = "DISCONNECTED (" + e.getMessage() + ")";
        }

        Map<String, Object> details = new HashMap<>();
        details.put("providerName", ttsService.getProviderName());
        details.put("region", awsPollyProperties.getRegion());
        details.put("defaultVoice", awsPollyProperties.getPolly().getDefaultVoice());
        details.put("outputFormat", awsPollyProperties.getPolly().getOutputFormat());
        details.put("engine", awsPollyProperties.getPolly().getEngine());
        details.put("maxTextLength", ttsProperties.getMaxTextLength());
        details.put("hasCredentials", awsPollyProperties.hasCredentials());
        details.put("mode", awsPollyProperties.hasCredentials() ? "PRODUCTION_AWS_POLLY_SYNTHESIS" : "LOCAL_DEVELOPMENT_PREVIEW");

        HealthResponseDto health = new HealthResponseDto(
                "UP",
                ttsService.getProviderName(),
                awsPollyProperties.hasCredentials(),
                dbStatus,
                details
        );

        return ResponseEntity.ok(health);
    }
}
