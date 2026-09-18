package com.example.tts.controller;

import com.example.tts.dto.TtsRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "aws.access-key-id=",
        "aws.secret-access-key="
})
class TtsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/health returns 200 OK")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/voices returns voices list")
    void testGetVoices() throws Exception {
        mockMvc.perform(get("/api/voices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/tts fails on empty text (400 Bad Request)")
    void testEmptyTextValidation() throws Exception {
        TtsRequestDto request = new TtsRequestDto("", "en-US", "Joanna");

        mockMvc.perform(post("/api/tts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/tts fails on missing language (400 Bad Request)")
    void testMissingLanguageValidation() throws Exception {
        TtsRequestDto request = new TtsRequestDto("Hello world", "", "Joanna");

        mockMvc.perform(post("/api/tts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/tts fails on missing voice (400 Bad Request)")
    void testMissingVoiceValidation() throws Exception {
        TtsRequestDto request = new TtsRequestDto("Hello world", "en-US", "");

        mockMvc.perform(post("/api/tts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/tts fails on mismatched voice and language (400 Bad Request)")
    void testVoiceLanguageMismatch() throws Exception {
        TtsRequestDto request = new TtsRequestDto("Hello world", "hi-IN", "Joanna");

        mockMvc.perform(post("/api/tts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    @DisplayName("POST /api/tts generates speech successfully (200 OK)")
    void testSuccessfulSpeechGeneration() throws Exception {
        TtsRequestDto request = new TtsRequestDto("Hello world, testing text to speech.", "en-US", "Joanna");

        mockMvc.perform(post("/api/tts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.audioUrl").isString());
    }

    @Test
    @DisplayName("GET /api/history returns history list (200 OK)")
    void testGetHistory() throws Exception {
        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("DELETE /api/history/{id} deletes history item")
    void testDeleteHistory() throws Exception {
        // First create an item
        TtsRequestDto request = new TtsRequestDto("Text to be deleted", "en-US", "Joanna");
        mockMvc.perform(post("/api/tts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then get history and delete first item if present
        mockMvc.perform(delete("/api/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/tts generates speech with Kajal in Hindi (hi-IN) successfully")
    void testKajalHindiEndpoint() throws Exception {
        TtsRequestDto request = new TtsRequestDto("नमस्ते, यह अमेज़न पॉली टेस्ट है।", "hi-IN", "Kajal");
        request.setEngine("neural");

        mockMvc.perform(post("/api/tts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.audioUrl").isString());
    }

    @Test
    @DisplayName("POST /api/tts generates speech with Kajal in Indian English (en-IN) successfully")
    void testKajalEnglishEndpoint() throws Exception {
        TtsRequestDto request = new TtsRequestDto("Hello from AWS Polly Kajal in Indian English.", "en-IN", "Kajal");
        request.setEngine("neural");

        mockMvc.perform(post("/api/tts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.audioUrl").isString());
    }
}
