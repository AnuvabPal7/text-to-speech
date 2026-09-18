package com.example.tts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TtsRequestDto {

    @NotBlank(message = "Text cannot be empty or whitespace.")
    @Size(max = 5000, message = "Text exceeds the maximum allowed limit of 5000 characters.")
    private String text;

    @NotBlank(message = "Language is required.")
    private String language;

    @NotBlank(message = "Voice is required.")
    private String voice;

    // Optional engine override (e.g. "neural" or "standard")
    private String engine;

    // Optional audio adjustments
    private Double speed = 1.0;
    private Double pitch = 1.0;
    private String format = "mp3";

    public TtsRequestDto() {
    }

    public TtsRequestDto(String text, String language, String voice) {
        this.text = text;
        this.language = language;
        this.voice = voice;
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

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getPitch() {
        return pitch;
    }

    public void setPitch(Double pitch) {
        this.pitch = pitch;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
