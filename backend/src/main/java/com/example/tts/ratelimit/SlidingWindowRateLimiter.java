package com.example.tts.ratelimit;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A simple in-memory rate limiter: each client (identified by IP) gets a
 * "bucket" that allows up to N requests per rolling time window. Once the
 * window elapses, the bucket resets and the client can make requests again.
 * <p>
 * This is intentionally simple (no external library, no Redis) - fine for a
 * single backend instance like this project. If you ever deploy multiple
 * backend instances behind a load balancer, each instance would track its
 * own limits separately, so a proper shared store (e.g. Redis + Bucket4j)
 * would be needed instead. For a solo/local/demo project, this is enough.
 */
@Component
public class SlidingWindowRateLimiter {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static class Bucket {
        long windowStartMillis;
        int count;
    }

    /**
     * Attempts to consume one request slot for the given client.
     *
     * @return true if the request is allowed, false if the client has
     *         exceeded maxRequests within the current window.
     */
    public synchronized boolean tryConsume(String clientId, int maxRequests, long windowSeconds) {
        long windowMillis = TimeUnit.SECONDS.toMillis(windowSeconds);
        long now = System.currentTimeMillis();

        Bucket bucket = buckets.computeIfAbsent(clientId, id -> {
            Bucket b = new Bucket();
            b.windowStartMillis = now;
            b.count = 0;
            return b;
        });

        // Current window has expired - start counting fresh.
        if (now - bucket.windowStartMillis >= windowMillis) {
            bucket.windowStartMillis = now;
            bucket.count = 0;
        }

        if (bucket.count >= maxRequests) {
            return false;
        }

        bucket.count++;
        return true;
    }

    /**
     * How many seconds until this client's current window resets.
     * Used to populate the Retry-After response header.
     */
    public long secondsUntilReset(String clientId, long windowSeconds) {
        Bucket bucket = buckets.get(clientId);
        if (bucket == null) {
            return 0;
        }
        long windowMillis = TimeUnit.SECONDS.toMillis(windowSeconds);
        long elapsed = System.currentTimeMillis() - bucket.windowStartMillis;
        long remainingMillis = Math.max(0, windowMillis - elapsed);
        return TimeUnit.MILLISECONDS.toSeconds(remainingMillis) + 1;
    }
}