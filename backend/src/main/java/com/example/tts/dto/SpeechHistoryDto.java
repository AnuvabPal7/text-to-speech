package com.example.tts.dto;

import com.example.tts.model.SpeechHistory;

import java.time.OffsetDateTime;

public class SpeechHistoryDto {

    private Long id;
    private String text;
    private String language;
    private String voice;
    private String audioUrl;
    private Integer characterCount;
    private Integer wordCount;
    private String audioFormat;
    private Long fileSizeBytes;
    private OffsetDateTime createdAt;

    public SpeechHistoryDto() {
    }

    public static SpeechHistoryDto fromEntity(SpeechHistory entity) {
        SpeechHistoryDto dto = new SpeechHistoryDto();
        dto.setId(entity.getId());
        dto.setText(entity.getText());
        dto.setLanguage(entity.getLanguage());
        dto.setVoice(entity.getVoice());
        dto.setAudioUrl(entity.getAudioUrl());
        dto.setCharacterCount(entity.getCharacterCount());
        dto.setWordCount(entity.getWordCount());
        dto.setAudioFormat(entity.getAudioFormat());
        dto.setFileSizeBytes(entity.getFileSizeBytes());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
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
