package dev.hytalemodding.hyssentials.commands.tpa;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.hyssentials.data.TpaRequest;
import dev.hytalemodding.hyssentials.data.TpaSettings;
import dev.hytalemodding.hyssentials.gui.TpaPlayerListGui;
import dev.hytalemodding.hyssentials.lang.Messages;
import dev.hytalemodding.hyssentials.manager.RankManager;
import dev.hytalemodding.hyssentials.manager.TpaManager;
import dev.hytalemodding.hyssentials.util.ChatUtil;
import java.util.UUID;
import javax.annotation.Nonnull;

public class TpaCommand extends AbstractPlayerCommand {
    private final TpaManager tpaManager;
    private final RankManager rankManager;

    public TpaCommand(@Nonnull TpaManager tpaManager, @Nonnull RankManager rankManager) {
        super("tpa", "Request to teleport to a player");
        this.tpaManager = tpaManager;
        this.rankManager = rankManager;
        this.setAllowsExtraArguments(true);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String input = context.getInputString().trim();
        String[] args = input.split("\\s+");

        if (args.length <= 1) {
            openTpaGui(store, ref, playerRef);
            return;
        }

        String targetName = args[1];
        sendTpaRequest(context, playerRef, targetName);
    }

    private void openTpaGui(Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        player.getPageManager().openCustomPage(ref, store,
            new TpaPlayerListGui(playerRef, tpaManager, rankManager, TpaRequest.TpaType.TPA, CustomPageLifetime.CanDismiss));
    }

    private void sendTpaRequest(CommandContext context, PlayerRef playerRef, String targetName) {
        UUID senderUuid = playerRef.getUuid();
        TpaSettings settings = rankManager.getEffectiveTpaSettings(playerRef);

        if (!settings.isEnabled()) {
            context.sendMessage(ChatUtil.parse(Messages.NO_PERMISSION_TPA));
            return;
        }

        if (tpaManager.isOnCooldown(senderUuid, settings.getCooldownSeconds())) {
            long remaining = tpaManager.getCooldownRemaining(senderUuid, settings.getCooldownSeconds());
            context.sendMessage(ChatUtil.parse(Messages.COOLDOWN_TPA, remaining));
            return;
        }

        PlayerRef targetPlayer = Universe.get().getPlayerByUsername(targetName, NameMatching.STARTS_WITH_IGNORE_CASE);
        if (targetPlayer == null) {
            context.sendMessage(ChatUtil.parse(Messages.ERROR_PLAYER_NOT_FOUND, targetName));
            return;
        }

        if (targetPlayer.getUuid().equals(senderUuid)) {
            context.sendMessage(ChatUtil.parse(Messages.ERROR_CANNOT_TELEPORT_SELF));
            return;
        }

        boolean sent = tpaManager.sendRequest(senderUuid, targetPlayer.getUuid(), TpaRequest.TpaType.TPA, settings.getTimeoutSeconds());
        if (!sent) {
            context.sendMessage(ChatUtil.parse(Messages.ERROR_ALREADY_PENDING_TPA));
            return;
        }

        tpaManager.setCooldown(senderUuid);
        context.sendMessage(ChatUtil.parse(Messages.SUCCESS_TPA_SENT, targetPlayer.getUsername()));
        targetPlayer.sendMessage(ChatUtil.parse(Messages.INFO_TPA_REQUEST_RECEIVED,
            playerRef.getUsername(), settings.getTimeoutSeconds()));
    }
}
