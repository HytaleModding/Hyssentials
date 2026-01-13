package com.leclowndu93150.hyssentials.commands.warp;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.leclowndu93150.hyssentials.manager.WarpManager;
import java.util.Set;
import javax.annotation.Nonnull;

public class WarpsCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;

    public WarpsCommand(@Nonnull WarpManager warpManager) {
        super("warps", "List all server warps");
        this.warpManager = warpManager;
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Set<String> warps = warpManager.getWarpNames();
        if (warps.isEmpty()) {
            context.sendMessage(Message.raw("No warps have been set. Use /setwarp <name> to create one."));
            return;
        }
        context.sendMessage(Message.raw(String.format("Server warps (%d):", warps.size())));
        for (String warp : warps) {
            context.sendMessage(Message.raw("  - " + warp));
        }
    }
}
