package site.holliverse.shared.alert.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import site.holliverse.shared.alert.config.AlertProperties;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DiscordBotClient {

    private static final String CREATE_DM_CHANNEL_URI = "/users/@me/channels";

    private final ObjectMapper objectMapper;
    private final AlertProperties alertProperties;
    private final HttpClient httpClient;

    public DiscordBotClient(ObjectMapper objectMapper, AlertProperties alertProperties) {
        this.objectMapper = objectMapper;
        this.alertProperties = alertProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public void sendDirectMessage(String discordUserId, String message) {
        AlertProperties.DiscordConfig discord = alertProperties.getDiscord();
        if (!discord.isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(discord.getBotToken()) || !StringUtils.hasText(discordUserId)) {
            return;
        }

        try {
            String channelId = createDmChannel(discordUserId, discord);
            if (!StringUtils.hasText(channelId)) {
                return;
            }
            sendChannelMessage(channelId, message, discord);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("discord bot dm send failed", e);
        }
    }

    private String createDmChannel(String discordUserId, AlertProperties.DiscordConfig discord)
            throws IOException, InterruptedException {
        Map<String, String> payload = Map.of("recipient_id", discordUserId);
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(normalizeBaseUrl(discord.getApiBaseUrl()) + CREATE_DM_CHANNEL_URI))
                .timeout(Duration.ofSeconds(3))
                .header("Authorization", "Bot " + discord.getBotToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            log.warn("discord dm channel create failed status={}, body={}", response.statusCode(), response.body());
            return null;
        }
        Map<?, ?> body = objectMapper.readValue(response.body(), Map.class);
        Object id = body.get("id");
        return id != null ? id.toString() : null;
    }

    private void sendChannelMessage(String channelId, String message, AlertProperties.DiscordConfig discord)
            throws IOException, InterruptedException {
        String endpoint = normalizeBaseUrl(discord.getApiBaseUrl()) + "/channels/" + channelId + "/messages";
        Map<String, String> payload = new HashMap<>();
        payload.put("content", message);

        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(3))
                .header("Authorization", "Bot " + discord.getBotToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            log.warn("discord dm message send failed status={}, body={}", response.statusCode(), response.body());
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private String toJson(Map<String, String> payload) throws JsonProcessingException {
        return objectMapper.writeValueAsString(payload);
    }
}
