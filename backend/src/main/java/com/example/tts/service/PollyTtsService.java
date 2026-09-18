package com.example.tts.service;

import com.example.tts.config.AwsPollyProperties;
import com.example.tts.config.TtsProperties;
import com.example.tts.dto.TtsRequestDto;
import com.example.tts.dto.VoiceDto;
import com.example.tts.exception.InvalidRequestException;
import com.example.tts.exception.TtsProviderException;
import com.example.tts.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Service
@ConditionalOnProperty(name = "tts.provider", havingValue = "polly", matchIfMissing = true)
public class PollyTtsService implements TtsService {

    private static final Logger log = LoggerFactory.getLogger(PollyTtsService.class);

    private final AwsPollyProperties awsPollyProperties;
    private final TtsProperties ttsProperties;
    private final VoiceService voiceService;

    public PollyTtsService(AwsPollyProperties awsPollyProperties,
                           TtsProperties ttsProperties,
                           VoiceService voiceService) {
        this.awsPollyProperties = awsPollyProperties;
        this.ttsProperties = ttsProperties;
        this.voiceService = voiceService;
    }

    @Override
    public byte[] synthesizeSpeech(TtsRequestDto request) {
        // If AWS credentials are not configured, generate demo synthetic audio for seamless local testing
        if (!awsPollyProperties.hasCredentials()) {
            log.warn("AWS credentials (AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY) not configured in environment. " +
                    "Generating local development preview audio for text: '{}'", truncate(request.getText(), 40));
            return generateDevelopmentAudio(request);
        }

        String regionStr = awsPollyProperties.getRegion();
        Region region;
        try {
            region = Region.of(regionStr);
        } catch (Exception ex) {
            log.warn("Invalid AWS region '{}', falling back to ap-northeast-2", regionStr);
            region = Region.AP_NORTHEAST_2;
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                awsPollyProperties.getAccessKeyId().trim(),
                awsPollyProperties.getSecretAccessKey().trim()
        );

        try (PollyClient pollyClient = PollyClient.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build()) {

            // Resolve VoiceId
            String rawVoice = request.getVoice() != null && !request.getVoice().trim().isEmpty()
                    ? request.getVoice().trim()
                    : awsPollyProperties.getPolly().getDefaultVoice();

            VoiceId voiceId;
            try {
                voiceId = VoiceId.fromValue(rawVoice);
            } catch (Exception ex) {
                log.warn("Unknown VoiceId '{}', falling back to default voice '{}'", rawVoice, awsPollyProperties.getPolly().getDefaultVoice());
                voiceId = VoiceId.fromValue(awsPollyProperties.getPolly().getDefaultVoice());
            }

            // Resolve OutputFormat
            OutputFormat outputFormat;
            String fmtStr = awsPollyProperties.getPolly().getOutputFormat();
            if ("mp3".equalsIgnoreCase(fmtStr)) {
                outputFormat = OutputFormat.MP3;
            } else if ("ogg_vorbis".equalsIgnoreCase(fmtStr) || "ogg".equalsIgnoreCase(fmtStr)) {
                outputFormat = OutputFormat.OGG_VORBIS;
            } else if ("pcm".equalsIgnoreCase(fmtStr) || "wav".equalsIgnoreCase(fmtStr)) {
                outputFormat = OutputFormat.PCM;
            } else {
                outputFormat = OutputFormat.MP3;
            }

            // Resolve Engine:
            // 1. If the request explicitly specifies an engine ("neural" vs "standard"), honor it.
            // 2. Otherwise, check the Voice metadata (e.g. Kajal is strictly Neural; Aditi is Standard).
            // 3. Otherwise, fall back to the application.properties configuration.
            Engine preferredEngine;
            if (request.getEngine() != null && !request.getEngine().trim().isEmpty()) {
                preferredEngine = "neural".equalsIgnoreCase(request.getEngine().trim())
                        ? Engine.NEURAL
                        : Engine.STANDARD;
            } else {
                Optional<VoiceDto> matchedVoice = voiceService.findVoice(rawVoice, request.getLanguage());
                if (matchedVoice.isPresent() && matchedVoice.get().getEngine() != null) {
                    preferredEngine = "neural".equalsIgnoreCase(matchedVoice.get().getEngine())
                            ? Engine.NEURAL
                            : Engine.STANDARD;
                } else if ("kajal".equalsIgnoreCase(rawVoice)) {
                    // Kajal is strictly Neural in Amazon Polly
                    preferredEngine = Engine.NEURAL;
                } else if ("aditi".equalsIgnoreCase(rawVoice) || "raveena".equalsIgnoreCase(rawVoice)) {
                    preferredEngine = Engine.STANDARD;
                } else {
                    preferredEngine = "standard".equalsIgnoreCase(awsPollyProperties.getPolly().getEngine())
                            ? Engine.STANDARD
                            : Engine.NEURAL;
                }
            }

            // Check if speed or pitch adjustments require SSML
            boolean hasSpeed = request.getSpeed() != null && Math.abs(request.getSpeed() - 1.0) > 0.01;
            boolean hasPitch = request.getPitch() != null && Math.abs(request.getPitch() - 1.0) > 0.01;

            SynthesizeSpeechRequest.Builder requestBuilder = SynthesizeSpeechRequest.builder()
                    .voiceId(voiceId)
                    .outputFormat(outputFormat);

            // Set explicit language code for bilingual voices (e.g. Kajal in hi-IN vs en-IN)
            if (request.getLanguage() != null && !request.getLanguage().trim().isEmpty()) {
                try {
                    requestBuilder.languageCode(LanguageCode.fromValue(request.getLanguage().trim()));
                } catch (Exception ex) {
                    log.debug("Language '{}' could not be mapped to Polly LanguageCode: {}", request.getLanguage(), ex.getMessage());
                }
            }

            if (hasSpeed || hasPitch) {
                int ratePct = (int) Math.round((request.getSpeed() != null ? request.getSpeed() : 1.0) * 100);
                int pitchPct = (int) Math.round(((request.getPitch() != null ? request.getPitch() : 1.0) - 1.0) * 50);
                String pitchStr = (pitchPct >= 0 ? "+" : "") + pitchPct + "%";
                String ssml = String.format("<speak><prosody rate=\"%d%%\" pitch=\"%s\">%s</prosody></speak>",
                        ratePct, pitchStr, escapeXml(request.getText()));
                requestBuilder.text(ssml).textType(TextType.SSML);
            } else {
                requestBuilder.text(request.getText()).textType(TextType.TEXT);
            }

            // Attempt synthesis with preferred engine, and intelligently fall back if the engine is unsupported
            try {
                requestBuilder.engine(preferredEngine);
                log.info("Sending TTS request to AWS Amazon Polly (region: {}, voice: {}, engine: {})",
                        region.id(), voiceId, preferredEngine);

                try (ResponseInputStream<SynthesizeSpeechResponse> response = pollyClient.synthesizeSpeech(requestBuilder.build())) {
                    byte[] audioBytes = response.readAllBytes();
                    log.info("Successfully received {} bytes of audio from AWS Amazon Polly", audioBytes.length);
                    return audioBytes;
                }
            } catch (EngineNotSupportedException ense) {
                // If the selected engine was NEURAL and failed, try STANDARD; if it was STANDARD and failed (like Kajal), try NEURAL!
                Engine alternateEngine = (preferredEngine == Engine.NEURAL) ? Engine.STANDARD : Engine.NEURAL;
                log.warn("Voice '{}' does not support {} engine ({}). Retrying with alternate {} engine...",
                        voiceId, preferredEngine, ense.getMessage(), alternateEngine);
                requestBuilder.engine(alternateEngine);

                try (ResponseInputStream<SynthesizeSpeechResponse> response = pollyClient.synthesizeSpeech(requestBuilder.build())) {
                    byte[] audioBytes = response.readAllBytes();
                    log.info("Successfully received {} bytes of audio from AWS Amazon Polly ({} engine fallback)",
                            audioBytes.length, alternateEngine);
                    return audioBytes;
                }
            }

        } catch (TextLengthExceededException ex) {
            log.error("Amazon Polly text length exceeded: {}", ex.getMessage());
            throw new InvalidRequestException("Text length exceeds Amazon Polly maximum character limit: " + ex.getMessage());

        } catch (InvalidSsmlException ex) {
            log.error("Amazon Polly invalid SSML: {}", ex.getMessage());
            throw new InvalidRequestException("Invalid SSML markup: " + ex.getMessage());

        } catch (PollyException ex) {
            log.error("Amazon Polly service exception (status: {}): {}", ex.statusCode(), ex.getMessage());
            if (ex.statusCode() == 401 || ex.statusCode() == 403) {
                throw new UnauthorizedException("AWS Polly authentication or authorization failed: " + ex.getMessage() +
                        ". Check AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, and IAM Polly permissions.");
            }
            throw new TtsProviderException("Amazon Polly error: " + ex.getMessage(), ex.statusCode());

        } catch (Exception ex) {
            log.error("Unexpected error during Amazon Polly speech synthesis: {}", ex.getMessage(), ex);
            throw new TtsProviderException("Speech synthesis failed: " + ex.getMessage(), 500);
        }
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }

