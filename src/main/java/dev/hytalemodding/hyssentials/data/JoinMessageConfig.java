package dev.hytalemodding.hyssentials.data;

import javax.annotation.Nonnull;

public record JoinMessageConfig(
    boolean enabled,
    @Nonnull String serverJoinMessage,
    @Nonnull String serverLeaveMessage,
    @Nonnull String worldEnterMessage,
    @Nonnull String worldLeaveMessage
) {
    public static final String DEFAULT_SERVER_JOIN_MESSAGE = "{player} has joined the server";
    public static final String DEFAULT_SERVER_LEAVE_MESSAGE = "{player} has left the server";
    public static final String DEFAULT_WORLD_ENTER_MESSAGE = "{player} entered {world}";
    public static final String DEFAULT_WORLD_LEAVE_MESSAGE = "{player} left {world}";

    public static JoinMessageConfig createDefault() {
        return new JoinMessageConfig(
            false,
            DEFAULT_SERVER_JOIN_MESSAGE,
            DEFAULT_SERVER_LEAVE_MESSAGE,
            DEFAULT_WORLD_ENTER_MESSAGE,
            DEFAULT_WORLD_LEAVE_MESSAGE
        );
    }

    public String formatServerJoinMessage(@Nonnull String playerName) {
        return serverJoinMessage.replace("{player}", playerName);
    }

    public String formatServerLeaveMessage(@Nonnull String playerName) {
        return serverLeaveMessage.replace("{player}", playerName);
    }

    public String formatWorldEnterMessage(@Nonnull String playerName, @Nonnull String worldName) {
        return worldEnterMessage
            .replace("{player}", playerName)
            .replace("{world}", worldName);
    }

    public String formatWorldLeaveMessage(@Nonnull String playerName, @Nonnull String worldName) {
        return worldLeaveMessage
            .replace("{player}", playerName)
            .replace("{world}", worldName);
    }
}
