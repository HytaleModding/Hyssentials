package dev.hytalemodding.hyssentials.commands.admin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import dev.hytalemodding.hyssentials.config.HyssentialsConfig;
import dev.hytalemodding.hyssentials.lang.Messages;
import dev.hytalemodding.hyssentials.manager.RankManager;
import dev.hytalemodding.hyssentials.util.ChatUtil;
import dev.hytalemodding.hyssentials.util.Permissions;
import javax.annotation.Nonnull;

public class ReloadSubCommand extends AbstractPlayerCommand {
    private final RankManager rankManager;
    private final Config<HyssentialsConfig> config;

    public ReloadSubCommand(@Nonnull RankManager rankManager, @Nonnull Config<HyssentialsConfig> config) {
        super("reload", "Reload configuration and ranks");
        this.rankManager = rankManager;
        this.config = config;
        this.requirePermission(Permissions.ADMIN_RELOAD);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef sender, @Nonnull World world) {
        // Permission check is handled by requirePermission() in constructor
        try {
            config.load().join();
            rankManager.reload();
            context.sendMessage(ChatUtil.parse(Messages.SUCCESS_CONFIG_RELOADED));
        } catch (Exception e) {
            context.sendMessage(ChatUtil.parse(Messages.ERROR_RELOAD_FAILED, e.getMessage()));
        }
    }
}
