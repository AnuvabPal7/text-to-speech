package com.example.tts.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TtsResponseDto {

    private boolean success;
    private String audioUrl;
    private String message;
    private Long historyId;
    private Integer characterCount;
    private Integer wordCount;
    private String format;
    private Long fileSizeBytes;

    public TtsResponseDto() {
    }

    public TtsResponseDto(boolean success, String audioUrl) {
        this.success = success;
        this.audioUrl = audioUrl;
    }

    public static TtsResponseDto ok(String audioUrl, Long historyId, int charCount, int wordCount, String format, long size) {
        TtsResponseDto dto = new TtsResponseDto(true, audioUrl);
        dto.setHistoryId(historyId);
        dto.setCharacterCount(charCount);
        dto.setWordCount(wordCount);
        dto.setFormat(format);
        dto.setFileSizeBytes(size);
        dto.setMessage("Audio successfully generated.");
        return dto;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
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

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }
}
