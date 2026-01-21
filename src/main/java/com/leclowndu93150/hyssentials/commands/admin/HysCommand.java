package com.leclowndu93150.hyssentials.commands.admin;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.util.Config;
import com.leclowndu93150.hyssentials.config.HyssentialsConfig;
import com.leclowndu93150.hyssentials.manager.HomeManager;
import com.leclowndu93150.hyssentials.manager.RankManager;
import javax.annotation.Nonnull;

public class HysCommand extends AbstractCommandCollection {
    public HysCommand(@Nonnull RankManager rankManager, @Nonnull HomeManager homeManager, @Nonnull Config<HyssentialsConfig> config) {
        super("hyssentials", "Hyssentials admin commands");
        this.addAliases("hys");
        this.addSubCommand(new RankSubCommand(rankManager));
        this.addSubCommand(new AssignSubCommand(rankManager));
        this.addSubCommand(new SetRankSubCommand(rankManager));
        this.addSubCommand(new RemoveRankSubCommand(rankManager));
        this.addSubCommand(new PlayerInfoSubCommand(rankManager, homeManager));
        this.addSubCommand(new ReloadSubCommand(rankManager, config));
    }

    @Override
    protected boolean canGeneratePermission() {
        // Don't require a permission for the parent command itself
        // Each subcommand has its own permission requirement
        return false;
    }
}
