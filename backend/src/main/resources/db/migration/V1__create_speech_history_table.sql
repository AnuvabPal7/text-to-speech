-- V1 Flyway Migration: Create speech history table
CREATE TABLE IF NOT EXISTS speech_history (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    language VARCHAR(30) NOT NULL,
    voice VARCHAR(100) NOT NULL,
    audio_url VARCHAR(500) NOT NULL,
    character_count INT NOT NULL,
    word_count INT NOT NULL,
    audio_format VARCHAR(10) DEFAULT 'mp3',
    file_size_bytes BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_speech_history_created_at ON speech_history (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_speech_history_language ON speech_history (language);
