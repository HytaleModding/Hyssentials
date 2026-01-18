package com.leclowndu93150.hyssentials.manager;

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
import com.leclowndu93150.hyssentials.data.JoinMessageConfig;
import com.leclowndu93150.hyssentials.util.ChatUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public class JoinMessageManager {
    private static final String CONFIG_FILE = "joinmessages.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path dataDirectory;
    private final HytaleLogger logger;
    private JoinMessageConfig config;
    private final Set<UUID> justConnected = new HashSet<>();
    private final Set<UUID> disconnecting = new HashSet<>();

    public JoinMessageManager(@Nonnull Path dataDirectory, @Nonnull HytaleLogger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
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

    @Nonnull
    public JoinMessageConfig getConfig() {
        return config;
    }

    public void onPlayerConnect(@Nonnull PlayerConnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        justConnected.add(playerRef.getUuid());

        if (!config.enabled()) {
            return;
        }

        String playerName = playerRef.getUsername();
        String formattedMessage = config.formatServerJoinMessage(playerName);
        Message message = ChatUtil.parseFormatted(formattedMessage);

        World world = event.getWorld();
        if (world != null) {
            for (PlayerRef player : world.getPlayerRefs()) {
                player.sendMessage(message);
            }
        }
    }

    public void onPlayerDisconnect(@Nonnull PlayerDisconnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        disconnecting.add(playerRef.getUuid());

        if (!config.enabled()) {
            return;
        }

        String playerName = playerRef.getUsername();
        String formattedMessage = config.formatServerLeaveMessage(playerName);
        Message message = ChatUtil.parseFormatted(formattedMessage);

        UUID worldUuid = playerRef.getWorldUuid();
        if (worldUuid != null) {
            World world = Universe.get().getWorld(worldUuid);
            if (world != null) {
                for (PlayerRef player : world.getPlayerRefs()) {
                    if (!player.getUuid().equals(playerRef.getUuid())) {
                        player.sendMessage(message);
                    }
                }
            }
        }

        disconnecting.remove(playerRef.getUuid());
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

        if (disconnecting.contains(playerRef.getUuid())) {
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

    private String getWorldDisplayName(@Nonnull World world) {
        WorldConfig worldConfig = world.getWorldConfig();
        if (worldConfig.getDisplayName() != null) {
            return worldConfig.getDisplayName();
        }
        return WorldConfig.formatDisplayName(world.getName());
    }
}
