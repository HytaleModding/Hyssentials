package com.leclowndu93150.hyssentials.manager;

import com.leclowndu93150.hyssentials.data.TpaRequest;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TpaManager {
    private final Map<UUID, TpaRequest> pendingRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final int timeoutSeconds;
    private final int cooldownSeconds;

    public TpaManager(int timeoutSeconds, int cooldownSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.cooldownSeconds = cooldownSeconds;
    }

    public boolean sendRequest(@Nonnull UUID sender, @Nonnull UUID target, @Nonnull TpaRequest.TpaType type) {
        cleanupExpired();
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
    public TpaRequest getRequest(@Nonnull UUID target) {
        cleanupExpired();
        TpaRequest request = pendingRequests.get(target);
        if (request != null && request.isExpired(timeoutSeconds)) {
            pendingRequests.remove(target);
            return null;
        }
        return request;
    }

    @Nullable
    public TpaRequest acceptRequest(@Nonnull UUID target) {
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

    public boolean isOnCooldown(@Nonnull UUID player) {
        Long lastRequest = cooldowns.get(player);
        if (lastRequest == null) {
            return false;
        }
        return System.currentTimeMillis() - lastRequest < cooldownSeconds * 1000L;
    }

    public long getCooldownRemaining(@Nonnull UUID player) {
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

    private void cleanupExpired() {
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().isExpired(timeoutSeconds));
        long cutoff = System.currentTimeMillis() - (cooldownSeconds * 1000L);
        cooldowns.entrySet().removeIf(entry -> entry.getValue() < cutoff);
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
