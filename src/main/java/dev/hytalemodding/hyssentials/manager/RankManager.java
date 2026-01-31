package dev.hytalemodding.hyssentials.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.hytalemodding.hyssentials.data.CommandSettings;
import dev.hytalemodding.hyssentials.data.Rank;
import dev.hytalemodding.hyssentials.data.TpaSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class RankManager {
    private static final String RANKS_FILE = "ranks.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path dataDirectory;
    private final HytaleLogger logger;
    private final List<Rank> ranks;
    private final String defaultRankId;
    private Runnable onRanksSavedCallback;

    private static final String LEGACY_VIP_PERMISSION = "hyssentials.vip";
    private static final String LEGACY_VIP_HOMES = "hyssentials.vip.homes";
    private static final String LEGACY_VIP_COOLDOWN = "hyssentials.vip.cooldown";
    public static final String COOLDOWN_BYPASS = "hyssentials.cooldown.bypass";

    public RankManager(@Nonnull Path dataDirectory, @Nonnull HytaleLogger logger, @Nonnull String defaultRankId) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.defaultRankId = defaultRankId;
        this.ranks = new CopyOnWriteArrayList<>();
        loadRanks();
    }

    private void loadRanks() {
        Path file = dataDirectory.resolve(RANKS_FILE);
        if (!Files.exists(file)) {
            logger.atInfo().log("No ranks.json found, creating defaults");
            createDefaultRanks();
            saveRanks();
            return;
        }

        try {
            String json = Files.readString(file);
            Type type = new TypeToken<RanksWrapper>(){}.getType();
            RanksWrapper wrapper = GSON.fromJson(json, type);

            ranks.clear();
            if (wrapper != null && wrapper.ranks != null) {
                ranks.addAll(wrapper.ranks);
            }

            ranks.sort(Comparator.comparingInt(Rank::getPriority).reversed());
            logger.atInfo().log("Loaded %d ranks from %s", ranks.size(), RANKS_FILE);

            if (getRankById(defaultRankId) == null) {
                logger.atWarning().log("Default rank '%s' not found, creating it", defaultRankId);
                ranks.add(Rank.createDefault());
                saveRanks();
            }
        } catch (IOException e) {
            logger.atSevere().log("Failed to load ranks: %s", e.getMessage());
            createDefaultRanks();
        }
    }

    private void createDefaultRanks() {
        ranks.clear();
        ranks.add(Rank.createDefault());
        ranks.add(Rank.createVip());
        ranks.sort(Comparator.comparingInt(Rank::getPriority).reversed());
    }

    public void saveRanks() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            RanksWrapper wrapper = new RanksWrapper();
            wrapper.ranks = new ArrayList<>(ranks);

            Path file = dataDirectory.resolve(RANKS_FILE);
            Files.writeString(file, GSON.toJson(wrapper));
            logger.atInfo().log("Saved %d ranks to %s", ranks.size(), RANKS_FILE);

            if (onRanksSavedCallback != null) {
                onRanksSavedCallback.run();
            }
        } catch (IOException e) {
            logger.atSevere().log("Failed to save ranks: %s", e.getMessage());
        }
    }

    public void setOnRanksSavedCallback(@Nullable Runnable callback) {
        this.onRanksSavedCallback = callback;
    }

    public void reload() {
        loadRanks();
    }

    @Nonnull
    public List<Rank> getAllRanks() {
        return new ArrayList<>(ranks);
    }

    @Nullable
    public Rank getRankById(@Nonnull String id) {
        for (Rank rank : ranks) {
            if (rank.getId().equalsIgnoreCase(id)) {
                return rank;
            }
        }
        return null;
    }

    @Nonnull
    public Rank getPlayerRank(@Nonnull PlayerRef player) {
        return getPlayerRank(player.getUuid());
    }

    @Nonnull
    public Rank getPlayerRank(@Nonnull UUID playerUuid) {
        PermissionsModule perms = PermissionsModule.get();

        for (Rank rank : ranks) {
            if (perms.hasPermission(playerUuid, rank.getPermission())) {
                return rank;
            }
        }

        if (perms.hasPermission(playerUuid, LEGACY_VIP_PERMISSION)) {
            Rank vipRank = getRankById("vip");
            if (vipRank != null) {
                return vipRank;
            }
        }

        Rank defaultRank = getRankById(defaultRankId);
        return defaultRank != null ? defaultRank : Rank.createDefault();
    }

    public boolean canBypassCooldown(@Nonnull PlayerRef player) {
        return PermissionsModule.get().hasPermission(player.getUuid(), COOLDOWN_BYPASS);
    }

    public boolean hasLegacyVipHomes(@Nonnull PlayerRef player) {
        return PermissionsModule.get().hasPermission(player.getUuid(), LEGACY_VIP_HOMES);
    }

    public boolean hasLegacyVipCooldown(@Nonnull PlayerRef player) {
        return PermissionsModule.get().hasPermission(player.getUuid(), LEGACY_VIP_COOLDOWN);
    }

    public void addRank(@Nonnull Rank rank) {
        ranks.removeIf(r -> r.getId().equalsIgnoreCase(rank.getId()));
        ranks.add(rank);
        ranks.sort(Comparator.comparingInt(Rank::getPriority).reversed());
        saveRanks();
    }

    public boolean removeRank(@Nonnull String rankId) {
        if (rankId.equalsIgnoreCase(defaultRankId)) {
            logger.atWarning().log("Cannot remove default rank '%s'", defaultRankId);
            return false;
        }

        boolean removed = ranks.removeIf(r -> r.getId().equalsIgnoreCase(rankId));
        if (removed) {
            saveRanks();
        }
        return removed;
    }

    public void updateRank(@Nonnull Rank rank) {
        for (int i = 0; i < ranks.size(); i++) {
            if (ranks.get(i).getId().equalsIgnoreCase(rank.getId())) {
                ranks.set(i, rank);
                ranks.sort(Comparator.comparingInt(Rank::getPriority).reversed());
                saveRanks();
                return;
            }
        }
        addRank(rank);
    }

    public boolean deleteRank(@Nonnull String rankId) {
        return removeRank(rankId);
    }

    @Nonnull
    public String getDefaultRankId() {
        return defaultRankId;
    }

    public int getEffectiveMaxHomes(@Nonnull PlayerRef player) {
        if (hasLegacyVipHomes(player)) {
            Rank vipRank = getRankById("vip");
            if (vipRank != null) {
                return vipRank.getMaxHomes();
            }
        }
        return getPlayerRank(player).getMaxHomes();
    }

    @Nonnull
    public CommandSettings getEffectiveSettings(@Nonnull PlayerRef player, @Nonnull String commandType) {
        Rank rank = getPlayerRank(player);
        return switch (commandType.toLowerCase()) {
            case "home" -> rank.getHomeSettings();
            case "warp" -> rank.getWarpSettings();
            case "spawn" -> rank.getSpawnSettings();
            case "back" -> rank.getBackSettings();
            case "rtp" -> rank.getRtpSettings();
            default -> CommandSettings.defaultSettings();
        };
    }

    @Nonnull
    public TpaSettings getEffectiveTpaSettings(@Nonnull PlayerRef player) {
        return getPlayerRank(player).getTpaSettings();
    }

    public void grantRankPermission(@Nonnull UUID playerUuid, @Nonnull String rankId) {
        Rank rank = getRankById(rankId);
        if (rank != null) {
            String permission = rank.getPermission();
            logger.atInfo().log("[RankManager] Granting permission '%s' to player %s", permission, playerUuid);
            PermissionsModule.get().addUserPermission(playerUuid, Set.of(permission));
            boolean hasAfter = PermissionsModule.get().hasPermission(playerUuid, permission);
            logger.atInfo().log("[RankManager] After grant, hasPermission=%s", hasAfter);

            List<String> grantedPerms = rank.getGrantedPermissions();
            if (!grantedPerms.isEmpty()) {
                logger.atInfo().log("[RankManager] Granting %d extra permissions from rank", grantedPerms.size());
                PermissionsModule.get().addUserPermission(playerUuid, Set.copyOf(grantedPerms));
            }
        } else {
            logger.atWarning().log("[RankManager] Rank not found: %s", rankId);
        }
    }

    public void revokeRankPermission(@Nonnull UUID playerUuid, @Nonnull String rankId) {
        Rank rank = getRankById(rankId);
        if (rank != null) {
            String permission = rank.getPermission();
            boolean hasBefore = PermissionsModule.get().hasPermission(playerUuid, permission);
            logger.atInfo().log("[RankManager] Revoking permission '%s' from player %s (hasBefore=%s)", permission, playerUuid, hasBefore);
            PermissionsModule.get().removeUserPermission(playerUuid, Set.of(permission));
            boolean hasAfter = PermissionsModule.get().hasPermission(playerUuid, permission);
            logger.atInfo().log("[RankManager] After revoke, hasPermission=%s", hasAfter);

            List<String> grantedPerms = rank.getGrantedPermissions();
            if (!grantedPerms.isEmpty()) {
                logger.atInfo().log("[RankManager] Revoking %d extra permissions from rank", grantedPerms.size());
                PermissionsModule.get().removeUserPermission(playerUuid, Set.copyOf(grantedPerms));
            }
        } else {
            logger.atWarning().log("[RankManager] Rank not found: %s", rankId);
        }
    }

    public void ensureGrantedPermissions(@Nonnull UUID playerUuid) {
        Rank playerRank = getPlayerRank(playerUuid);
        List<String> grantedPerms = playerRank.getGrantedPermissions();
        if (grantedPerms.isEmpty()) {
            return;
        }

        PermissionsModule perms = PermissionsModule.get();
        List<String> missingPerms = new ArrayList<>();
        for (String perm : grantedPerms) {
            if (!perms.hasPermission(playerUuid, perm)) {
                missingPerms.add(perm);
            }
        }

        if (!missingPerms.isEmpty()) {
            logger.atFine().log("[RankManager] Granting %d missing permissions to player %s from rank %s",
                missingPerms.size(), playerUuid, playerRank.getId());
            perms.addUserPermission(playerUuid, Set.copyOf(missingPerms));
        }
    }

    private static class RanksWrapper {
        List<Rank> ranks;
    }
}
