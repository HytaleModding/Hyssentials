package dev.hytalemodding.hyssentials.data;

import javax.annotation.Nonnull;

public class CommandSettings {
    private boolean enabled = true;
    private int cooldownSeconds = 60;
    private int warmupSeconds = 3;

    public CommandSettings() {
    }

    public CommandSettings(boolean enabled, int cooldownSeconds, int warmupSeconds) {
        this.enabled = enabled;
        this.cooldownSeconds = cooldownSeconds;
        this.warmupSeconds = warmupSeconds;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public int getWarmupSeconds() {
        return warmupSeconds;
    }

    public void setWarmupSeconds(int warmupSeconds) {
        this.warmupSeconds = warmupSeconds;
    }

    @Nonnull
    public static CommandSettings defaultSettings() {
        return new CommandSettings(true, 60, 3);
    }

    @Nonnull
    public static CommandSettings vipSettings() {
        return new CommandSettings(true, 0, 0);
    }

    @Nonnull
    public static CommandSettings rtpDefaultSettings() {
        return new CommandSettings(true, 300, 5); // 5 min cooldown, 5 sec warmup
    }

    @Nonnull
    public static CommandSettings rtpVipSettings() {
        return new CommandSettings(true, 60, 0); // 1 min cooldown, no warmup
    }

    public CommandSettings copy() {
        return new CommandSettings(enabled, cooldownSeconds, warmupSeconds);
    }
}
