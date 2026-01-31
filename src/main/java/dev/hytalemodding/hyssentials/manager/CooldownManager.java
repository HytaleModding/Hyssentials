package dev.hytalemodding.hyssentials.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public class CooldownManager {
    private final Map<String, Map<UUID, Long>> cooldowns = new ConcurrentHashMap<>();

    public static final String HOME = "home";
    public static final String WARP = "warp";
    public static final String SPAWN = "spawn";
    public static final String BACK = "back";
    public static final String TPA = "tpa";

    public CooldownManager() {
    }

    public boolean isOnCooldown(@Nonnull UUID player, @Nonnull String commandType, int cooldownSeconds) {
        if (cooldownSeconds <= 0) {
            return false;
        }
        Map<UUID, Long> commandCooldowns = cooldowns.get(commandType);
        if (commandCooldowns == null) {
            return false;
        }
        Long lastUse = commandCooldowns.get(player);
        if (lastUse == null) {
            return false;
        }
        return System.currentTimeMillis() - lastUse < cooldownSeconds * 1000L;
    }

    public long getCooldownRemaining(@Nonnull UUID player, @Nonnull String commandType, int cooldownSeconds) {
        if (cooldownSeconds <= 0) {
            return 0;
        }
        Map<UUID, Long> commandCooldowns = cooldowns.get(commandType);
        if (commandCooldowns == null) {
            return 0;
        }
        Long lastUse = commandCooldowns.get(player);
        if (lastUse == null) {
            return 0;
        }
        long remaining = (cooldownSeconds * 1000L) - (System.currentTimeMillis() - lastUse);
        return Math.max(0, remaining / 1000);
    }

    public void setCooldown(@Nonnull UUID player, @Nonnull String commandType) {
        cooldowns.computeIfAbsent(commandType, k -> new ConcurrentHashMap<>())
                .put(player, System.currentTimeMillis());
        cleanupExpired(commandType);
    }

    private void cleanupExpired(@Nonnull String commandType) {
        Map<UUID, Long> commandCooldowns = cooldowns.get(commandType);
        if (commandCooldowns != null) {
            long cutoff = System.currentTimeMillis() - (300 * 1000L);
            commandCooldowns.entrySet().removeIf(entry -> entry.getValue() < cutoff);
        }
    }

    public void clearCooldown(@Nonnull UUID player, @Nonnull String commandType) {
        Map<UUID, Long> commandCooldowns = cooldowns.get(commandType);
        if (commandCooldowns != null) {
            commandCooldowns.remove(player);
        }
    }
}
