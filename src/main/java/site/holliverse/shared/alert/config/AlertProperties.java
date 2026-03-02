package site.holliverse.shared.alert.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.alerts")
public class AlertProperties {

    private boolean relayEnabled = false;
    private String relaySecret = "";
    private int notifyCooldownSeconds = 120;
    private String defaultOwner = "team";
    private String defaultRunbookUrl = "";
    private Map<String, OwnerConfig> owners = new HashMap<>();
    private DiscordConfig discord = new DiscordConfig();

    public boolean isRelayEnabled() {
        return relayEnabled;
    }

    public void setRelayEnabled(boolean relayEnabled) {
        this.relayEnabled = relayEnabled;
    }

    public String getRelaySecret() {
        return relaySecret;
    }

    public void setRelaySecret(String relaySecret) {
        this.relaySecret = relaySecret;
    }

    public int getNotifyCooldownSeconds() {
        return notifyCooldownSeconds;
    }

    public void setNotifyCooldownSeconds(int notifyCooldownSeconds) {
        this.notifyCooldownSeconds = notifyCooldownSeconds;
    }

    public String getDefaultOwner() {
        return defaultOwner;
    }

    public void setDefaultOwner(String defaultOwner) {
        this.defaultOwner = defaultOwner;
    }

    public String getDefaultRunbookUrl() {
        return defaultRunbookUrl;
    }

    public void setDefaultRunbookUrl(String defaultRunbookUrl) {
        this.defaultRunbookUrl = defaultRunbookUrl;
    }

    public Map<String, OwnerConfig> getOwners() {
        return owners;
    }

    public void setOwners(Map<String, OwnerConfig> owners) {
        this.owners = owners;
    }

    public DiscordConfig getDiscord() {
        return discord;
    }

    public void setDiscord(DiscordConfig discord) {
        this.discord = discord;
    }

    public static class OwnerConfig {
        private String discordUserId = "";
        private String runbookUrl = "";
        private String team = "";

        public String getDiscordUserId() {
            return discordUserId;
        }

        public void setDiscordUserId(String discordUserId) {
            this.discordUserId = discordUserId;
        }

        public String getRunbookUrl() {
            return runbookUrl;
        }

        public void setRunbookUrl(String runbookUrl) {
            this.runbookUrl = runbookUrl;
        }

        public String getTeam() {
            return team;
        }

        public void setTeam(String team) {
            this.team = team;
        }
    }

    public static class DiscordConfig {
        private boolean enabled = false;
        private String botToken = "";
        private String apiBaseUrl = "https://discord.com/api/v10";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBotToken() {
            return botToken;
        }

        public void setBotToken(String botToken) {
            this.botToken = botToken;
        }

        public String getApiBaseUrl() {
            return apiBaseUrl;
        }

        public void setApiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
        }
    }
}
