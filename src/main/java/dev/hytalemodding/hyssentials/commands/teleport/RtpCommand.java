package dev.hytalemodding.hyssentials.commands.teleport;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.hyssentials.data.CommandSettings;
import dev.hytalemodding.hyssentials.data.LocationData;
import dev.hytalemodding.hyssentials.lang.Messages;
import dev.hytalemodding.hyssentials.manager.CooldownManager;
import dev.hytalemodding.hyssentials.manager.RankManager;
import dev.hytalemodding.hyssentials.manager.TeleportWarmupManager;
import dev.hytalemodding.hyssentials.util.ChatUtil;
import dev.hytalemodding.hyssentials.util.Permissions;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RtpCommand extends AbstractPlayerCommand {
    public static final String RTP = "rtp";
    private static final int DEFAULT_MIN_RANGE = 100;
    private static final int DEFAULT_MAX_RANGE = 5000;
    private static final int MAX_ATTEMPTS = 15;
    private static final int MIN_Y = 1;
    private static final int MAX_Y = 256;
    private static final int MIN_SKY_LIGHT = 10;
    private static final int MIN_SURFACE_Y = 35;

    private final TeleportWarmupManager warmupManager;
    private final CooldownManager cooldownManager;
    private final RankManager rankManager;
    private final Random random = new Random();

    private final int minRange;
    private final int maxRange;

    public RtpCommand(@Nonnull TeleportWarmupManager warmupManager,
                      @Nonnull CooldownManager cooldownManager,
                      @Nonnull RankManager rankManager) {
        this(warmupManager, cooldownManager, rankManager, DEFAULT_MIN_RANGE, DEFAULT_MAX_RANGE);
    }

    public RtpCommand(@Nonnull TeleportWarmupManager warmupManager,
                      @Nonnull CooldownManager cooldownManager,
                      @Nonnull RankManager rankManager,
                      int minRange, int maxRange) {
        super("rtp", "Teleport to a random location");
        this.warmupManager = warmupManager;
        this.cooldownManager = cooldownManager;
        this.rankManager = rankManager;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.addAliases("randomtp", "wild");
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        UUID playerUuid = playerRef.getUuid();
        CommandSettings settings = rankManager.getEffectiveSettings(playerRef, RTP);
        boolean bypassCooldown = Permissions.canBypassCooldown(playerRef);

        if (!settings.isEnabled()) {
            context.sendMessage(ChatUtil.parse(Messages.NO_PERMISSION_RTP));
            return;
        }

        if (!bypassCooldown && cooldownManager.isOnCooldown(playerUuid, RTP, settings.getCooldownSeconds())) {
            long remaining = cooldownManager.getCooldownRemaining(playerUuid, RTP, settings.getCooldownSeconds());
            context.sendMessage(ChatUtil.parse(Messages.COOLDOWN_RTP, remaining));
            return;
        }

        context.sendMessage(ChatUtil.parse(Messages.INFO_RTP_SEARCHING));

        findSafeLocation(world, 0).thenAccept(location -> {
            if (location == null) {
                playerRef.sendMessage(ChatUtil.parse(Messages.ERROR_RTP_NO_SAFE_LOCATION, MAX_ATTEMPTS));
                return;
            }

            int warmupSeconds = bypassCooldown ? 0 : settings.getWarmupSeconds();
            String displayName = String.format("random location (%.0f, %.0f, %.0f)", location.x(), location.y(), location.z());
            warmupManager.startWarmup(playerRef, store, ref, world, location, warmupSeconds, RTP, displayName, null);
        }).exceptionally(ex -> {
            playerRef.sendMessage(ChatUtil.parse(Messages.ERROR_RTP_FAILED));
            return null;
        });
    }

    private CompletableFuture<LocationData> findSafeLocation(World world, int attempt) {
        if (attempt >= MAX_ATTEMPTS) {
            return CompletableFuture.completedFuture(null);
        }

        int x = generateRandomCoordinate();
        int z = generateRandomCoordinate();

        long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);

        return world.getChunkAsync(chunkIndex).thenCompose(chunk -> {
            if (chunk == null) {
                return findSafeLocation(world, attempt + 1);
            }

            Integer safeY = findSafeY(chunk, x, z);
            if (safeY == null) {
                return findSafeLocation(world, attempt + 1);
            }

            LocationData location = new LocationData(world.getName(), x + 0.5, safeY + 1.0, z + 0.5, 0, 0);
            return CompletableFuture.completedFuture(location);
        }).exceptionally(ex -> {
            return null;
        }).thenCompose(result -> {
            if (result == null && attempt < MAX_ATTEMPTS - 1) {
                return findSafeLocation(world, attempt + 1);
            }
            return CompletableFuture.completedFuture(result);
        });
    }

    private int generateRandomCoordinate() {
        int range = random.nextInt(maxRange - minRange) + minRange;
        return random.nextBoolean() ? range : -range;
    }

    private Integer findSafeY(WorldChunk chunk, int worldX, int worldZ) {
        int localX = worldX & 31;
        int localZ = worldZ & 31;

        for (int y = MAX_Y; y >= MIN_Y; y--) {
            int blockBelow = chunk.getBlock(localX, y - 1, localZ);
            int blockAt = chunk.getBlock(localX, y, localZ);
            int blockAbove = chunk.getBlock(localX, y + 1, localZ);

            if (blockBelow != 0 && blockAt == 0 && blockAbove == 0) {
                int fluidAtFeet = isFluid(localX, y, localZ, chunk.getWorld().getChunkStore());
                int fluidAtHead = isFluid(localX, y + 1, localZ, chunk.getWorld().getChunkStore());
                int fluidBelow = isFluid(localX, y - 1, localZ, chunk.getWorld().getChunkStore());

                if (fluidAtFeet != 0 || fluidAtHead != 0 || fluidBelow != 0) {
                    continue;
                }

                if (chunk.getBlockChunk() != null) {
                    byte skyLight = chunk.getBlockChunk().getSkyLight(localX, y, localZ);
                    if (skyLight < MIN_SKY_LIGHT) {
                        continue;
                    }
                }

                if (y < MIN_SURFACE_Y) {
                    continue;
                }

                return y - 1;
            }
        }

        return null;
    }

    private Integer isFluid(int x, int y, int z, ChunkStore chunkStore) {
        int chunkX = ChunkUtil.chunkCoordinate(x);
        int sectionY = ChunkUtil.indexSection(y);
        int chunkZ = ChunkUtil.chunkCoordinate(z);
        int blockIndex = ChunkUtil.indexBlock(x, y, z);
        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReference(chunkX, sectionY, chunkZ);
        FluidSection fluidSection = chunkStore.getStore().getComponent(sectionRef, FluidSection.getComponentType());
        return fluidSection.getFluidId(blockIndex);
    }
}
