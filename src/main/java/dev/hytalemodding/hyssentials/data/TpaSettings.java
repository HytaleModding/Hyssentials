package dev.hytalemodding.hyssentials.data;

import javax.annotation.Nonnull;

public class TpaSettings {
    private boolean enabled = true;
    private int cooldownSeconds = 30;
    private int warmupSeconds = 0;
    private int timeoutSeconds = 60;

    public TpaSettings() {
    }

    public TpaSettings(boolean enabled, int cooldownSeconds, int warmupSeconds, int timeoutSeconds) {
        this.enabled = enabled;
        this.cooldownSeconds = cooldownSeconds;
        this.warmupSeconds = warmupSeconds;
        this.timeoutSeconds = timeoutSeconds;
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

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Nonnull
    public static TpaSettings defaultSettings() {
        return new TpaSettings(true, 30, 0, 60);
    }

    @Nonnull
    public static TpaSettings vipSettings() {
        return new TpaSettings(true, 0, 0, 120);
    }

    public TpaSettings copy() {
        return new TpaSettings(enabled, cooldownSeconds, warmupSeconds, timeoutSeconds);
    }
}
