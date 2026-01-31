package dev.hytalemodding.hyssentials.commands.admin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.hyssentials.lang.Messages;
import dev.hytalemodding.hyssentials.manager.VanishManager;
import dev.hytalemodding.hyssentials.util.ChatUtil;
import dev.hytalemodding.hyssentials.util.Permissions;
import javax.annotation.Nonnull;

public class VanishCommand extends AbstractPlayerCommand {
    private final VanishManager vanishManager;

    public VanishCommand(@Nonnull VanishManager vanishManager) {
        super("vanish", "Toggle vanish mode");
        this.vanishManager = vanishManager;
        this.addAliases("v");
        this.requirePermission(Permissions.VANISH);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        // Permission check is handled by requirePermission() in constructor
        boolean nowVanished = vanishManager.toggleVanish(playerRef.getUuid());

        if (nowVanished) {
            context.sendMessage(ChatUtil.parse(Messages.SUCCESS_VANISH_ENABLED));
        } else {
            context.sendMessage(ChatUtil.parse(Messages.SUCCESS_VANISH_DISABLED));
        }
    }
}
