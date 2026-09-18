package com.example.tts.controller;

import com.example.tts.config.TtsProperties;
import com.example.tts.dto.TtsRequestDto;
import com.example.tts.dto.TtsResponseDto;
import com.example.tts.exception.InvalidRequestException;
import com.example.tts.exception.ResourceNotFoundException;
import com.example.tts.model.SpeechHistory;
import com.example.tts.service.SpeechHistoryService;
import com.example.tts.service.TtsService;
import com.example.tts.service.VoiceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TtsController {

    private static final Logger log = LoggerFactory.getLogger(TtsController.class);

    private final TtsService ttsService;
    private final VoiceService voiceService;
    private final SpeechHistoryService historyService;
    private final TtsProperties properties;

    public TtsController(TtsService ttsService,
                         VoiceService voiceService,
                         SpeechHistoryService historyService,
                         TtsProperties properties) {
        this.ttsService = ttsService;
        this.voiceService = voiceService;
        this.historyService = historyService;
        this.properties = properties;
    }

    /**
     * POST /api/tts
     * Synthesizes provided text into audio using the configured TTS provider.
     */
    @PostMapping(value = "/tts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TtsResponseDto> generateSpeech(@Valid @RequestBody TtsRequestDto request) {
        String trimmedText = request.getText() != null ? request.getText().trim() : "";
        if (trimmedText.isEmpty()) {
            throw new InvalidRequestException("Text cannot be empty or whitespace.");
        }

        if (trimmedText.length() > properties.getMaxTextLength()) {
            throw new InvalidRequestException(String.format(
                    "Text length (%d characters) exceeds the maximum allowed limit of %d characters.",
                    trimmedText.length(), properties.getMaxTextLength()
            ));
        }

        // Validate language and voice pairing
        voiceService.validateVoiceAndLanguage(request.getVoice(), request.getLanguage());

        log.info("Processing TTS request: {} chars, language='{}', voice='{}'",
                trimmedText.length(), request.getLanguage(), request.getVoice());

        // Perform TTS synthesis
        byte[] audioBytes = ttsService.synthesizeSpeech(request);

        // Determine file extension
        String extension = "mp3";
        if (audioBytes.length >= 4 && audioBytes[0] == 'R' && audioBytes[1] == 'I' && audioBytes[2] == 'F' && audioBytes[3] == 'F') {
            extension = "wav";
        }

        // Save audio to disk
        String filename = UUID.randomUUID() + "." + extension;
        Path storageDir = Paths.get(properties.getStorageDir());
        File storageDirFile = storageDir.toFile();
        if (!storageDirFile.exists()) {
            storageDirFile.mkdirs();
        }

        File targetFile = storageDir.resolve(filename).toFile();
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(audioBytes);
        } catch (IOException e) {
            log.error("Failed to save audio file to disk: {}", e.getMessage());
        }

        String audioUrl = "/api/audio/" + filename;
        int wordCount = trimmedText.split("\\s+").length;

        // Persist to PostgreSQL database
        SpeechHistory savedHistory = historyService.saveHistory(
                trimmedText,
                request.getLanguage(),
                request.getVoice(),
                audioUrl,
                trimmedText.length(),
                wordCount,
                extension,
                (long) audioBytes.length
        );

        TtsResponseDto response = TtsResponseDto.ok(
                audioUrl,
                savedHistory.getId(),
                trimmedText.length(),
                wordCount,
                extension,
                audioBytes.length
        );

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/audio/{filename}
     * Streams generated audio file for playback and downloading.
     */
    @GetMapping("/audio/{filename}")
    public ResponseEntity<Resource> streamAudio(@PathVariable String filename) {
        // Prevent path traversal
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new InvalidRequestException("Invalid audio filename.");
        }

        try {
            Path filePath = Paths.get(properties.getStorageDir()).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Requested audio file was not found: " + filename);
            }

            MediaType mediaType = filename.endsWith(".wav") ? MediaType.parseMediaType("audio/wav") : MediaType.parseMediaType("audio/mpeg");

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(resource);

        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw (ResourceNotFoundException) e;
            }
            throw new ResourceNotFoundException("Error loading audio file: " + filename);
        }
    }
}
