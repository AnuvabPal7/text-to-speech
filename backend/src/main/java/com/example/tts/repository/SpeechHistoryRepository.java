package com.example.tts.repository;

import com.example.tts.model.SpeechHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpeechHistoryRepository extends JpaRepository<SpeechHistory, Long> {

    List<SpeechHistory> findAllByOrderByCreatedAtDesc();

    List<SpeechHistory> findTop50ByOrderByCreatedAtDesc();

    List<SpeechHistory> findByLanguageOrderByCreatedAtDesc(String language);
}
