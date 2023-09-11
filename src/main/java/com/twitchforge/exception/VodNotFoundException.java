package com.twitchforge.exception;

public class VodNotFoundException extends RuntimeException {
    public VodNotFoundException() {
        super("Unfortunately, I couldn't find the requested VOD\uD83D\uDE22");
    }
}
