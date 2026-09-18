package com.example.tts.controller;

import com.example.tts.dto.SpeechHistoryDto;
import com.example.tts.service.SpeechHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final SpeechHistoryService historyService;

    public HistoryController(SpeechHistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * GET /api/history
     * Returns history of generated speeches from PostgreSQL.
     */
    @GetMapping
    public ResponseEntity<List<SpeechHistoryDto>> getHistory(@RequestParam(defaultValue = "30") int limit) {
        List<SpeechHistoryDto> history = historyService.getRecentHistory(limit);
        return ResponseEntity.ok(history);
    }

    /**
     * DELETE /api/history/{id}
     * Deletes a specific speech history record.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteHistory(@PathVariable Long id) {
        boolean deleted = historyService.deleteHistory(id);
        return ResponseEntity.ok(Map.of("success", deleted, "id", id));
    }

    /**
     * DELETE /api/history
     * Clears all speech history records.
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearHistory() {
        historyService.deleteAllHistory();
        return ResponseEntity.ok(Map.of("success", true, "message", "All speech history records cleared"));
    }
}
