package com.twitchforge.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Quality {
    SOURCE("Source", "/chunked/index-dvr.m3u8"),
    HD("720p60fps", "/720p60/index-dvr.m3u8"),
    SHD1("480p30fps", "/480p30/index-dvr.m3u8"),
    SHD2("360p30fps", "/360p30/index-dvr.m3u8"),
    LHD("160p30fps", "/160p30/index-dvr.m3u8"),
    AUDIO("Audio only", "/audio_only/index-dvr.m3u8");

    private final String text;
    private final String m3u8Link;
}
