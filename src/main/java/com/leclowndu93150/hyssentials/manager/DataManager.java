package com.leclowndu93150.hyssentials.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.logger.HytaleLogger;
import com.leclowndu93150.hyssentials.data.LocationData;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public class DataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path dataDirectory;
    private final HytaleLogger logger;

    public DataManager(@Nonnull Path dataDirectory, @Nonnull HytaleLogger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    public <T> Map<String, T> loadData(String fileName, Type type) {
        Path file = dataDirectory.resolve(fileName);
        if (!Files.exists(file, new LinkOption[0])) {
            logger.atInfo().log("No existing %s file found, starting fresh.", fileName);
            return new ConcurrentHashMap<>();
        }
        try {
            String json = Files.readString(file);
            Map<String, T> loaded = GSON.fromJson(json, type);
            if (loaded != null) {
                logger.atInfo().log("Loaded %d entries from %s", loaded.size(), fileName);
                return new ConcurrentHashMap<>(loaded);
            }
        } catch (IOException e) {
            logger.atSevere().log("Failed to load %s: %s", fileName, e.getMessage());
        }
        return new ConcurrentHashMap<>();
    }

    public <T> void saveData(String fileName, Map<String, T> data, Type type) {
        try {
            if (!Files.exists(dataDirectory, new LinkOption[0])) {
                Files.createDirectories(dataDirectory);
            }
            Path file = dataDirectory.resolve(fileName);
            String json = GSON.toJson(data, type);
            Files.writeString(file, json);
            logger.atFine().log("Saved %d entries to %s", data.size(), fileName);
        } catch (IOException e) {
            logger.atSevere().log("Failed to save %s: %s", fileName, e.getMessage());
        }
    }

    public Map<UUID, Map<String, LocationData>> loadPlayerHomes() {
        Type type = new TypeToken<Map<String, Map<String, LocationData>>>(){}.getType();
        Map<String, Map<String, LocationData>> stringMap = loadData("homes.json", type);
        Map<UUID, Map<String, LocationData>> result = new ConcurrentHashMap<>();
        int removedCount = 0;
        for (Map.Entry<String, Map<String, LocationData>> entry : stringMap.entrySet()) {
            try {
                UUID uuid = UUID.fromString(entry.getKey());
                Map<String, LocationData> filteredHomes = new ConcurrentHashMap<>();
                for (Map.Entry<String, LocationData> homeEntry : entry.getValue().entrySet()) {
                    if (homeEntry.getValue().worldName() != null &&
                        homeEntry.getValue().worldName().startsWith("instance-")) {
                        removedCount++;
                    } else {
                        filteredHomes.put(homeEntry.getKey(), homeEntry.getValue());
                    }
                }
                if (!filteredHomes.isEmpty()) {
                    result.put(uuid, filteredHomes);
                }
            } catch (IllegalArgumentException e) {
                logger.atWarning().log("Invalid UUID in homes file: %s", entry.getKey());
            }
        }
        if (removedCount > 0) {
            logger.atInfo().log("Removed %d homes in instance worlds (temporary worlds)", removedCount);
        }
        return result;
    }

    public void savePlayerHomes(Map<UUID, Map<String, LocationData>> homes) {
        Type type = new TypeToken<Map<String, Map<String, LocationData>>>(){}.getType();
        Map<String, Map<String, LocationData>> stringMap = new ConcurrentHashMap<>();
        for (Map.Entry<UUID, Map<String, LocationData>> entry : homes.entrySet()) {
            stringMap.put(entry.getKey().toString(), entry.getValue());
        }
        saveData("homes.json", stringMap, type);
    }

    public Map<String, LocationData> loadWarps() {
        Type type = new TypeToken<Map<String, LocationData>>(){}.getType();
        Map<String, LocationData> loaded = loadData("warps.json", type);
        Map<String, LocationData> filtered = new ConcurrentHashMap<>();
        int removedCount = 0;
        for (Map.Entry<String, LocationData> entry : loaded.entrySet()) {
            if (entry.getValue().worldName() != null &&
                entry.getValue().worldName().startsWith("instance-")) {
                removedCount++;
            } else {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        if (removedCount > 0) {
            logger.atInfo().log("Removed %d warps in instance worlds (temporary worlds)", removedCount);
        }
        return filtered;
    }

    public void saveWarps(Map<String, LocationData> warps) {
        Type type = new TypeToken<Map<String, LocationData>>(){}.getType();
        saveData("warps.json", warps, type);
    }
}
