package com.example.tts.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Entity representing stored speech generation history in PostgreSQL.
 */
@Entity
@Table(name = "speech_history")
public class SpeechHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(nullable = false, length = 30)
    private String language;

    @Column(nullable = false, length = 100)
    private String voice;

    @Column(name = "audio_url", nullable = false, length = 500)
    private String audioUrl;

    @Column(name = "character_count", nullable = false)
    private Integer characterCount;

    @Column(name = "word_count", nullable = false)
    private Integer wordCount;

    @Column(name = "audio_format", length = 10)
    private String audioFormat = "mp3";

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public SpeechHistory() {
    }

    public SpeechHistory(String text, String language, String voice, String audioUrl,
                         Integer characterCount, Integer wordCount, String audioFormat, Long fileSizeBytes) {
        this.text = text;
        this.language = language;
        this.voice = voice;
        this.audioUrl = audioUrl;
        this.characterCount = characterCount;
        this.wordCount = wordCount;
        this.audioFormat = audioFormat;
        this.fileSizeBytes = fileSizeBytes;
        this.createdAt = OffsetDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        if (this.audioFormat == null) {
            this.audioFormat = "mp3";
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public Integer getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(Integer characterCount) {
        this.characterCount = characterCount;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public String getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(String audioFormat) {
        this.audioFormat = audioFormat;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
