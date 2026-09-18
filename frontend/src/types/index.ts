export interface Voice {
  id: string;
  name: string;
  languageCode: string;
  languageName: string;
  gender: 'Female' | 'Male' | 'Neutral';
  accent?: string;
  provider?: string;
  engine?: 'neural' | 'standard' | 'long-form' | 'generative';
}

export interface Language {
  code: string;
  name: string;
  nativeName: string;
  flag: string;
}

export interface TtsRequest {
  text: string;
  language: string;
  voice: string;
  engine?: string;
  speed?: number;
  pitch?: number;
  format?: string;
}

export interface TtsResponse {
  success: boolean;
  audioUrl: string;
  message?: string;
  engine?: string;
  historyId?: number;
  characterCount?: number;
  wordCount?: number;
  format?: string;
  fileSizeBytes?: number;
}

// Connectivity state of the Spring Boot backend, as tracked by the frontend.
// This is intentionally separate from HealthStatus.status: it also covers the
// "we haven't checked yet" and "the request itself failed" cases, which the
// backend's JSON payload can never tell us (because it never arrives).
export type BackendStatus = 'CHECKING' | 'UP' | 'DOWN' | 'DEGRADED';

export interface HealthStatus {
  status: 'UP' | 'DOWN' | 'DEGRADED';
  provider: string;
  providerConfigured: boolean;
  databaseStatus: string;
  timestamp: string;
  details?: {
    region?: string;
    maxTextLength?: number;
    mode?: string;
    hasApiKey?: boolean;
    [key: string]: unknown;
  };
}

export interface SpeechHistoryItem {
  id: number;
  text: string;
  language: string;
  voice: string;
  audioUrl: string;
  characterCount: number;
  wordCount: number;
  audioFormat: string;
  fileSizeBytes?: number;
  createdAt: string;
}

export interface ApiError {
  timestamp?: string;
  status: number;
  error: string;
  message: string;
  path?: string;
  fieldErrors?: Record<string, string>;
}