package dev.hytalemodding.hyssentials;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import dev.hytalemodding.hyssentials.commands.admin.HysCommand;
import dev.hytalemodding.hyssentials.commands.home.DelHomeCommand;
import dev.hytalemodding.hyssentials.commands.home.HomeCommand;
import dev.hytalemodding.hyssentials.commands.home.HomesCommand;
import dev.hytalemodding.hyssentials.commands.home.SetHomeCommand;
import dev.hytalemodding.hyssentials.commands.spawn.SetSpawnCommand;
import dev.hytalemodding.hyssentials.commands.spawn.SpawnCommand;
import dev.hytalemodding.hyssentials.commands.teleport.BackCommand;
import dev.hytalemodding.hyssentials.commands.teleport.RtpCommand;
import dev.hytalemodding.hyssentials.commands.teleport.TpCommand;
import dev.hytalemodding.hyssentials.commands.teleport.TphereCommand;
import dev.hytalemodding.hyssentials.commands.tpa.TpacceptCommand;
import dev.hytalemodding.hyssentials.commands.tpa.TpaCommand;
import dev.hytalemodding.hyssentials.commands.tpa.TpahereCommand;
import dev.hytalemodding.hyssentials.commands.tpa.TpcancelCommand;
import dev.hytalemodding.hyssentials.commands.tpa.TpdenyCommand;
import dev.hytalemodding.hyssentials.commands.msg.AdminChatCommand;
import dev.hytalemodding.hyssentials.commands.msg.MsgCommand;
import dev.hytalemodding.hyssentials.commands.msg.ReplyCommand;
import dev.hytalemodding.hyssentials.commands.warp.DelWarpCommand;
import dev.hytalemodding.hyssentials.commands.warp.SetWarpCommand;
import dev.hytalemodding.hyssentials.commands.warp.WarpCommand;
import dev.hytalemodding.hyssentials.commands.warp.WarpsCommand;
import dev.hytalemodding.hyssentials.config.ConfigMigrator;
import dev.hytalemodding.hyssentials.config.HyssentialsConfig;
import dev.hytalemodding.hyssentials.manager.BackManager;
import dev.hytalemodding.hyssentials.manager.CooldownManager;
import dev.hytalemodding.hyssentials.manager.DataManager;
import dev.hytalemodding.hyssentials.manager.HomeManager;
import dev.hytalemodding.hyssentials.manager.RankManager;
import dev.hytalemodding.hyssentials.manager.TeleportWarmupManager;
import dev.hytalemodding.hyssentials.manager.TpaManager;
import dev.hytalemodding.hyssentials.manager.WarpManager;
import dev.hytalemodding.hyssentials.manager.PrivateMessageManager;
import dev.hytalemodding.hyssentials.manager.AdminChatManager;
import dev.hytalemodding.hyssentials.manager.VanishManager;
import dev.hytalemodding.hyssentials.manager.JoinMessageManager;
import dev.hytalemodding.hyssentials.commands.admin.VanishCommand;
import dev.hytalemodding.hyssentials.commands.admin.FlyCommand;
import dev.hytalemodding.hyssentials.system.PlayerDeathBackSystem;
import dev.hytalemodding.hyssentials.lang.LanguageManager;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.DrainPlayerFromWorldEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class HyssentialsPlugin extends JavaPlugin {
    private final Config<HyssentialsConfig> config = this.withConfig("config", HyssentialsConfig.CODEC);
    private DataManager dataManager;
    private RankManager rankManager;
    private TpaManager tpaManager;
    private HomeManager homeManager;
    private WarpManager warpManager;
    private BackManager backManager;
    private CooldownManager cooldownManager;
    private TeleportWarmupManager warmupManager;
    private PrivateMessageManager msgManager;
    private AdminChatManager adminChatManager;
    private VanishManager vanishManager;
    private JoinMessageManager joinMessageManager;
    private ScheduledExecutorService permissionScheduler;

    public HyssentialsPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        ConfigMigrator migrator = new ConfigMigrator(this.getDataDirectory(), this.getLogger());
        migrator.migrate();

        HyssentialsConfig cfg = this.config.get();
        this.config.save();

        // Initialize language system (auto-syncs translation files)
        LanguageManager.init(this.getDataDirectory(), this.getLogger());
        LanguageManager.setLanguage(cfg.getLanguage());

        this.dataManager = new DataManager(this.getDataDirectory(), this.getLogger());
        this.rankManager = new RankManager(this.getDataDirectory(), this.getLogger(), cfg.getDefaultRankId());
        this.rankManager.setOnRanksSavedCallback(this::syncAllPlayerPermissions);
        this.backManager = new BackManager(cfg.getBackHistorySize());
        this.cooldownManager = new CooldownManager();
        this.warmupManager = new TeleportWarmupManager(this.backManager, this.cooldownManager);
        this.tpaManager = new TpaManager(this.rankManager);
        this.homeManager = new HomeManager(this.dataManager, this.rankManager);
        this.warpManager = new WarpManager(this.dataManager);
        this.msgManager = new PrivateMessageManager();
        this.adminChatManager = new AdminChatManager(this.getDataDirectory(), this.getLogger());
        this.vanishManager = new VanishManager();
        this.joinMessageManager = new JoinMessageManager(this.getDataDirectory(), this.getLogger());

        this.getEntityStoreRegistry().registerSystem(new PlayerDeathBackSystem(this.backManager));

        this.getEventRegistry().register(PlayerConnectEvent.class, this::onPlayerConnect);
        this.getEventRegistry().register(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
        this.getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, this.joinMessageManager::onPlayerEnterWorld);
        this.getEventRegistry().registerGlobal(DrainPlayerFromWorldEvent.class, this.joinMessageManager::onPlayerLeaveWorld);
    }

    @Override
    protected void start() {
        this.getCommandRegistry().registerCommand(new TpaCommand(this.tpaManager, this.rankManager));
        this.getCommandRegistry().registerCommand(new TpahereCommand(this.tpaManager, this.rankManager));
        this.getCommandRegistry().registerCommand(new TpacceptCommand(this.tpaManager, this.warmupManager, this.rankManager));
        this.getCommandRegistry().registerCommand(new TpdenyCommand(this.tpaManager));
        this.getCommandRegistry().registerCommand(new TpcancelCommand(this.tpaManager));
        this.getCommandRegistry().registerCommand(new SetHomeCommand(this.homeManager, this.rankManager));
        this.getCommandRegistry().registerCommand(new HomeCommand(this.homeManager, this.warmupManager, this.cooldownManager, this.rankManager));
        this.getCommandRegistry().registerCommand(new DelHomeCommand(this.homeManager));
        this.getCommandRegistry().registerCommand(new HomesCommand(this.homeManager, this.rankManager));
        this.getCommandRegistry().registerCommand(new SetWarpCommand(this.warpManager));
        this.getCommandRegistry().registerCommand(new WarpCommand(this.warpManager, this.warmupManager, this.cooldownManager, this.rankManager));
        this.getCommandRegistry().registerCommand(new DelWarpCommand(this.warpManager));
        this.getCommandRegistry().registerCommand(new WarpsCommand(this.warpManager));
        this.getCommandRegistry().registerCommand(new SetSpawnCommand());
        this.getCommandRegistry().registerCommand(new SpawnCommand(this.warmupManager, this.cooldownManager, this.rankManager));
        this.getCommandRegistry().registerCommand(new BackCommand(this.backManager, this.warmupManager, this.cooldownManager, this.rankManager));
        this.getCommandRegistry().registerCommand(new RtpCommand(this.warmupManager, this.cooldownManager, this.rankManager, this.config.get().getRtpMinRange(), this.config.get().getRtpMaxRange()));
        this.getCommandRegistry().registerCommand(new TpCommand(this.backManager));
        this.getCommandRegistry().registerCommand(new TphereCommand(this.backManager));
        this.getCommandRegistry().registerCommand(new HysCommand(this.rankManager, this.homeManager, this.config));
        this.getCommandRegistry().registerCommand(new MsgCommand(this.msgManager));
        this.getCommandRegistry().registerCommand(new ReplyCommand(this.msgManager));
        this.getCommandRegistry().registerCommand(new AdminChatCommand(this.adminChatManager));
        this.getCommandRegistry().registerCommand(new VanishCommand(this.vanishManager));
        this.getCommandRegistry().registerCommand(new FlyCommand());

        this.permissionScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Hyssentials-PermissionSync");
            t.setDaemon(true);
            return t;
        });
        this.permissionScheduler.scheduleAtFixedRate(this::syncAllPlayerPermissions, 1, 1, TimeUnit.MINUTES);

        this.getLogger().at(Level.INFO).log("Hyssentials loaded with rank system!");
    }

    private void onPlayerConnect(@Nonnull PlayerConnectEvent event) {
        vanishManager.onPlayerJoin(event.getPlayerRef());
        joinMessageManager.onPlayerConnect(event);

        PlayerRef player = event.getPlayerRef();
        if (!playerHasAnyRank(player)) {
            rankManager.grantRankPermission(player.getUuid(), rankManager.getDefaultRankId());
        } else {
            rankManager.ensureGrantedPermissions(player.getUuid());
        }
    }

    private boolean playerHasAnyRank(@Nonnull PlayerRef player) {
        for (var rank : rankManager.getAllRanks()) {
            if (com.hypixel.hytale.server.core.permissions.PermissionsModule.get()
                    .hasPermission(player.getUuid(), rank.getPermission())) {
                return true;
            }
        }
        return false;
    }

    private void onPlayerDisconnect(@Nonnull PlayerDisconnectEvent event) {
        joinMessageManager.onPlayerDisconnect(event);
        vanishManager.onPlayerLeave(event.getPlayerRef().getUuid());
    }

    private void syncAllPlayerPermissions() {
        try {
            Universe universe = Universe.get();
            if (universe == null) {
                return;
            }
            for (PlayerRef player : universe.getPlayers()) {
                rankManager.ensureGrantedPermissions(player.getUuid());
            }
        } catch (Exception e) {
            this.getLogger().at(Level.WARNING).log("Failed to sync player permissions: %s", e.getMessage());
        }
    }

    public void triggerPermissionSync() {
        syncAllPlayerPermissions();
    }

    @Override
    protected void shutdown() {
        if (this.permissionScheduler != null) {
            this.permissionScheduler.shutdown();
        }
        if (this.homeManager != null) {
            this.homeManager.save();
        }
        if (this.warpManager != null) {
            this.warpManager.save();
        }
        if (this.warmupManager != null) {
            this.warmupManager.shutdown();
        }
        if (this.joinMessageManager != null) {
            this.joinMessageManager.shutdown();
        }
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public void reloadConfig() {
        this.config.load();
        this.rankManager.reload();
        if (this.adminChatManager != null) {
            this.adminChatManager.reload();
        }
        if (this.joinMessageManager != null) {
            this.joinMessageManager.reload();
        }
        // Reload language
        LanguageManager.setLanguage(this.config.get().getLanguage());
        LanguageManager.reload();
    }
}
