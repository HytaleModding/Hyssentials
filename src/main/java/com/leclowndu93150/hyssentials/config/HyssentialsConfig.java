package com.leclowndu93150.hyssentials.config;


import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class HyssentialsConfig {
    public static final BuilderCodec<HyssentialsConfig> CODEC = BuilderCodec
        .builder(HyssentialsConfig.class, HyssentialsConfig::new)
        .append(new KeyedCodec<>("MaxHomes", Codec.INTEGER), HyssentialsConfig::setMaxHomes, HyssentialsConfig::getMaxHomes).add()
        .append(new KeyedCodec<>("TpaTimeout", Codec.INTEGER), HyssentialsConfig::setTpaTimeout, HyssentialsConfig::getTpaTimeout).add()
        .append(new KeyedCodec<>("TpaCooldown", Codec.INTEGER), HyssentialsConfig::setTpaCooldown, HyssentialsConfig::getTpaCooldown).add()
        .append(new KeyedCodec<>("TeleportDelay", Codec.INTEGER), HyssentialsConfig::setTeleportDelay, HyssentialsConfig::getTeleportDelay).add()
        .append(new KeyedCodec<>("BackHistorySize", Codec.INTEGER), HyssentialsConfig::setBackHistorySize, HyssentialsConfig::getBackHistorySize).add()
        .build();

    private int maxHomes = 5;
    private int tpaTimeout = 60;
    private int tpaCooldown = 30;
    private int teleportDelay = 3;
    private int backHistorySize = 5;

    public HyssentialsConfig() {
    }

    public int getMaxHomes() {
        return maxHomes;
    }

    public void setMaxHomes(int maxHomes) {
        this.maxHomes = maxHomes;
    }

    public int getTpaTimeout() {
        return tpaTimeout;
    }

    public void setTpaTimeout(int tpaTimeout) {
        this.tpaTimeout = tpaTimeout;
    }

    public int getTpaCooldown() {
        return tpaCooldown;
    }

    public void setTpaCooldown(int tpaCooldown) {
        this.tpaCooldown = tpaCooldown;
    }

    public int getTeleportDelay() {
        return teleportDelay;
    }

    public void setTeleportDelay(int teleportDelay) {
        this.teleportDelay = teleportDelay;
    }

    public int getBackHistorySize() {
        return backHistorySize;
    }

    public void setBackHistorySize(int backHistorySize) {
        this.backHistorySize = backHistorySize;
    }
}
