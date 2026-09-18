package com.example.tts.service;

import com.example.tts.dto.TtsRequestDto;

public interface TtsService {

    /**
     * Synthesizes input text to audio using the configured TTS provider.
     *
     * @param request the validated TTS request DTO
     * @return raw audio byte array
     */
    byte[] synthesizeSpeech(TtsRequestDto request);

    /**
     * Returns provider name (e.g. "polly", "google").
     */
    String getProviderName();

    /**
     * Checks if provider is configured and available.
     */
    boolean isAvailable();
}
