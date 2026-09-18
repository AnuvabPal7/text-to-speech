package com.example.tts.service;

import com.example.tts.dto.SpeechHistoryDto;
import com.example.tts.model.SpeechHistory;
import com.example.tts.repository.SpeechHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class SpeechHistoryService {

    private static final Logger log = LoggerFactory.getLogger(SpeechHistoryService.class);

    private final SpeechHistoryRepository repository;
    // In-memory fallback if database is not active
    private final List<SpeechHistory> inMemoryFallback = new CopyOnWriteArrayList<>();

    public SpeechHistoryService(SpeechHistoryRepository repository) {
        this.repository = repository;
    }

    public SpeechHistory saveHistory(String text, String language, String voice, String audioUrl,
                                     int characterCount, int wordCount, String audioFormat, long fileSizeBytes) {
        SpeechHistory history = new SpeechHistory(
                text, language, voice, audioUrl, characterCount, wordCount, audioFormat, fileSizeBytes
        );

        try {
            return repository.save(history);
        } catch (Exception ex) {
            log.warn("Database storage failed (falling back to memory buffer): {}", ex.getMessage());
            history.setId((long) (inMemoryFallback.size() + 1));
            inMemoryFallback.add(0, history);
            return history;
        }
    }

    public List<SpeechHistoryDto> getRecentHistory(int limit) {
        try {
            return repository.findTop50ByOrderByCreatedAtDesc().stream()
                    .limit(limit)
                    .map(SpeechHistoryDto::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.warn("Querying database failed, reading from in-memory store: {}", ex.getMessage());
            return inMemoryFallback.stream()
                    .limit(limit)
                    .map(SpeechHistoryDto::fromEntity)
                    .collect(Collectors.toList());
        }
    }

    public boolean deleteHistory(Long id) {
        try {
            if (repository.existsById(id)) {
                repository.deleteById(id);
                return true;
            }
        } catch (Exception ignored) {
        }
        return inMemoryFallback.removeIf(h -> h.getId().equals(id));
    }

    @Transactional
    public void deleteAllHistory() {
        try {
            repository.deleteAll();
        } catch (Exception ex) {
            log.warn("Database deleteAll failed, clearing memory store: {}", ex.getMessage());
        }
        inMemoryFallback.clear();
    }
}
