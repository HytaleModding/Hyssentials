package dev.hytalemodding.hyssentials.manager;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.hytalemodding.hyssentials.data.TpaRequest;
import dev.hytalemodding.hyssentials.data.TpaSettings;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TpaManager {
    private final Map<UUID, TpaRequest> pendingRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final RankManager rankManager;

    public TpaManager(@Nonnull RankManager rankManager) {
        this.rankManager = rankManager;
    }

    public boolean sendRequest(@Nonnull UUID sender, @Nonnull UUID target, @Nonnull TpaRequest.TpaType type, int timeoutSeconds) {
        cleanupExpired(timeoutSeconds);
        if (pendingRequests.containsKey(target)) {
            TpaRequest existing = pendingRequests.get(target);
            if (existing.sender().equals(sender) && !existing.isExpired(timeoutSeconds)) {
                return false;
            }
        }
        pendingRequests.put(target, new TpaRequest(sender, target, type, System.currentTimeMillis()));
        return true;
    }

    @Nullable
    public TpaRequest getRequest(@Nonnull UUID target, int timeoutSeconds) {
        cleanupExpired(timeoutSeconds);
        TpaRequest request = pendingRequests.get(target);
        if (request != null && request.isExpired(timeoutSeconds)) {
            pendingRequests.remove(target);
            return null;
        }
        return request;
    }

    @Nullable
    public TpaRequest acceptRequest(@Nonnull UUID target, int timeoutSeconds) {
        TpaRequest request = pendingRequests.remove(target);
        if (request != null && !request.isExpired(timeoutSeconds)) {
            return request;
        }
        return null;
    }

    public boolean denyRequest(@Nonnull UUID target) {
        return pendingRequests.remove(target) != null;
    }

    public boolean cancelRequest(@Nonnull UUID sender) {
        for (Iterator<Map.Entry<UUID, TpaRequest>> it = pendingRequests.entrySet().iterator(); it.hasNext();) {
            Map.Entry<UUID, TpaRequest> entry = it.next();
            if (entry.getValue().sender().equals(sender)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public boolean isOnCooldown(@Nonnull UUID player, int cooldownSeconds) {
        if (cooldownSeconds <= 0) {
            return false;
        }
        Long lastRequest = cooldowns.get(player);
        if (lastRequest == null) {
            return false;
        }
        return System.currentTimeMillis() - lastRequest < cooldownSeconds * 1000L;
    }

    public long getCooldownRemaining(@Nonnull UUID player, int cooldownSeconds) {
        if (cooldownSeconds <= 0) {
            return 0;
        }
        Long lastRequest = cooldowns.get(player);
        if (lastRequest == null) {
            return 0;
        }
        long remaining = (cooldownSeconds * 1000L) - (System.currentTimeMillis() - lastRequest);
        return Math.max(0, remaining / 1000);
    }

    public void setCooldown(@Nonnull UUID player) {
        cooldowns.put(player, System.currentTimeMillis());
    }

    private void cleanupExpired(int timeoutSeconds) {
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().isExpired(timeoutSeconds));
        long cutoff = System.currentTimeMillis() - (300 * 1000L);
        cooldowns.entrySet().removeIf(entry -> entry.getValue() < cutoff);
    }

    @Nonnull
    public TpaSettings getSettingsForPlayer(@Nonnull PlayerRef player) {
        return rankManager.getEffectiveTpaSettings(player);
    }

    @Nonnull
    public RankManager getRankManager() {
        return rankManager;
    }
}
