package com.example.tts.dto;

public class VoiceDto {

    private String id;
    private String name;
    private String languageCode;
    private String languageName;
    private String gender; // "Female", "Male", "Neutral"
    private String accent;
    private String provider;
    private String engine; // "neural" or "standard"

    public VoiceDto() {
    }

    public VoiceDto(String id, String name, String languageCode, String languageName, String gender, String accent, String provider) {
        this(id, name, languageCode, languageName, gender, accent, provider, "neural");
    }

    public VoiceDto(String id, String name, String languageCode, String languageName, String gender, String accent, String provider, String engine) {
        this.id = id;
        this.name = name;
        this.languageCode = languageCode;
        this.languageName = languageName;
        this.gender = gender;
        this.accent = accent;
        this.provider = provider;
        this.engine = engine;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAccent() {
        return accent;
    }

    public void setAccent(String accent) {
        this.accent = accent;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }
}
