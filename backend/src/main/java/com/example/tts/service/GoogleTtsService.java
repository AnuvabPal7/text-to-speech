package com.example.tts.service;

import com.example.tts.config.TtsProperties;
import com.example.tts.dto.TtsRequestDto;
import com.example.tts.exception.TtsProviderException;
import com.example.tts.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "tts.provider", havingValue = "google")
public class GoogleTtsService implements TtsService {

    private static final Logger log = LoggerFactory.getLogger(GoogleTtsService.class);

    private final TtsProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${google.tts.api-key:${TTS_API_KEY:}}")
    private String apiKey;

    public GoogleTtsService(TtsProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public byte[] synthesizeSpeech(TtsRequestDto request) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new UnauthorizedException("Google Cloud TTS API Key is not configured. Please set GOOGLE_TTS_API_KEY.");
        }

        String endpoint = "https://texttospeech.googleapis.com/v1/text:synthesize?key=" + apiKey;

        Map<String, Object> inputMap = Map.of("text", request.getText());
        Map<String, Object> voiceMap = Map.of(
                "languageCode", request.getLanguage(),
                "name", request.getVoice()
        );
        Map<String, Object> audioConfigMap = new HashMap<>();
        audioConfigMap.put("audioEncoding", "MP3");
        if (request.getSpeed() != null) {
            audioConfigMap.put("speakingRate", request.getSpeed());
        }
        if (request.getPitch() != null) {
            audioConfigMap.put("pitch", (request.getPitch() - 1.0) * 10.0);
        }

        Map<String, Object> requestBody = Map.of(
                "input", inputMap,
                "voice", voiceMap,
                "audioConfig", audioConfigMap
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                String audioContentBase64 = json.path("audioContent").asText();
                return Base64.getDecoder().decode(audioContentBase64);
            }

            throw new TtsProviderException("Unexpected response from Google TTS API", response.getStatusCode().value());

        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden ex) {
            log.error("Google TTS Authentication failed: {}", ex.getMessage());
            throw new UnauthorizedException("Invalid Google Cloud TTS API key or permissions.");

        } catch (Exception ex) {
            log.error("Error calling Google TTS API: {}", ex.getMessage(), ex);
            throw new TtsProviderException("Google TTS error: " + ex.getMessage(), 500);
        }
    }

    @Override
    public String getProviderName() {
        return "Google Cloud Text-to-Speech";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}
