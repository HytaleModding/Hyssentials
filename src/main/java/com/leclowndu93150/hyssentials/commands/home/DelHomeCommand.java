package com.leclowndu93150.hyssentials.commands.home;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.leclowndu93150.hyssentials.manager.HomeManager;
import java.util.UUID;
import javax.annotation.Nonnull;

public class DelHomeCommand extends AbstractPlayerCommand {
    private final HomeManager homeManager;
    private final RequiredArg<String> nameArg = this.withRequiredArg("name", "Home name to delete", ArgTypes.STRING);

    public DelHomeCommand(@Nonnull HomeManager homeManager) {
        super("delhome", "Delete a home");
        this.homeManager = homeManager;
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String name = nameArg.get(context);
        UUID playerUuid = playerRef.getUuid();
        boolean deleted = homeManager.deleteHome(playerUuid, name);
        if (deleted) {
            context.sendMessage(Message.raw(String.format("Home '%s' has been deleted.", name)));
        } else {
            context.sendMessage(Message.raw(String.format("Home '%s' not found.", name)));
        }
    }
}
