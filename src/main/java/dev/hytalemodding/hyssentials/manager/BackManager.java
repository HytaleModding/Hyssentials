package dev.hytalemodding.hyssentials.manager;

import dev.hytalemodding.hyssentials.data.LocationData;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BackManager {
    private final Map<UUID, Deque<LocationData>> backHistory = new ConcurrentHashMap<>();
    private final int maxHistorySize;

    public BackManager(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    public void saveLocation(@Nonnull UUID playerUuid, @Nonnull LocationData location) {
        Deque<LocationData> history = backHistory.computeIfAbsent(playerUuid, k -> new ArrayDeque<>());
        history.addFirst(location);
        while (history.size() > maxHistorySize) {
            history.removeLast();
        }
    }

    @Nullable
    public LocationData getLastLocation(@Nonnull UUID playerUuid) {
        Deque<LocationData> history = backHistory.get(playerUuid);
        if (history == null || history.isEmpty()) {
            return null;
        }
        return history.pollFirst();
    }

    public boolean hasBackLocation(@Nonnull UUID playerUuid) {
        Deque<LocationData> history = backHistory.get(playerUuid);
        return history != null && !history.isEmpty();
    }

    public void clearHistory(@Nonnull UUID playerUuid) {
        backHistory.remove(playerUuid);
    }
}
