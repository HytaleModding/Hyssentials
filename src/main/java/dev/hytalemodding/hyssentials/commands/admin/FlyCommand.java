package dev.hytalemodding.hyssentials.commands.admin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.hyssentials.lang.Messages;
import dev.hytalemodding.hyssentials.util.ChatUtil;
import dev.hytalemodding.hyssentials.util.Permissions;
import javax.annotation.Nonnull;

public class FlyCommand extends AbstractPlayerCommand {

    public FlyCommand() {
        super("fly", "Toggle flight mode");
        this.requirePermission(Permissions.FLY);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        MovementManager movementManager = store.getComponent(ref, MovementManager.getComponentType());
        if (movementManager == null) {
            return;
        }

        boolean currentlyCanFly = movementManager.getSettings().canFly;
        movementManager.getSettings().canFly = !currentlyCanFly;
        movementManager.update(playerRef.getPacketHandler());

        MovementStatesComponent states = (MovementStatesComponent) store.getComponent(ref, MovementStatesComponent.getComponentType());
        if (states == null) {
            return;
        }
        MovementStates newStates = states.getMovementStates().clone();
        newStates.flying = false;
        states.setSentMovementStates(newStates);

        if (!currentlyCanFly) {
            context.sendMessage(ChatUtil.parse(Messages.SUCCESS_FLY_ENABLED));
        } else {
            context.sendMessage(ChatUtil.parse(Messages.SUCCESS_FLY_DISABLED));
        }
    }
}
