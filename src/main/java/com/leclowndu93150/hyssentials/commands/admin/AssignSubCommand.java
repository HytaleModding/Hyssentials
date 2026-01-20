package com.leclowndu93150.hyssentials.commands.admin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.leclowndu93150.hyssentials.gui.PlayerRankAssignGui;
import com.leclowndu93150.hyssentials.manager.RankManager;
import com.leclowndu93150.hyssentials.util.Permissions;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class AssignSubCommand extends AbstractAsyncCommand {
    private final RankManager rankManager;

    public AssignSubCommand(@Nonnull RankManager rankManager) {
        super("assign", "Assign ranks to players");
        this.rankManager = rankManager;
        this.requirePermission(Permissions.ADMIN_RANKS);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        CommandSender sender = context.sender();
        if (sender instanceof Player player) {
            Ref<EntityStore> ref = player.getReference();
            if (ref != null && ref.isValid()) {
                Store<EntityStore> store = ref.getStore();
                World world = store.getExternalData().getWorld();
                return CompletableFuture.runAsync(() -> {
                    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                    if (playerRef != null) {
                        if (!Permissions.canManageRanks(playerRef)) {
                            context.sendMessage(Message.raw("You don't have permission to assign ranks."));
                            return;
                        }
                        player.getPageManager().openCustomPage(ref, store, new PlayerRankAssignGui(playerRef, rankManager, CustomPageLifetime.CanDismiss));
                    }
                }, world);
            }
        }
        return CompletableFuture.completedFuture(null);
    }
}
