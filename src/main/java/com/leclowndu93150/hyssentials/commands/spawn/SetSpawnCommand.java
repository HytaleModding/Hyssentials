package com.leclowndu93150.hyssentials.commands.spawn;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.leclowndu93150.hyssentials.manager.SpawnManager;
import javax.annotation.Nonnull;

public class SetSpawnCommand extends AbstractPlayerCommand {
    private final SpawnManager spawnManager;

    public SetSpawnCommand(@Nonnull SpawnManager spawnManager) {
        super("setspawn", "Set the server spawn at your current location");
        this.spawnManager = spawnManager;
        this.requirePermission(HytalePermissions.fromCommand("hyssentials.setspawn"));
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            context.sendMessage(Message.raw("Could not get your position."));
            return;
        }
        HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
        Vector3f rotation = headRotation != null ? headRotation.getRotation() : new Vector3f(0, 0, 0);
        Vector3d position = transform.getPosition();
        spawnManager.setSpawn(world, position, rotation);
        context.sendMessage(Message.raw(String.format(
            "Custom spawn set at %.1f, %.1f, %.1f in %s",
            position.getX(), position.getY(), position.getZ(), world.getName())));
    }
}
