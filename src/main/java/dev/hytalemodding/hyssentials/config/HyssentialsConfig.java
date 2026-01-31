package dev.hytalemodding.hyssentials.config;


import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class HyssentialsConfig {
    public static final int CONFIG_VERSION = 5;

    public static final BuilderCodec<HyssentialsConfig> CODEC = BuilderCodec
        .builder(HyssentialsConfig.class, HyssentialsConfig::new)
        .append(new KeyedCodec<>("ConfigVersion", Codec.INTEGER), HyssentialsConfig::setConfigVersion, HyssentialsConfig::getConfigVersion).add()
        .append(new KeyedCodec<>("BackHistorySize", Codec.INTEGER), HyssentialsConfig::setBackHistorySize, HyssentialsConfig::getBackHistorySize).add()
        .append(new KeyedCodec<>("DefaultRankId", Codec.STRING), HyssentialsConfig::setDefaultRankId, HyssentialsConfig::getDefaultRankId).add()
        .append(new KeyedCodec<>("Language", Codec.STRING), HyssentialsConfig::setLanguage, HyssentialsConfig::getLanguage).add()
        .append(new KeyedCodec<>("RtpMinRange", Codec.INTEGER), HyssentialsConfig::setRtpMinRange, HyssentialsConfig::getRtpMinRange).add()
        .append(new KeyedCodec<>("RtpMaxRange", Codec.INTEGER), HyssentialsConfig::setRtpMaxRange, HyssentialsConfig::getRtpMaxRange).add()
        .build();

    private int configVersion = CONFIG_VERSION;
    private int backHistorySize = 5;
    private String defaultRankId = "default";
    private String language = "en";
    private int rtpMinRange = 100;
    private int rtpMaxRange = 5000;

    public HyssentialsConfig() {
    }

    public int getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(int configVersion) {
        this.configVersion = configVersion;
    }

    public int getBackHistorySize() {
        return backHistorySize;
    }

    public void setBackHistorySize(int backHistorySize) {
        this.backHistorySize = backHistorySize;
    }

    public String getDefaultRankId() {
        return defaultRankId;
    }

    public void setDefaultRankId(String defaultRankId) {
        this.defaultRankId = defaultRankId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getRtpMinRange() {
        return rtpMinRange;
    }

    public void setRtpMinRange(int rtpMinRange) {
        this.rtpMinRange = rtpMinRange;
    }

    public int getRtpMaxRange() {
        return rtpMaxRange;
    }

    public void setRtpMaxRange(int rtpMaxRange) {
        this.rtpMaxRange = rtpMaxRange;
    }
}
