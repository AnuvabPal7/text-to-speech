package com.example.tts.service;

import com.example.tts.dto.VoiceDto;
import com.example.tts.exception.InvalidRequestException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VoiceService {

    private final List<VoiceDto> voices = new ArrayList<>();
    private final Set<String> supportedLanguages = new HashSet<>();

    @PostConstruct
    public void initVoices() {
        // English (US, UK, India) - Amazon Polly Neural Voices
        addVoice("Joanna", "Joanna (Neural)", "en-US", "English (United States)", "Female", "US", "polly", "neural");
        addVoice("Matthew", "Matthew (Neural)", "en-US", "English (United States)", "Male", "US", "polly", "neural");
        addVoice("Kendra", "Kendra (Neural)", "en-US", "English (United States)", "Female", "US", "polly", "neural");
        addVoice("Salli", "Salli (Neural)", "en-US", "English (United States)", "Female", "US", "polly", "neural");
        addVoice("Amy", "Amy (Neural)", "en-GB", "English (United Kingdom)", "Female", "British", "polly", "neural");
        addVoice("Brian", "Brian (Neural)", "en-GB", "English (United Kingdom)", "Male", "British", "polly", "neural");
        addVoice("Arthur", "Arthur (Neural)", "en-GB", "English (United Kingdom)", "Male", "British", "polly", "neural");

        // Indian English (en-IN)
        // Kajal is Neural-only in Amazon Polly. Aditi and Raveena are Standard-only.
        addVoice("Kajal", "Kajal (Neural)", "en-IN", "English (India)", "Female", "Indian", "polly", "neural");
        addVoice("Aditi", "Aditi (Standard)", "en-IN", "English (India)", "Female", "Indian", "polly", "standard");
        addVoice("Raveena", "Raveena (Standard)", "en-IN", "English (India)", "Female", "Indian", "polly", "standard");

        // Hindi (hi-IN)
        // Kajal is Neural-only in Amazon Polly. Aditi is Standard-only.
        addVoice("Kajal", "Kajal (Neural)", "hi-IN", "Hindi (India)", "Female", "Indian", "polly", "neural");
        addVoice("Aditi", "Aditi (Standard)", "hi-IN", "Hindi (India)", "Female", "Indian", "polly", "standard");

        // Spanish (Spain & Mexico)
        addVoice("Lucia", "Lucia (Neural)", "es-ES", "Spanish (Spain)", "Female", "Castilian", "polly", "neural");
        addVoice("Enrique", "Enrique (Standard)", "es-ES", "Spanish (Spain)", "Male", "Castilian", "polly", "standard");
        addVoice("Mia", "Mia (Neural)", "es-MX", "Spanish (Mexico)", "Female", "Mexican", "polly", "neural");
        addVoice("Andres", "Andres (Neural)", "es-MX", "Spanish (Mexico)", "Male", "Mexican", "polly", "neural");

        // French
        addVoice("Lea", "Lea (Neural)", "fr-FR", "French (France)", "Female", "Standard French", "polly", "neural");
        addVoice("Remi", "Remi (Neural)", "fr-FR", "French (France)", "Male", "Standard French", "polly", "neural");

        // German
        addVoice("Vicki", "Vicki (Neural)", "de-DE", "German (Germany)", "Female", "Standard German", "polly", "neural");
        addVoice("Daniel", "Daniel (Neural)", "de-DE", "German (Germany)", "Male", "Standard German", "polly", "neural");
    }

    private void addVoice(String id, String name, String langCode, String langName, String gender, String accent, String provider, String engine) {
        voices.add(new VoiceDto(id, name, langCode, langName, gender, accent, provider, engine));
        supportedLanguages.add(langCode.toLowerCase());
    }

    public List<VoiceDto> getAllVoices() {
        return Collections.unmodifiableList(voices);
    }

    public List<VoiceDto> getVoicesByLanguage(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            return getAllVoices();
        }
        String normalized = languageCode.trim().toLowerCase();
        return voices.stream()
                .filter(v -> v.getLanguageCode().equalsIgnoreCase(normalized) || v.getLanguageCode().toLowerCase().startsWith(normalized))
                .collect(Collectors.toList());
    }

    public Optional<VoiceDto> findVoice(String voiceId, String languageCode) {
        if (voiceId == null) return Optional.empty();
        String normalizedVoice = voiceId.trim();
        if (languageCode != null && !languageCode.trim().isEmpty()) {
            String normalizedLang = languageCode.trim().toLowerCase();
            Optional<VoiceDto> exact = voices.stream()
                    .filter(v -> v.getId().equalsIgnoreCase(normalizedVoice) &&
                            (v.getLanguageCode().equalsIgnoreCase(normalizedLang) || v.getLanguageCode().toLowerCase().startsWith(normalizedLang)))
                    .findFirst();
            if (exact.isPresent()) return exact;
        }
        return voices.stream()
                .filter(v -> v.getId().equalsIgnoreCase(normalizedVoice))
                .findFirst();
    }

    public boolean isLanguageSupported(String languageCode) {
        if (languageCode == null) return false;
        String normalized = languageCode.trim().toLowerCase();
        return supportedLanguages.stream().anyMatch(l -> l.equalsIgnoreCase(normalized) || l.startsWith(normalized));
    }

    public boolean isVoiceSupported(String voiceId) {
        if (voiceId == null) return false;
        return voices.stream().anyMatch(v -> v.getId().equalsIgnoreCase(voiceId.trim()));
    }

    public void validateVoiceAndLanguage(String voiceId, String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            throw new InvalidRequestException("Language code is required.");
        }
        if (!isLanguageSupported(languageCode)) {
            throw new InvalidRequestException("Language '" + languageCode + "' is not supported. Supported languages: " +
                    String.join(", ", supportedLanguages));
        }
        if (voiceId == null || voiceId.trim().isEmpty()) {
            throw new InvalidRequestException("Voice ID is required.");
        }

        // First check if any voice matches both ID and requested language
        String normalizedReqLang = languageCode.trim().toLowerCase();
        Optional<VoiceDto> exactMatch = voices.stream()
                .filter(v -> v.getId().equalsIgnoreCase(voiceId.trim()) &&
                        (v.getLanguageCode().equalsIgnoreCase(normalizedReqLang) ||
                                v.getLanguageCode().toLowerCase().startsWith(normalizedReqLang)))
                .findFirst();

        if (exactMatch.isPresent()) {
            return;
        }

        // Check if voice exists for any other language
        Optional<VoiceDto> anyVoice = voices.stream()
                .filter(v -> v.getId().equalsIgnoreCase(voiceId.trim()))
                .findFirst();

        if (anyVoice.isEmpty()) {
            throw new InvalidRequestException("Voice '" + voiceId + "' is invalid or does not exist.");
        }

        VoiceDto voice = anyVoice.get();
        throw new InvalidRequestException(String.format(
                "Voice '%s' belongs to language '%s', but '%s' was requested. Please select a valid voice for the chosen language.",
                voice.getName(), voice.getLanguageCode(), languageCode
        ));
    }
}
