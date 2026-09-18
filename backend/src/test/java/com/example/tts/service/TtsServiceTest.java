package com.example.tts.service;

import com.example.tts.config.AwsPollyProperties;
import com.example.tts.config.TtsProperties;
import com.example.tts.dto.TtsRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TtsServiceTest {

    private PollyTtsService pollyTtsService;
    private AwsPollyProperties awsProperties;
    private TtsProperties ttsProperties;
    private VoiceService voiceService;

    @BeforeEach
    void setUp() {
        awsProperties = new AwsPollyProperties();
        awsProperties.setAccessKeyId(""); // Test local preview mode
        awsProperties.setSecretAccessKey("");
        awsProperties.setRegion("ap-south-1");

        ttsProperties = new TtsProperties();
        ttsProperties.setProvider("polly");

        voiceService = new VoiceService();
        voiceService.initVoices();

        pollyTtsService = new PollyTtsService(awsProperties, ttsProperties, voiceService);
    }

    @Test
    @DisplayName("Returns correct provider name and availability")
    void testProviderInfo() {
        assertEquals("polly", pollyTtsService.getProviderName());
        assertFalse(pollyTtsService.isAvailable(), "Should not be available when credentials are empty");

        awsProperties.setAccessKeyId("AKIAIOSFODNN7EXAMPLE");
        awsProperties.setSecretAccessKey("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        assertTrue(pollyTtsService.isAvailable(), "Should be available when credentials are populated");
    }

    @Test
    @DisplayName("Synthesizes local preview audio when AWS credentials are empty")
    void testSynthesisPreviewAudio() {
        TtsRequestDto request = new TtsRequestDto("Hello world, testing AWS Polly text to speech.", "en-US", "Joanna");
        byte[] audio = pollyTtsService.synthesizeSpeech(request);

        assertNotNull(audio);
        assertTrue(audio.length > 1000, "Audio byte array should contain valid WAV preview data");
        // Verify WAV header RIFF
        assertEquals('R', (char) audio[0]);
        assertEquals('I', (char) audio[1]);
        assertEquals('F', (char) audio[2]);
        assertEquals('F', (char) audio[3]);
    }

    @Test
    @DisplayName("Kajal voice is supported for Hindi (hi-IN) with Neural engine")
    void testKajalHindiConfiguration() {
        // 1. Validate voice service accepts Kajal for hi-IN
        assertDoesNotThrow(() -> voiceService.validateVoiceAndLanguage("Kajal", "hi-IN"));

        var voiceOpt = voiceService.findVoice("Kajal", "hi-IN");
        assertTrue(voiceOpt.isPresent(), "Kajal must be found for hi-IN");
        assertEquals("neural", voiceOpt.get().getEngine(), "Kajal engine must be neural");
        assertEquals("hi-IN", voiceOpt.get().getLanguageCode(), "Language must be hi-IN");

        // 2. Synthesize speech for Kajal in Hindi
        TtsRequestDto request = new TtsRequestDto("नमस्ते दुनिया, अमेज़न पॉली में आपका स्वागत है।", "hi-IN", "Kajal");
        request.setEngine("neural");
        byte[] audio = pollyTtsService.synthesizeSpeech(request);

        assertNotNull(audio);
        assertTrue(audio.length > 1000, "Audio output should be valid WAV data");
        assertEquals('R', (char) audio[0]);
        assertEquals('I', (char) audio[1]);
        assertEquals('F', (char) audio[2]);
        assertEquals('F', (char) audio[3]);
    }

    @Test
    @DisplayName("Kajal voice is also supported for Indian English (en-IN) as a bilingual voice")
    void testKajalIndianEnglishConfiguration() {
        assertDoesNotThrow(() -> voiceService.validateVoiceAndLanguage("Kajal", "en-IN"));

        var voiceOpt = voiceService.findVoice("Kajal", "en-IN");
        assertTrue(voiceOpt.isPresent(), "Kajal must be found for en-IN");
        assertEquals("neural", voiceOpt.get().getEngine(), "Kajal engine must be neural");
        assertEquals("en-IN", voiceOpt.get().getLanguageCode(), "Language must be en-IN");

        TtsRequestDto request = new TtsRequestDto("Welcome to Amazon Polly text-to-speech in Indian English.", "en-IN", "Kajal");
        request.setEngine("neural");
        byte[] audio = pollyTtsService.synthesizeSpeech(request);
        assertNotNull(audio);
        assertTrue(audio.length > 1000);
    }

    @Test
    @DisplayName("Kajal voice correctly rejects unsupported languages like fr-FR")
    void testKajalRejectsNonSupportedLanguage() {
        assertThrows(com.example.tts.exception.InvalidRequestException.class, () ->
                voiceService.validateVoiceAndLanguage("Kajal", "fr-FR")
        );
    }
}
