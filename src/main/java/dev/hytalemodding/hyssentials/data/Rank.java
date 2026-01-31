package dev.hytalemodding.hyssentials.data;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Rank {
    public static final String PERMISSION_PREFIX = "hyssentials.rank.";

    private String id = "default";
    private String displayName = "Default";
    private int priority = 0;
    private int maxHomes = 5;
    private CommandSettings homeSettings = CommandSettings.defaultSettings();
    private CommandSettings warpSettings = CommandSettings.defaultSettings();
    private CommandSettings spawnSettings = CommandSettings.defaultSettings();
    private CommandSettings backSettings = CommandSettings.defaultSettings();
    private TpaSettings tpaSettings = TpaSettings.defaultSettings();
    private CommandSettings rtpSettings = CommandSettings.defaultSettings();
    private List<String> grantedPermissions = new ArrayList<>();

    public Rank() {
    }

    public Rank(String id, String displayName, int priority, int maxHomes,
                CommandSettings homeSettings, CommandSettings warpSettings,
                CommandSettings spawnSettings, CommandSettings backSettings,
                TpaSettings tpaSettings, CommandSettings rtpSettings, List<String> grantedPermissions) {
        this.id = id;
        this.displayName = displayName;
        this.priority = priority;
        this.maxHomes = maxHomes;
        this.homeSettings = homeSettings;
        this.warpSettings = warpSettings;
        this.spawnSettings = spawnSettings;
        this.backSettings = backSettings;
        this.tpaSettings = tpaSettings;
        this.rtpSettings = rtpSettings != null ? rtpSettings : CommandSettings.defaultSettings();
        this.grantedPermissions = grantedPermissions != null ? new ArrayList<>(grantedPermissions) : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getMaxHomes() {
        return maxHomes;
    }

    public void setMaxHomes(int maxHomes) {
        this.maxHomes = maxHomes;
    }

    public CommandSettings getHomeSettings() {
        return homeSettings;
    }

    public void setHomeSettings(CommandSettings homeSettings) {
        this.homeSettings = homeSettings;
    }

    public CommandSettings getWarpSettings() {
        return warpSettings;
    }

    public void setWarpSettings(CommandSettings warpSettings) {
        this.warpSettings = warpSettings;
    }

    public CommandSettings getSpawnSettings() {
        return spawnSettings;
    }

    public void setSpawnSettings(CommandSettings spawnSettings) {
        this.spawnSettings = spawnSettings;
    }

    public CommandSettings getBackSettings() {
        return backSettings;
    }

    public void setBackSettings(CommandSettings backSettings) {
        this.backSettings = backSettings;
    }

    public TpaSettings getTpaSettings() {
        return tpaSettings;
    }

    public void setTpaSettings(TpaSettings tpaSettings) {
        this.tpaSettings = tpaSettings;
    }

    public CommandSettings getRtpSettings() {
        return rtpSettings;
    }

    public void setRtpSettings(CommandSettings rtpSettings) {
        this.rtpSettings = rtpSettings;
    }

    @Nonnull
    public List<String> getGrantedPermissions() {
        return grantedPermissions;
    }

    public void setGrantedPermissions(List<String> grantedPermissions) {
        this.grantedPermissions = grantedPermissions != null ? new ArrayList<>(grantedPermissions) : new ArrayList<>();
    }

    public void addGrantedPermission(String permission) {
        if (!grantedPermissions.contains(permission)) {
            grantedPermissions.add(permission);
        }
    }

    public void removeGrantedPermission(String permission) {
        grantedPermissions.remove(permission);
    }

    @Nonnull
    public String getPermission() {
        return PERMISSION_PREFIX + id;
    }

    @Nonnull
    public static Rank createDefault() {
        return new Rank(
            "default",
            "Default",
            0,
            5,
            CommandSettings.defaultSettings(),
            CommandSettings.defaultSettings(),
            CommandSettings.defaultSettings(),
            CommandSettings.defaultSettings(),
            TpaSettings.defaultSettings(),
            CommandSettings.rtpDefaultSettings(),
            new ArrayList<>()
        );
    }

    @Nonnull
    public static Rank createVip() {
        return new Rank(
            "vip",
            "VIP",
            10,
            10,
            CommandSettings.vipSettings(),
            CommandSettings.vipSettings(),
            CommandSettings.vipSettings(),
            CommandSettings.vipSettings(),
            TpaSettings.vipSettings(),
            CommandSettings.rtpVipSettings(),
            new ArrayList<>()
        );
    }

    public Rank copy() {
        return new Rank(
            id,
            displayName,
            priority,
            maxHomes,
            homeSettings.copy(),
            warpSettings.copy(),
            spawnSettings.copy(),
            backSettings.copy(),
            tpaSettings.copy(),
            rtpSettings.copy(),
            new ArrayList<>(grantedPermissions)
        );
    }
}
