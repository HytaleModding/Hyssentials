package dev.hytalemodding.hyssentials.data;

import java.util.UUID;

public record TpaRequest(
    UUID sender,
    UUID target,
    TpaType type,
    long timestamp
) {
    public enum TpaType {
        TPA,
        TPAHERE
    }

    public boolean isExpired(long timeoutSeconds) {
        return System.currentTimeMillis() - timestamp > timeoutSeconds * 1000;
    }
}
