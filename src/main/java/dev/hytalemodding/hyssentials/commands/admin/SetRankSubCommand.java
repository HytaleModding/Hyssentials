package dev.hytalemodding.hyssentials.commands.admin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.hyssentials.data.Rank;
import dev.hytalemodding.hyssentials.lang.Messages;
import dev.hytalemodding.hyssentials.manager.RankManager;
import dev.hytalemodding.hyssentials.util.ChatUtil;
import dev.hytalemodding.hyssentials.util.Permissions;
import javax.annotation.Nonnull;

public class SetRankSubCommand extends AbstractPlayerCommand {
    private final RankManager rankManager;
    private final RequiredArg<String> playerArg = this.withRequiredArg("player", "Target player", ArgTypes.STRING);
    private final RequiredArg<String> rankArg = this.withRequiredArg("rank", "Rank ID", ArgTypes.STRING);

    public SetRankSubCommand(@Nonnull RankManager rankManager) {
        super("setrank", "Assign a rank to a player");
        this.rankManager = rankManager;
        this.requirePermission(Permissions.ADMIN_SETRANK);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef sender, @Nonnull World world) {
        // Permission check is handled by requirePermission() in constructor
        String targetName = playerArg.get(context);
        String rankId = rankArg.get(context);

        PlayerRef targetPlayer = Universe.get().getPlayerByUsername(targetName, NameMatching.STARTS_WITH_IGNORE_CASE);
        if (targetPlayer == null) {
            context.sendMessage(ChatUtil.parse(Messages.ERROR_PLAYER_NOT_FOUND, targetName));
            return;
        }

        Rank rank = rankManager.getRankById(rankId);
        if (rank == null) {
            context.sendMessage(ChatUtil.parse(Messages.ERROR_RANK_NOT_FOUND, rankId));
            return;
        }

        rankManager.grantRankPermission(targetPlayer.getUuid(), rankId);
        context.sendMessage(ChatUtil.parse(Messages.SUCCESS_RANK_GRANTED,
            rank.getDisplayName(), targetPlayer.getUsername(), rank.getPermission()));
        targetPlayer.sendMessage(ChatUtil.parse(Messages.SUCCESS_YOU_RECEIVED_RANK, rank.getDisplayName()));
    }
}
