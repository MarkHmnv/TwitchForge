package com.twitchforge.util;

public enum Command {
    RETRIEVE, RECOVER, START, NONE;

    public static Command fromText(String text) {
        return switch (text) {
            case "/retrieve" -> RETRIEVE;
            case "/recover" -> RECOVER;
            case "/start", "/help" -> START;
            default -> NONE;
        };
    }
}
