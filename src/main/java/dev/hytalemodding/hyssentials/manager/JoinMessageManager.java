package dev.hytalemodding.hyssentials.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.DrainPlayerFromWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateTranslations;
import dev.hytalemodding.hyssentials.data.JoinMessageConfig;
import dev.hytalemodding.hyssentials.util.ChatUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class JoinMessageManager {
    private static final String CONFIG_FILE = "joinmessages.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String TRANSLATION_KEY_JOINED_WORLD = "server.general.playerJoinedWorld";
    private static final String TRANSLATION_KEY_LEFT_WORLD = "server.general.playerLeftWorld";
    private static final long JOIN_MESSAGE_DELAY_MS = 3000;

    private final Path dataDirectory;
    private final HytaleLogger logger;
    private JoinMessageConfig config;
    private final Set<UUID> justConnected = new HashSet<>();
    private final Set<UUID> disconnecting = new HashSet<>();
    private final ScheduledExecutorService scheduler;

    public JoinMessageManager(@Nonnull Path dataDirectory, @Nonnull HytaleLogger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Hyssentials-JoinMessages");
            t.setDaemon(true);
            return t;
        });
        load();
    }

    public void load() {
        Path file = dataDirectory.resolve(CONFIG_FILE);
        if (Files.exists(file)) {
            try {
                String json = Files.readString(file);
                config = GSON.fromJson(json, JoinMessageConfig.class);
                if (config == null) {
                    config = JoinMessageConfig.createDefault();
                }
            } catch (IOException e) {
                logger.atSevere().log("Failed to load join message config: %s", e.getMessage());
                config = JoinMessageConfig.createDefault();
            }
        } else {
            config = JoinMessageConfig.createDefault();
            save();
        }
    }

    public void save() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            Path file = dataDirectory.resolve(CONFIG_FILE);
            String json = GSON.toJson(config);
            Files.writeString(file, json);
        } catch (IOException e) {
            logger.atSevere().log("Failed to save join message config: %s", e.getMessage());
        }
    }

    public void reload() {
        load();
    }

    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    @Nonnull
    public JoinMessageConfig getConfig() {
        return config;
    }

    public void overrideDefaultTranslationsForAllPlayers() {
        if (!config.enabled()) {
            return;
        }

        Map<String, String> overrides = new HashMap<>();
        overrides.put(TRANSLATION_KEY_JOINED_WORLD, "");
        overrides.put(TRANSLATION_KEY_LEFT_WORLD, "");
        UpdateTranslations packet = new UpdateTranslations(UpdateType.AddOrUpdate, overrides);

        Universe universe = Universe.get();
        if (universe == null) {
            return;
        }

        for (PlayerRef player : universe.getPlayers()) {
            player.getPacketHandler().write(packet);
        }
    }

    public void overrideDefaultTranslations(@Nonnull PlayerRef playerRef) {
        if (!config.enabled()) {
            return;
        }

        Map<String, String> overrides = new HashMap<>();
        overrides.put(TRANSLATION_KEY_JOINED_WORLD, "");
        overrides.put(TRANSLATION_KEY_LEFT_WORLD, "");
        UpdateTranslations packet = new UpdateTranslations(UpdateType.AddOrUpdate, overrides);
        playerRef.getPacketHandler().write(packet);
    }

    public void onPlayerConnect(@Nonnull PlayerConnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        justConnected.add(playerRef.getUuid());

        if (!config.enabled()) {
            return;
        }
        overrideDefaultTranslations(playerRef);

        String playerName = playerRef.getUsername();
        String formattedMessage = config.formatServerJoinMessage(playerName);
        Message message = ChatUtil.parseFormatted(formattedMessage);

        scheduler.schedule(() -> broadcastToAllPlayers(message, null), JOIN_MESSAGE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    public void onPlayerDisconnect(@Nonnull PlayerDisconnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        disconnecting.add(playerRef.getUuid());

        if (!config.enabled()) {
            return;
        }
        overrideDefaultTranslations(playerRef);

        String playerName = playerRef.getUsername();
        String formattedMessage = config.formatServerLeaveMessage(playerName);
        Message message = ChatUtil.parseFormatted(formattedMessage);

        broadcastToAllPlayers(message, playerRef.getUuid());
    }

    public void onPlayerEnterWorld(@Nonnull AddPlayerToWorldEvent event) {
        event.setBroadcastJoinMessage(false);

        World world = event.getWorld();
        PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
        if (playerRef == null) {
            return;
        }

        if (justConnected.remove(playerRef.getUuid())) {
            return;
        }

        if (!config.enabled()) {
            return;
        }

        String playerName = playerRef.getUsername();
        String worldName = getWorldDisplayName(world);
        String formattedMessage = config.formatWorldEnterMessage(playerName, worldName);
        Message message = ChatUtil.parseFormatted(formattedMessage);

        for (PlayerRef player : world.getPlayerRefs()) {
            player.sendMessage(message);
        }
        playerRef.sendMessage(message);
    }

    public void onPlayerLeaveWorld(@Nonnull DrainPlayerFromWorldEvent event) {
        World world = event.getWorld();
        PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
        if (playerRef == null) {
            return;
        }

        if (disconnecting.remove(playerRef.getUuid())) {
            return;
        }

        if (!config.enabled()) {
            return;
        }

        String playerName = playerRef.getUsername();
        String worldName = getWorldDisplayName(world);
        String formattedMessage = config.formatWorldLeaveMessage(playerName, worldName);
        Message message = ChatUtil.parseFormatted(formattedMessage);

        for (PlayerRef player : world.getPlayerRefs()) {
            if (!player.getUuid().equals(playerRef.getUuid())) {
                player.sendMessage(message);
            }
        }
    }

    private void broadcastToAllPlayers(@Nonnull Message message, UUID excludeUuid) {
        Universe universe = Universe.get();
        if (universe == null) {
            return;
        }

        for (PlayerRef player : universe.getPlayers()) {
            if (excludeUuid == null || !player.getUuid().equals(excludeUuid)) {
                player.sendMessage(message);
            }
        }
    }

    private String getWorldDisplayName(@Nonnull World world) {
        WorldConfig worldConfig = world.getWorldConfig();
        if (worldConfig.getDisplayName() != null) {
            return worldConfig.getDisplayName();
        }
        return WorldConfig.formatDisplayName(world.getName());
    }
}
