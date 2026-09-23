import { Voice, TtsRequest, TtsResponse, HealthStatus, SpeechHistoryItem, ApiError } from '../types';

// In local dev, Vite's proxy (vite.config.ts) forwards "/api" to the Spring
// Boot backend on :8080, so an empty base URL works fine there.
// In production (e.g. on Vercel), there is no dev proxy - the frontend and
// backend are on completely different domains - so VITE_API_BASE_URL must be
// set to the deployed backend's URL (e.g. https://your-app.onrender.com),
// or every request below will hit Vercel's own domain instead of Render's.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

// The backend returns audioUrl as a relative path (e.g. "/api/audio/x.mp3"),
// same as every other endpoint. In local dev that's fine - the Vite proxy
// resolves it against localhost:8080. In production, a relative URL used
// directly as an <audio src> or download <a href> resolves against the
// CURRENT page's origin (Vercel), not the backend (Render) - so it 404s.
// Every place that plays or downloads generated audio must resolve the
// URL through this helper instead of using audioUrl directly.
export function resolveAudioUrl(audioUrl: string): string {
  if (!audioUrl) return audioUrl;
  // Already absolute (e.g. a future S3/CDN URL) - leave it alone.
  if (audioUrl.startsWith('http://') || audioUrl.startsWith('https://')) {
    return audioUrl;
  }
  return `${API_BASE_URL}${audioUrl}`;
}

export const DEFAULT_LANGUAGES = [
  { code: 'en-US', name: 'English (United States)', nativeName: 'English (US)', flag: '🇺🇸' },
  { code: 'en-GB', name: 'English (United Kingdom)', nativeName: 'English (UK)', flag: '🇬🇧' },
  { code: 'en-IN', name: 'English (India)', nativeName: 'English (IN)', flag: '🇮🇳' },
  { code: 'hi-IN', name: 'Hindi (India)', nativeName: 'हिन्दी', flag: '🇮🇳' },
  { code: 'es-ES', name: 'Spanish (Spain)', nativeName: 'Español', flag: '🇪🇸' },
  { code: 'fr-FR', name: 'French (France)', nativeName: 'Français', flag: '🇫🇷' },
  { code: 'de-DE', name: 'German (Germany)', nativeName: 'Deutsch', flag: '🇩🇪' },
];

export const FALLBACK_VOICES: Voice[] = [
  { id: 'Joanna', name: 'Joanna (Neural)', languageCode: 'en-US', languageName: 'English (US)', gender: 'Female', accent: 'US', provider: 'polly', engine: 'neural' },
  { id: 'Matthew', name: 'Matthew (Neural)', languageCode: 'en-US', languageName: 'English (US)', gender: 'Male', accent: 'US', provider: 'polly', engine: 'neural' },
  { id: 'Kendra', name: 'Kendra (Neural)', languageCode: 'en-US', languageName: 'English (US)', gender: 'Female', accent: 'US', provider: 'polly', engine: 'neural' },
  { id: 'Salli', name: 'Salli (Neural)', languageCode: 'en-US', languageName: 'English (US)', gender: 'Female', accent: 'US', provider: 'polly', engine: 'neural' },
  { id: 'Amy', name: 'Amy (Neural)', languageCode: 'en-GB', languageName: 'English (UK)', gender: 'Female', accent: 'British', provider: 'polly', engine: 'neural' },
  { id: 'Brian', name: 'Brian (Neural)', languageCode: 'en-GB', languageName: 'English (UK)', gender: 'Male', accent: 'British', provider: 'polly', engine: 'neural' },
  { id: 'Arthur', name: 'Arthur (Neural)', languageCode: 'en-GB', languageName: 'English (UK)', gender: 'Male', accent: 'British', provider: 'polly', engine: 'neural' },
  // Indian English (en-IN): Kajal (Neural), Aditi (Standard), Raveena (Standard)
  { id: 'Kajal', name: 'Kajal (Neural)', languageCode: 'en-IN', languageName: 'English (India)', gender: 'Female', accent: 'Indian', provider: 'polly', engine: 'neural' },
  { id: 'Aditi', name: 'Aditi (Standard)', languageCode: 'en-IN', languageName: 'English (India)', gender: 'Female', accent: 'Indian', provider: 'polly', engine: 'standard' },
  { id: 'Raveena', name: 'Raveena (Standard)', languageCode: 'en-IN', languageName: 'English (India)', gender: 'Female', accent: 'Indian', provider: 'polly', engine: 'standard' },
  // Hindi (hi-IN): Kajal (Neural), Aditi (Standard)
  { id: 'Kajal', name: 'Kajal (Neural)', languageCode: 'hi-IN', languageName: 'Hindi (India)', gender: 'Female', accent: 'Indian', provider: 'polly', engine: 'neural' },
  { id: 'Aditi', name: 'Aditi (Standard)', languageCode: 'hi-IN', languageName: 'Hindi (India)', gender: 'Female', accent: 'Indian', provider: 'polly', engine: 'standard' },
  { id: 'Lucia', name: 'Lucia (Neural)', languageCode: 'es-ES', languageName: 'Spanish (Spain)', gender: 'Female', accent: 'Castilian', provider: 'polly', engine: 'neural' },
  { id: 'Enrique', name: 'Enrique (Standard)', languageCode: 'es-ES', languageName: 'Spanish (Spain)', gender: 'Male', accent: 'Castilian', provider: 'polly', engine: 'standard' },
  { id: 'Mia', name: 'Mia (Neural)', languageCode: 'es-MX', languageName: 'Spanish (Mexico)', gender: 'Female', accent: 'Mexican', provider: 'polly', engine: 'neural' },
  { id: 'Andres', name: 'Andres (Neural)', languageCode: 'es-MX', languageName: 'Spanish (Mexico)', gender: 'Male', accent: 'Mexican', provider: 'polly', engine: 'neural' },
  { id: 'Lea', name: 'Lea (Neural)', languageCode: 'fr-FR', languageName: 'French (France)', gender: 'Female', accent: 'Standard', provider: 'polly', engine: 'neural' },
  { id: 'Remi', name: 'Remi (Neural)', languageCode: 'fr-FR', languageName: 'French (France)', gender: 'Male', accent: 'Standard', provider: 'polly', engine: 'neural' },
  { id: 'Vicki', name: 'Vicki (Neural)', languageCode: 'de-DE', languageName: 'German (Germany)', gender: 'Female', accent: 'Standard', provider: 'polly', engine: 'neural' },
  { id: 'Daniel', name: 'Daniel (Neural)', languageCode: 'de-DE', languageName: 'German (Germany)', gender: 'Male', accent: 'Standard', provider: 'polly', engine: 'neural' },
];

