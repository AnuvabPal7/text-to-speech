package com.example.tts.controller;

import com.example.tts.dto.VoiceDto;
import com.example.tts.service.VoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class VoiceController {

    private final VoiceService voiceService;

    public VoiceController(VoiceService voiceService) {
        this.voiceService = voiceService;
    }

    /**
     * GET /api/voices
     * Returns list of available TTS voices, optionally filtered by language code (e.g. ?language=en-US)
     */
    @GetMapping("/voices")
    public ResponseEntity<List<VoiceDto>> getVoices(@RequestParam(required = false) String language) {
        List<VoiceDto> voices = voiceService.getVoicesByLanguage(language);
        return ResponseEntity.ok(voices);
    }
}
