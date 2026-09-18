package com.example.tts.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aws")
public class AwsPollyProperties {

    /**
     * AWS Region (default: ap-south-1)
     */
    private String region = "ap-south-1";

    /**
     * AWS Access Key ID injected via AWS_ACCESS_KEY_ID
     */
    private String accessKeyId = "";

    /**
     * AWS Secret Access Key injected via AWS_SECRET_ACCESS_KEY
     */
    private String secretAccessKey = "";

    /**
     * Polly specific options
     */
    private Polly polly = new Polly();

    public static class Polly {
        private String defaultVoice = "Joanna";
        private String outputFormat = "mp3";
        private String engine = "neural";

        public String getDefaultVoice() {
            return defaultVoice;
        }

        public void setDefaultVoice(String defaultVoice) {
            this.defaultVoice = defaultVoice;
        }

        public String getOutputFormat() {
            return outputFormat;
        }

        public void setOutputFormat(String outputFormat) {
            this.outputFormat = outputFormat;
        }

        public String getEngine() {
            return engine;
        }

        public void setEngine(String engine) {
            this.engine = engine;
        }
    }

    public boolean hasCredentials() {
        return getAccessKeyId() != null && !getAccessKeyId().trim().isEmpty() &&
                getSecretAccessKey() != null && !getSecretAccessKey().trim().isEmpty();
    }

    // Getters and Setters
    public String getRegion() {
        if (region != null && !region.trim().isEmpty()) {
            return region.trim();
        }
        String env = System.getenv("TTS_AWS_REGION");
        if (env != null && !env.trim().isEmpty()) return env.trim();
        env = System.getenv("AWS_REGION");
        if (env != null && !env.trim().isEmpty()) return env.trim();
        return "ap-south-1";
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKeyId() {
        if (accessKeyId != null && !accessKeyId.trim().isEmpty()) {
            return accessKeyId.trim();
        }
        String env = System.getenv("TTS_AWS_ACCESS_KEY_ID");
        if (env != null && !env.trim().isEmpty()) return env.trim();
        env = System.getenv("AWS_ACCESS_KEY_ID");
        if (env != null && !env.trim().isEmpty()) return env.trim();
        return "";
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        if (secretAccessKey != null && !secretAccessKey.trim().isEmpty()) {
            return secretAccessKey.trim();
        }
        String env = System.getenv("TTS_AWS_SECRET_ACCESS_KEY");
        if (env != null && !env.trim().isEmpty()) return env.trim();
        env = System.getenv("AWS_SECRET_ACCESS_KEY");
        if (env != null && !env.trim().isEmpty()) return env.trim();
        return "";
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public Polly getPolly() {
        return polly;
    }

    public void setPolly(Polly polly) {
        this.polly = polly;
    }
}
