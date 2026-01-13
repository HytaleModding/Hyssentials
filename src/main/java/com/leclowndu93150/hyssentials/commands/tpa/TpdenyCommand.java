package com.leclowndu93150.hyssentials.commands.tpa;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.leclowndu93150.hyssentials.data.TpaRequest;
import com.leclowndu93150.hyssentials.manager.TpaManager;
import java.util.UUID;
import javax.annotation.Nonnull;

public class TpdenyCommand extends AbstractPlayerCommand {
    private final TpaManager tpaManager;

    public TpdenyCommand(@Nonnull TpaManager tpaManager) {
        super("tpdeny", "Deny a pending teleport request");
        this.tpaManager = tpaManager;
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        UUID targetUuid = playerRef.getUuid();
        TpaRequest request = tpaManager.getRequest(targetUuid);
        if (request == null) {
            context.sendMessage(Message.raw("You have no pending teleport requests."));
            return;
        }
        tpaManager.denyRequest(targetUuid);
        PlayerRef senderPlayer = Universe.get().getPlayer(request.sender());
        if (senderPlayer != null) {
            senderPlayer.sendMessage(Message.raw(String.format("%s denied your teleport request.", playerRef.getUsername())));
        }
        context.sendMessage(Message.raw("Teleport request denied."));
    }
}
