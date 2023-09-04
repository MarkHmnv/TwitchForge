package com.twitchforge.util;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public final class Constants {
    private Constants() {}

    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String BOT_TOKEN = "botToken";
    public static final String LOG_CHAT_ID = "logChatId";
    public static final String BOT_USERNAME = "botUsername";
    public static final String CLIENT_ID_HEADER = "Client-ID";
    public static final String GRANT_TYPE = "client_credentials";
    public static final String TOKEN_URL = "https://id.twitch.tv/oauth2/token";
    public static final String VOD_RETRIEVE_URL = "https://api.twitch.tv/helix/videos?id=";
    public static final Pattern TWITCH_VIDEO_PATTERN = Pattern.compile("videos/(\\d+)");
    public static final Pattern VOD_TOKEN_PATTERN = Pattern.compile("/([a-zA-Z0-9_]+)//thumb");
    public static final Pattern STREAMER_NICKNAME_PATTERN = Pattern.compile(".+/(.+)/streams/\\d+");
    public static final Pattern STREAM_ID_PATTERN = Pattern.compile(".+/streams/(\\d+)");
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz");
}
