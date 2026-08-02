package com.app;

public class HeartbeatProtocol {
    public static final String PING = "PING";
    public static final String SEPARATOR = ":";

    // Formats message as "AppID:PING"
    public static String createMessage(String appId) {
        return appId + SEPARATOR + PING;
    }

    // Parses incoming strings into an array: [AppID, PING]
    public static String[] parseMessage(String message) {
        if (message == null || !message.contains(SEPARATOR)) {
            return null;
        }
        return message.split(SEPARATOR, 2);
    }
}