    @Override
    public String getProviderName() {
        return "polly";
    }

    @Override
    public boolean isAvailable() {
        return awsPollyProperties.hasCredentials();
    }

    /**
     * Generates clean synthetic PCM/WAV audio for local development / testing
     * when AWS credentials are not yet supplied.
     */
    private byte[] generateDevelopmentAudio(TtsRequestDto request) {
        int sampleRate = 22050;
        double duration = Math.max(1.5, Math.min(10.0, request.getText().length() * 0.065));
        int totalSamples = (int) (sampleRate * duration);

        double baseFreq = 220.0;
        if (request.getVoice() != null) {
            String v = request.getVoice().toLowerCase();
            if (v.contains("matthew") || v.contains("brian") || v.contains("arthur") || v.contains("enrique") || v.contains("andres") || v.contains("remi") || v.contains("daniel")) {
                baseFreq = 135.0; // Male pitch
            } else {
                baseFreq = 240.0; // Female pitch
            }
        }

        if (request.getPitch() != null) {
            baseFreq *= request.getPitch();
        }

        byte[] pcmData = new byte[totalSamples * 2];
        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / sampleRate;
            double cadence = 0.6 + 0.4 * Math.sin(2 * Math.PI * 3.5 * t);
            double edgeFade = Math.min(1.0, Math.min((double) i / (sampleRate * 0.05), (double) (totalSamples - i) / (sampleRate * 0.05)));

            double fundamental = Math.sin(2 * Math.PI * baseFreq * t);
            double formant1 = 0.4 * Math.sin(2 * Math.PI * (baseFreq * 2.2) * t);
            double formant2 = 0.2 * Math.sin(2 * Math.PI * (baseFreq * 3.5) * t);
            double sampleVal = (fundamental + formant1 + formant2) * cadence * edgeFade * 0.35;

            short sample16 = (short) Math.max(-32768, Math.min(32767, (int) (sampleVal * 32767)));
            pcmData[i * 2] = (byte) (sample16 & 0xff);
            pcmData[i * 2 + 1] = (byte) ((sample16 >> 8) & 0xff);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            writeWavHeader(baos, totalSamples * 2, sampleRate, 1, 16);
            baos.write(pcmData);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate development audio: {}", e.getMessage());
            return new byte[0];
        }
    }

    private void writeWavHeader(ByteArrayOutputStream out, int pcmDataLength, int sampleRate, int channels, int bitsPerSample) throws IOException {
        int byteRate = sampleRate * channels * (bitsPerSample / 8);
        int blockAlign = channels * (bitsPerSample / 8);
        int chunkSize = 36 + pcmDataLength;

        out.write(new byte[]{'R', 'I', 'F', 'F'});
        out.write(intToLittleEndianByteArray(chunkSize));
        out.write(new byte[]{'W', 'A', 'V', 'E'});
        out.write(new byte[]{'f', 'm', 't', ' '});
        out.write(intToLittleEndianByteArray(16)); // SubChunk1Size (16 for PCM)
        out.write(shortToLittleEndianByteArray((short) 1)); // AudioFormat (1 for PCM)
        out.write(shortToLittleEndianByteArray((short) channels));
        out.write(intToLittleEndianByteArray(sampleRate));
        out.write(intToLittleEndianByteArray(byteRate));
        out.write(shortToLittleEndianByteArray((short) blockAlign));
        out.write(shortToLittleEndianByteArray((short) bitsPerSample));
        out.write(new byte[]{'d', 'a', 't', 'a'});
        out.write(intToLittleEndianByteArray(pcmDataLength));
    }

    private byte[] intToLittleEndianByteArray(int value) {
        return new byte[]{
                (byte) (value & 0xff),
                (byte) ((value >> 8) & 0xff),
                (byte) ((value >> 16) & 0xff),
                (byte) ((value >> 24) & 0xff)
        };
    }

    private byte[] shortToLittleEndianByteArray(short value) {
        return new byte[]{
                (byte) (value & 0xff),
                (byte) ((value >> 8) & 0xff)
        };
    }
}