const HEALTH_CHECK_TIMEOUT_MS = 5000;

// Confirmed against the backend project:
//   - Endpoint: GET /api/health (backend/.../controller/HealthController.java,
//     @RequestMapping("/api") + @GetMapping("/health"))
//   - Port: 8080 (backend/src/main/resources/application.properties, server.port=8080)
//   - The Vite dev server proxies "/api" -> http://localhost:8080 (vite.config.ts),
//     so this relative fetch reaches the real Spring Boot process, not a mock.
//
// IMPORTANT: this function must NEVER fabricate a healthy response. If the
// request fails, is rejected, or times out, that means the backend's status
// is genuinely unknown/unreachable - the caller decides what to show (e.g. DOWN),
// but this function's job is only to report what actually happened.
export async function fetchHealth(): Promise<HealthStatus> {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), HEALTH_CHECK_TIMEOUT_MS);

  try {
    const res = await fetch(`${API_BASE_URL}/api/health`, {
      signal: controller.signal,
      cache: 'no-store',
    });

    if (!res.ok) {
      throw new Error(`Health check failed with HTTP ${res.status}`);
    }

    return await res.json();
  } finally {
    clearTimeout(timeoutId);
  }
}

export async function fetchVoices(languageCode?: string): Promise<Voice[]> {
  try {
    const url = languageCode
        ? `${API_BASE_URL}/api/voices?language=${encodeURIComponent(languageCode)}`
        : `${API_BASE_URL}/api/voices`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`Failed to load voices (${res.status})`);
    const data = await res.json();
    return Array.isArray(data) && data.length > 0 ? data : FALLBACK_VOICES;
  } catch {
    if (languageCode) {
      return FALLBACK_VOICES.filter(
          v => v.languageCode.toLowerCase() === languageCode.toLowerCase() ||
              v.languageCode.toLowerCase().startsWith(languageCode.toLowerCase())
      );
    }
    return FALLBACK_VOICES;
  }
}

export async function generateSpeech(payload: TtsRequest): Promise<TtsResponse> {
  const res = await fetch(`${API_BASE_URL}/api/tts`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    let errorJson: ApiError;
    try {
      errorJson = await res.json();
    } catch {
      errorJson = {
        status: res.status,
        error: res.statusText || 'API Error',
        message: `Request failed with HTTP status ${res.status}`,
      };
    }
    throw errorJson;
  }

  return await res.json();
}

export async function fetchHistory(limit: number = 30): Promise<SpeechHistoryItem[]> {
  try {
    const res = await fetch(`${API_BASE_URL}/api/history?limit=${limit}`);
    if (!res.ok) throw new Error(`Failed to fetch history (${res.status})`);
    return await res.json();
  } catch {
    return [];
  }
}

export async function deleteHistory(id: number): Promise<boolean> {
  try {
    const res = await fetch(`${API_BASE_URL}/api/history/${id}`, {
      method: 'DELETE',
    });
    return res.ok;
  } catch {
    return false;
  }
}