package dev.hytalemodding.hyssentials.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.logger.HytaleLogger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class ConfigMigrator {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path dataDirectory;
    private final Path configPath;
    private final Path ranksPath;
    private final HytaleLogger logger;

    public ConfigMigrator(@Nonnull Path dataDirectory, @Nonnull HytaleLogger logger) {
        this.dataDirectory = dataDirectory;
        this.configPath = dataDirectory.resolve("config.json");
        this.ranksPath = dataDirectory.resolve("ranks.json");
        this.logger = logger;
    }

    public void migrate() {
        if (!Files.exists(configPath)) {
            logger.atInfo().log("No existing config found, will create fresh config.");
            return;
        }

        try {
            String content = Files.readString(configPath);
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();

            int currentVersion = json.has("ConfigVersion") ? json.get("ConfigVersion").getAsInt() : 1;

            if (currentVersion >= HyssentialsConfig.CONFIG_VERSION) {
                logger.atInfo().log("Config is up to date (version %d).", currentVersion);
                return;
            }

            logger.atInfo().log("Migrating config from version %d to version %d...",
                currentVersion, HyssentialsConfig.CONFIG_VERSION);

            if (currentVersion < 2) {
                migrateV1ToV2(json);
            }
            if (currentVersion < 3) {
                migrateV2ToV3(json);
            }
            if (currentVersion < 4) {
                migrateV3ToV4(json);
            }

            json.addProperty("ConfigVersion", HyssentialsConfig.CONFIG_VERSION);

            Files.writeString(configPath, GSON.toJson(json));
            logger.atInfo().log("Config migration complete!");

        } catch (IOException e) {
            logger.atWarning().log("Failed to migrate config: %s", e.getMessage());
        }
    }

    private void migrateV1ToV2(@Nonnull JsonObject json) {
        // Rename old keys to new format with Seconds suffix
        renameKey(json, "TpaTimeout", "TpaTimeoutSeconds");
        renameKey(json, "TpaCooldown", "TpaCooldownSeconds");
        renameKey(json, "TeleportDelay", "TeleportDelaySeconds");

        // Add new cooldown settings (in seconds)
        if (!json.has("HomeCooldownSeconds")) {
            json.addProperty("HomeCooldownSeconds", 60);
            logger.atInfo().log("Added HomeCooldownSeconds with default value 60");
        }
        if (!json.has("WarpCooldownSeconds")) {
            json.addProperty("WarpCooldownSeconds", 60);
            logger.atInfo().log("Added WarpCooldownSeconds with default value 60");
        }
        if (!json.has("SpawnCooldownSeconds")) {
            json.addProperty("SpawnCooldownSeconds", 60);
            logger.atInfo().log("Added SpawnCooldownSeconds with default value 60");
        }
        if (!json.has("BackCooldownSeconds")) {
            json.addProperty("BackCooldownSeconds", 60);
            logger.atInfo().log("Added BackCooldownSeconds with default value 60");
        }
    }

    private void migrateV2ToV3(@Nonnull JsonObject json) {
        // Add VIP settings
        if (!json.has("VipMaxHomes")) {
            json.addProperty("VipMaxHomes", 10);
            logger.atInfo().log("Added VipMaxHomes with default value 10");
        }
        if (!json.has("VipHomeCooldownSeconds")) {
            json.addProperty("VipHomeCooldownSeconds", 0);
            logger.atInfo().log("Added VipHomeCooldownSeconds with default value 0 (no cooldown)");
        }
        if (!json.has("VipWarpCooldownSeconds")) {
            json.addProperty("VipWarpCooldownSeconds", 0);
            logger.atInfo().log("Added VipWarpCooldownSeconds with default value 0 (no cooldown)");
        }
        if (!json.has("VipSpawnCooldownSeconds")) {
            json.addProperty("VipSpawnCooldownSeconds", 0);
            logger.atInfo().log("Added VipSpawnCooldownSeconds with default value 0 (no cooldown)");
        }
        if (!json.has("VipBackCooldownSeconds")) {
            json.addProperty("VipBackCooldownSeconds", 0);
            logger.atInfo().log("Added VipBackCooldownSeconds with default value 0 (no cooldown)");
        }
    }

    private void migrateV3ToV4(@Nonnull JsonObject json) {
        // Skip if ranks.json already exists (user may have manually created it)
        if (Files.exists(ranksPath)) {
            logger.atInfo().log("ranks.json already exists, skipping rank migration");
            return;
        }

        logger.atInfo().log("Creating ranks.json from legacy config values...");

        // Extract values from old config (with defaults)
        int maxHomes = getIntOrDefault(json, "MaxHomes", 5);
        int vipMaxHomes = getIntOrDefault(json, "VipMaxHomes", 10);
        int homeCooldown = getIntOrDefault(json, "HomeCooldownSeconds", 60);
        int warpCooldown = getIntOrDefault(json, "WarpCooldownSeconds", 60);
        int spawnCooldown = getIntOrDefault(json, "SpawnCooldownSeconds", 60);
        int backCooldown = getIntOrDefault(json, "BackCooldownSeconds", 60);
        int vipHomeCooldown = getIntOrDefault(json, "VipHomeCooldownSeconds", 0);
        int vipWarpCooldown = getIntOrDefault(json, "VipWarpCooldownSeconds", 0);
        int vipSpawnCooldown = getIntOrDefault(json, "VipSpawnCooldownSeconds", 0);
        int vipBackCooldown = getIntOrDefault(json, "VipBackCooldownSeconds", 0);
        int tpaCooldown = getIntOrDefault(json, "TpaCooldownSeconds", 30);
        int tpaTimeout = getIntOrDefault(json, "TpaTimeoutSeconds", 60);
        int teleportDelay = getIntOrDefault(json, "TeleportDelaySeconds", 3);

        // Create default rank
        JsonObject defaultRank = new JsonObject();
        defaultRank.addProperty("id", "default");
        defaultRank.addProperty("displayName", "Default");
        defaultRank.addProperty("priority", 0);
        defaultRank.addProperty("maxHomes", maxHomes);
        defaultRank.add("homeSettings", createCommandSettings(true, homeCooldown, teleportDelay));
        defaultRank.add("warpSettings", createCommandSettings(true, warpCooldown, teleportDelay));
        defaultRank.add("spawnSettings", createCommandSettings(true, spawnCooldown, teleportDelay));
        defaultRank.add("backSettings", createCommandSettings(true, backCooldown, teleportDelay));
        defaultRank.add("tpaSettings", createTpaSettings(true, tpaCooldown, 0, tpaTimeout));

        // Create VIP rank
        JsonObject vipRank = new JsonObject();
        vipRank.addProperty("id", "vip");
        vipRank.addProperty("displayName", "VIP");
        vipRank.addProperty("priority", 10);
        vipRank.addProperty("maxHomes", vipMaxHomes);
        vipRank.add("homeSettings", createCommandSettings(true, vipHomeCooldown, 0));
        vipRank.add("warpSettings", createCommandSettings(true, vipWarpCooldown, 0));
        vipRank.add("spawnSettings", createCommandSettings(true, vipSpawnCooldown, 0));
        vipRank.add("backSettings", createCommandSettings(true, vipBackCooldown, 0));
        vipRank.add("tpaSettings", createTpaSettings(true, 0, 0, tpaTimeout * 2));

        // Create ranks array
        JsonArray ranksArray = new JsonArray();
        ranksArray.add(defaultRank);
        ranksArray.add(vipRank);

        // Create ranks.json
        JsonObject ranksRoot = new JsonObject();
        ranksRoot.add("ranks", ranksArray);

        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            Files.writeString(ranksPath, GSON.toJson(ranksRoot));
            logger.atInfo().log("Created ranks.json with 'default' and 'vip' ranks from legacy settings");
        } catch (IOException e) {
            logger.atWarning().log("Failed to create ranks.json: %s", e.getMessage());
        }

        // Add new config fields
        if (!json.has("DefaultRankId")) {
            json.addProperty("DefaultRankId", "default");
        }

        // Remove old VIP-specific fields (keep them commented for reference)
        // We don't actually remove them to maintain some backwards compatibility
        // if someone downgrades, but they won't be used anymore
        logger.atInfo().log("Legacy VIP config fields are now deprecated - ranks.json is used instead");
    }

    @Nonnull
    private JsonObject createCommandSettings(boolean enabled, int cooldownSeconds, int warmupSeconds) {
        JsonObject settings = new JsonObject();
        settings.addProperty("enabled", enabled);
        settings.addProperty("cooldownSeconds", cooldownSeconds);
        settings.addProperty("warmupSeconds", warmupSeconds);
        return settings;
    }

    @Nonnull
    private JsonObject createTpaSettings(boolean enabled, int cooldownSeconds, int warmupSeconds, int timeoutSeconds) {
        JsonObject settings = new JsonObject();
        settings.addProperty("enabled", enabled);
        settings.addProperty("cooldownSeconds", cooldownSeconds);
        settings.addProperty("warmupSeconds", warmupSeconds);
        settings.addProperty("timeoutSeconds", timeoutSeconds);
        return settings;
    }

    private int getIntOrDefault(@Nonnull JsonObject json, @Nonnull String key, int defaultValue) {
        return json.has(key) ? json.get(key).getAsInt() : defaultValue;
    }

    private void renameKey(@Nonnull JsonObject json, @Nonnull String oldKey, @Nonnull String newKey) {
        if (json.has(oldKey) && !json.has(newKey)) {
            json.add(newKey, json.get(oldKey));
            json.remove(oldKey);
            logger.atInfo().log("Renamed config key '%s' to '%s'", oldKey, newKey);
        }
    }
}
