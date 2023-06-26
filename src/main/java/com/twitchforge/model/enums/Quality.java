package com.twitchforge.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum Quality {
    SOURCE("Source", "chunked", "source", 0.00),
    QUADK60("4k60fps", "1080p60", "3840x2160", 60.000),
    QUADK("4k30fps", "1080p30", "3840x2160", 30.000),
    QHD4k60("2580p60fps", "1080p60fps", "2580x1080", 60.000),
    QHD4k("2580p30fps", "1080p30", "2580x1080", 30.000),
    QHD60("1440p60fps", "1080p60", "2560x1440", 60.000),
    QHD("1440p30fps", "1080p30", "2560x1440", 60.000),
    FHD60("1080p60fps", "1080p60", "1920x1080", 60.000),
    FHD("1080p30fps", "1080p30", "1920x1080", 30.000),
    FMHD60("936p60fps", "936p60", "1664x936", 60.000),
    FMHD("936p30fps", "936p30", "1664x936", 30.000),
    MHD60("900p60fps", "900p60", "1600x900", 60.000),
    MHD("900p30fps", "900p30", "1600x900", 30.000),
    HD60("720p60fps", "720p60", "1280x720", 60.000),
    HD("720p30fps", "720p30", "1280x720", 30.000),
    SHD160("480p60fps", "480p60", "852x480", 60.000),
    SHD1("480p30fps", "480p30", "852x480", 30.000),
    SHD260("360p60fps", "360p60", "640x360", 60.000),
    SHD2("360p30fps", "360p30", "640x360", 30.000),
    LHD60("160p60fps", "160p60", "284x160", 60.000),
    LHD("160p30fps", "160p30", "284x160", 30.000),
    SLHD60("144p60fps", "144p60", "256×144", 60.000),
    SLHD("144p30fps", "144p30", "256×144", 30.000),
    AUDIO("Audio only", "audio_only", "0x0", 0.000);

    private final String text;
    private final String video;
    private final String resolution;
    private final double fps;

    public static Quality getQualityByVideo(String video) {
        return Arrays.stream(values())
                .filter(quality -> quality.getVideo().equals(video))
                .findFirst()
                .orElse(null);
    }

    public static Quality getQualityByResolutionAndFps(String resolution, double fps) {
        return Arrays.stream(values())
                .filter(quality -> quality.getResolution().equals(resolution) && quality.getFps() == fps)
                .findFirst()
                .orElse(null);
    }
}
