package com.example.tts.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tts.rate-limit")
public class RateLimitProperties {

    /** Master switch - set to false to disable rate limiting entirely. */
    private boolean enabled = true;

    /** Max requests a single client (by IP) may make within the window. */
    private int requestsPerWindow = 10;

    /** Length of the rolling time window, in seconds. */
    private long windowSeconds = 60;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRequestsPerWindow() {
        return requestsPerWindow;
    }

    public void setRequestsPerWindow(int requestsPerWindow) {
        this.requestsPerWindow = requestsPerWindow;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }
}